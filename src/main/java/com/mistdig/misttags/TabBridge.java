package com.mistdig.misttags;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pushes prefixes/suffixes into TAB's own API when display.mode resolves to DisplayMode.TAB
 * (explicit "tab", or "auto" when TAB is installed -- see MistTags#resolveDisplayMode).
 *
 * By default this preserves whatever TAB already computed for a player (e.g. from TAB's
 * own config referencing %luckperms_prefix%, or a group-based tag) until that player has
 * an active MistTags prefix/suffix. Once staff sets a MistTags tag, that tag overrides the
 * matching TAB/LuckPerms value for that player only. Set display.tab-active-tag-behavior
 * to "combine" if you want TAB/LuckPerms + MistTags shown together instead.
 *
 * The "read TAB's current value" step relies on TAB's Property API (TabPlayer#getProperty /
 * Property#getCurrentRawValue), reached via reflection since TAB isn't a compile-time
 * dependency. TAB's internal API surface has shifted across versions, so this is
 * intentionally defensive: if the expected methods aren't found, MistTags just falls back
 * to plain overwrite (the previous behavior) instead of failing to load or spamming errors.
 */
public class TabBridge {

    private final MistTags plugin;
    private Object api;
    private Method getTabPlayer;
    private Object nameTagManager;
    private Object tabListFormatManager;
    private Method nameTagPrefix;
    private Method nameTagSuffix;
    private Method tabListPrefix;
    private Method tabListSuffix;
    private Method getProperty;        // TabPlayer#getProperty(String)
    private Method getCurrentRawValue; // Property#getCurrentRawValue()
    private boolean available;
    private boolean canReadForeignValue;

    // Each player's TAB-computed prefix/suffix from BEFORE MistTags ever touched them,
    // captured once per property per player so repeated applies always combine with that
    // original TAB-side value instead of compounding on top of MistTags' own previous
    // injection. Cleared on quit/disable by DisplayManager via clearCache().
    private final Map<String, String> foreignBaseCache = new ConcurrentHashMap<>();

    public TabBridge(MistTags plugin) {
        this.plugin = plugin;
        available = bind();
    }

    public boolean isAvailable() {
        return available;
    }

    public void apply(Player player, String prefix, String suffix) {
        if (!available) return;
        try {
            Object tabPlayer = getTabPlayer.invoke(api, player.getUniqueId());
            if (tabPlayer == null) return;

            String behavior = plugin.getConfig().getString("display.tab-active-tag-behavior", "override");
            boolean combine = behavior.equalsIgnoreCase("combine");
            boolean misttagsFirst = "misttags-first"
                    .equalsIgnoreCase(plugin.getConfig().getString("display.tab-combine-order", "existing-first"));
            String separator = plugin.getConfig().getString("display.tab-combine-separator", " ");

            if (nameTagManager != null) {
                String combinedPrefix = combine(tabPlayer, player.getUniqueId(), "tagprefix", prefix, combine, misttagsFirst, separator);
                String combinedSuffix = combine(tabPlayer, player.getUniqueId(), "tagsuffix", suffix, combine, misttagsFirst, separator);
                nameTagPrefix.invoke(nameTagManager, tabPlayer, combinedPrefix);
                nameTagSuffix.invoke(nameTagManager, tabPlayer, combinedSuffix);
            }
            if (tabListFormatManager != null) {
                String combinedPrefix = combine(tabPlayer, player.getUniqueId(), "tabprefix", prefix, combine, misttagsFirst, separator);
                String combinedSuffix = combine(tabPlayer, player.getUniqueId(), "tabsuffix", suffix, combine, misttagsFirst, separator);
                tabListPrefix.invoke(tabListFormatManager, tabPlayer, combinedPrefix);
                tabListSuffix.invoke(tabListFormatManager, tabPlayer, combinedSuffix);
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            available = false;
            plugin.getLogger().warning("TAB integration failed and was disabled: " + e.getMessage());
        }
    }

    public void clear(Player player) {
        apply(player, "", "");
    }

    /** Drops the cached "foreign base" for a player so a future apply() re-reads TAB's current value fresh. */
    public void clearCache(UUID uuid) {
        String prefix = uuid.toString() + ":";
        foreignBaseCache.keySet().removeIf(key -> key.startsWith(prefix));
    }

    private String combine(Object tabPlayer, UUID uuid, String propertyName, String misttagsValue,
                           boolean combine, boolean misttagsFirst, String separator) {
        if (!canReadForeignValue) return misttagsValue;

        String base = foreignBaseCache.computeIfAbsent(uuid + ":" + propertyName,
                k -> readCurrentRawValue(tabPlayer, propertyName));

        if (!combine) {
            return misttagsValue.isEmpty() ? base : misttagsValue;
        }

        if (base.isEmpty()) return misttagsValue;
        if (misttagsValue.isEmpty()) return base;
        return misttagsFirst ? (misttagsValue + separator + base) : (base + separator + misttagsValue);
    }

    private String readCurrentRawValue(Object tabPlayer, String propertyName) {
        try {
            Object property = getProperty.invoke(tabPlayer, propertyName);
            if (property == null) return "";
            Object raw = getCurrentRawValue.invoke(property);
            return raw == null ? "" : raw.toString();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return "";
        }
    }

    private boolean bind() {
        try {
            Class<?> tabApiClass = Class.forName("me.neznamy.tab.api.TabAPI");
            api = tabApiClass.getMethod("getInstance").invoke(null);
            getTabPlayer = tabApiClass.getMethod("getPlayer", UUID.class);
            nameTagManager = invokeOptional(api, "getNameTagManager");
            tabListFormatManager = invokeOptional(api, "getTabListFormatManager");

            Class<?> tabPlayerClass = Class.forName("me.neznamy.tab.api.TabPlayer");
            if (nameTagManager != null) {
                nameTagPrefix = nameTagManager.getClass().getMethod("setPrefix", tabPlayerClass, String.class);
                nameTagSuffix = nameTagManager.getClass().getMethod("setSuffix", tabPlayerClass, String.class);
            }
            if (tabListFormatManager != null) {
                tabListPrefix = tabListFormatManager.getClass().getMethod("setPrefix", tabPlayerClass, String.class);
                tabListSuffix = tabListFormatManager.getClass().getMethod("setSuffix", tabPlayerClass, String.class);
            }

            bindForeignValueReader(tabPlayerClass);

            boolean ok = nameTagManager != null || tabListFormatManager != null;
            if (ok) {
                plugin.getLogger().info("TAB integration active: MistTags will push prefixes/suffixes into TAB"
                        + (canReadForeignValue ? " (combining with TAB's existing value)." : " (plain overwrite -- "
                        + "couldn't hook TAB's Property API to read its existing value on this TAB version)."));
            }
            return ok;
        } catch (ReflectiveOperationException | RuntimeException e) {
            plugin.getLogger().warning("TAB was detected, but its API could not be hooked: " + e.getMessage());
            return false;
        }
    }

    /**
     * Best-effort hook into TAB's Property API so apply() can read what TAB already had
     * before MistTags sets anything. Deliberately non-fatal: if TAB's version doesn't expose
     * these methods under these names, canReadForeignValue just stays false and apply()
     * falls back to plain overwrite instead of the whole integration breaking.
     */
    private void bindForeignValueReader(Class<?> tabPlayerClass) {
        try {
            getProperty = tabPlayerClass.getMethod("getProperty", String.class);
            Class<?> propertyClass = Class.forName("me.neznamy.tab.api.Property");
            getCurrentRawValue = propertyClass.getMethod("getCurrentRawValue");
            canReadForeignValue = true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            canReadForeignValue = false;
            plugin.getLogger().info("TAB integration: couldn't hook TAB's Property API on this TAB version, "
                    + "so MistTags can't read TAB/LuckPerms' existing prefix/suffix before applying its own. "
                    + "Players with active MistTags will still be overridden, but players without active "
                    + "MistTags may not preserve TAB's current value through this bridge. Consider "
                    + "display.mode: placeholder-only if that's undesirable.");
        }
    }

    private Object invokeOptional(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}
