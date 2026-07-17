package com.mistdig.misttags;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.time.Duration;
import java.time.Instant;

public class TagExpansion extends PlaceholderExpansion {

    private final MistTags plugin;

    public TagExpansion(MistTags plugin) {
        this.plugin = plugin;
    }

    @Override public String getAuthor() { return "MistDig"; }
    @Override public String getIdentifier() { return "misttags"; }
    @Override public String getVersion() { return "2.3-beta"; }
    @Override public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        String key = params.toLowerCase();
        String serverValue = serverPlaceholder(key);
        if (serverValue != null) return serverValue;
        if (player == null) return "";

        // Read-only lookup against the in-memory cache. Expiry is handled exclusively by
        // MistTags' own main-thread sweep, so this never writes to shared state and is
        // safe to call from whatever thread the requesting plugin (TAB, DeluxeMenus, etc.)
        // happens to use.
        PlayerTagData data = plugin.getData(player.getUniqueId());
        if (data == null) return switch (key) {
            case "has_prefix", "has_suffix", "has_any", "prefix_permanent", "suffix_permanent" -> "false";
            case "prefix_expires_at", "suffix_expires_at", "prefix_remaining_seconds",
                    "suffix_remaining_seconds", "last_seen",
                    "custom_prefix_cooldown_seconds", "custom_suffix_cooldown_seconds" -> "0";
            default -> "";
        };

        return switch (key) {
            case "prefix" -> TagSpacingUtil.prefix(render(data.getPrefix()));
            case "suffix" -> TagSpacingUtil.suffix(render(data.getSuffix()));
            case "display_prefix" -> TagSpacingUtil.prefix(displayValue(player, data.getPrefix(), "%luckperms_prefix%"));
            case "display_suffix" -> TagSpacingUtil.suffix(displayValue(player, data.getSuffix(), "%luckperms_suffix%"));
            case "prefix_raw" -> nullToEmpty(data.getPrefix());
            case "suffix_raw" -> nullToEmpty(data.getSuffix());
            case "prefix_plain" -> plain(render(data.getPrefix()));
            case "suffix_plain" -> plain(render(data.getSuffix()));
            case "has_prefix" -> String.valueOf(data.getPrefix() != null);
            case "has_suffix" -> String.valueOf(data.getSuffix() != null);
            case "has_any" -> String.valueOf(!data.isEmpty());
            case "prefix_expires_at" -> String.valueOf(data.getPrefixExpire());
            case "suffix_expires_at" -> String.valueOf(data.getSuffixExpire());
            case "prefix_remaining_seconds" -> String.valueOf(remainingSeconds(data.getPrefixExpire()));
            case "suffix_remaining_seconds" -> String.valueOf(remainingSeconds(data.getSuffixExpire()));
            case "prefix_remaining" -> remainingText(data.getPrefixExpire());
            case "suffix_remaining" -> remainingText(data.getSuffixExpire());
            case "prefix_permanent" -> String.valueOf(data.getPrefix() != null && data.getPrefixExpire() == 0);
            case "suffix_permanent" -> String.valueOf(data.getSuffix() != null && data.getSuffixExpire() == 0);
            case "name_color" -> TagColorUtil.colorTagForMiniMessage(render(data.getPrefix()));
            case "last_seen" -> String.valueOf(data.getLastSeen());
            case "last_seen_iso" -> data.getLastSeen() > 0 ? Instant.ofEpochMilli(data.getLastSeen()).toString() : "";
            case "custom_prefix_cooldown_seconds" -> String.valueOf(customCooldownSeconds(data.getLastCustomPrefixChange()));
            case "custom_suffix_cooldown_seconds" -> String.valueOf(customCooldownSeconds(data.getLastCustomSuffixChange()));
            default -> null;
        };
    }

    private String serverPlaceholder(String key) {
        return switch (key) {
            case "active_prefixes" -> String.valueOf(plugin.getAllPlayerData().stream().filter(d -> d.getPrefix() != null).count());
            case "active_suffixes" -> String.valueOf(plugin.getAllPlayerData().stream().filter(d -> d.getSuffix() != null).count());
            case "active_players" -> String.valueOf(plugin.getAllPlayerData().stream().filter(d -> !d.isEmpty()).count());
            case "animated_players" -> String.valueOf(plugin.getAllPlayerData().stream().filter(this::hasAnimation).count());
            case "animations_loaded" -> String.valueOf(plugin.getAnimations().size());
            case "animations_running" -> String.valueOf(plugin.getAnimations().values().stream().filter(a -> a.getFrameCount() > 1).count());
            case "cached_players" -> String.valueOf(plugin.getAllPlayerData().size());
            case "display_mode" -> plugin.getDisplayModeName();
            case "database_enabled" -> String.valueOf(plugin.isDatabaseEnabled());
            default -> null;
        };
    }

    private String render(String stored) {
        if (stored == null) return "";
        if (stored.toLowerCase().startsWith("anim:")) {
            AnimationData anim = plugin.getAnimations().get(stored.substring(5).toLowerCase());
            return anim != null ? anim.getCurrentFrame() : "";
        }
        return stored;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String displayValue(OfflinePlayer player, String mistTagsValue, String fallbackPlaceholder) {
        if (mistTagsValue != null) return render(mistTagsValue);
        String resolved = PlaceholderAPI.setPlaceholders(player, fallbackPlaceholder);
        if (resolved == null || resolved.equalsIgnoreCase(fallbackPlaceholder)) return "";
        return resolved;
    }

    private String plain(String rendered) {
        return MiniMessageSanitizer.plainText(rendered);
    }

    private long remainingSeconds(long expireAt) {
        if (expireAt <= 0) return 0;
        return Math.max(0, (expireAt - System.currentTimeMillis() + 999) / 1000);
    }

    private String remainingText(long expireAt) {
        long seconds = remainingSeconds(expireAt);
        return seconds <= 0 ? "" : DurationParser.describe(Duration.ofSeconds(seconds));
    }

    private long customCooldownSeconds(long lastChange) {
        if (lastChange <= 0) return 0;
        long endsAt = lastChange + (plugin.getCustomTagPolicy().getCooldownSeconds() * 1000L);
        return Math.max(0, (endsAt - System.currentTimeMillis() + 999) / 1000);
    }

    private boolean hasAnimation(PlayerTagData data) {
        return startsWithAnim(data.getPrefix()) || startsWithAnim(data.getSuffix());
    }

    private boolean startsWithAnim(String stored) {
        return stored != null && stored.toLowerCase().startsWith("anim:");
    }
}
