package com.mistdig.misttags;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

/** Repairs an unchanged TAB entry after join/pack initialization, without a global reload. */
final class TabJoinRefresh implements Listener {
    private final MistTags plugin;
    private boolean warned;

    TabJoinRefresh(MistTags plugin) { this.plugin = plugin; }

    void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (Player player : Bukkit.getOnlinePlayers()) schedule(player, 20L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        schedule(event.getPlayer(), 20L);
        schedule(event.getPlayer(), 100L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPackLoaded(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            schedule(event.getPlayer(), 20L);
        }
    }

    private void schedule(Player player, long delay) {
        plugin.getScheduler().runGlobalDelayed(() -> {
            if (player.isOnline()) plugin.getScheduler().runForPlayer(player, () -> refresh(player));
        }, delay);
    }

    private void refresh(Player player) {
        if (!player.isOnline()) return;
        var tab = Bukkit.getPluginManager().getPlugin("TAB");
        if (tab == null || !tab.isEnabled()) return;
        try {
            ClassLoader loader = tab.getClass().getClassLoader();
            Class<?> apiType = loader.loadClass("me.neznamy.tab.api.TabAPI");
            Object api = apiType.getMethod("getInstance").invoke(null);
            if (api != null) TabListResender.resend(api, player.getUniqueId(),
                    loader.loadClass("me.neznamy.tab.api.TabPlayer"));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException error) {
            if (!warned) {
                warned = true;
                plugin.getLogger().warning("Could not refresh MistTags TAB entries after join: " + error);
            }
        }
    }
}
