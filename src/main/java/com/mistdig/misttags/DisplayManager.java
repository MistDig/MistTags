package com.mistdig.misttags;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

/**
 * Renders prefixes/suffixes without any other plugin installed. Nametags/tab list use the
 * server's main scoreboard with per-player teams (a vanilla feature, not a TAB feature), and
 * chat prefixes/suffixes are handled by ChatListener.
 *
 * Active when display.mode resolves to "standalone" (the default whenever nothing else is
 * managing prefixes/suffixes -- see MistTags#resolveDisplayMode) or to "tab" (an explicit
 * TabBridge is then used instead of scoreboard teams, preserving whatever TAB/LuckPerms
 * already had until a player has an active MistTags tag. "auto" uses TAB when TAB is
 * installed so /mt addprefix can win for tagged players without blanking normal rank
 * prefixes for everyone else.
 *
 * Per-player work here (team membership, TAB pushes) is routed through
 * MistTags#getScheduler() rather than a bare Bukkit API call, so it stays correct on Folia
 * as well as on regular Paper/Purpur.
 */
public class DisplayManager implements Listener {

    // "mt_" + 36-char UUID = 39 chars, under the 40-char team name limit introduced in 1.18+.
    private static final String TEAM_PREFIX = "mt_";

    private final MistTags plugin;
    private final TabBridge tabBridge;
    private ChatListener chatListener;

    public DisplayManager(MistTags plugin) {
        this.plugin = plugin;
        this.tabBridge = plugin.isTabDisplayActive() ? new TabBridge(plugin) : null;
    }

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (plugin.getConfig().getBoolean("display.chat-format", true)) {
            chatListener = new ChatListener(plugin);
            Bukkit.getPluginManager().registerEvents(chatListener, plugin);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getScheduler().runForPlayer(p, () -> applyDisplay(p));
        }
    }

    public void disable() {
        HandlerList.unregisterAll(this);
        if (chatListener != null) {
            HandlerList.unregisterAll(chatListener);
            chatListener = null;
        }
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Player p : Bukkit.getOnlinePlayers()) {
            Team team = board.getTeam(teamName(p.getUniqueId()));
            if (team != null) team.unregister();
            if (tabBridge != null) {
                tabBridge.clear(p);
                tabBridge.clearCache(p.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        plugin.getOrCreate(p.getUniqueId(), p.getName());
        plugin.getScheduler().runForPlayer(p, () -> applyDisplay(p));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam(teamName(event.getPlayer().getUniqueId()));
        if (team != null) team.unregister();
        if (tabBridge != null) tabBridge.clearCache(event.getPlayer().getUniqueId());
    }

    public void refresh(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) plugin.getScheduler().runForPlayer(p, () -> applyDisplay(p));
    }

    public void refreshAll() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getScheduler().runForPlayer(p, () -> applyDisplay(p));
        }
    }

    /** Called by the animation ticker only when at least one frame actually advanced. */
    public void refreshAnimatedPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerTagData data = plugin.getData(p.getUniqueId());
            if (data == null) continue;
            boolean animated = startsWithAnim(data.getPrefix()) || startsWithAnim(data.getSuffix());
            if (animated) plugin.getScheduler().runForPlayer(p, () -> applyDisplay(p));
        }
    }

    private boolean startsWithAnim(String stored) {
        return stored != null && stored.toLowerCase().startsWith("anim:");
    }

    private void applyDisplay(Player player) {
        if (plugin.isTabDisplayActive()) {
            applyTab(player);
        } else {
            applyTeam(player);
        }
    }

    private void applyTab(Player player) {
        if (tabBridge == null || !tabBridge.isAvailable()) return;
        PlayerTagData data = plugin.getData(player.getUniqueId());
        String prefix = TagSpacingUtil.prefix(resolvePlain(data == null ? null : data.getPrefix()));
        String suffix = TagSpacingUtil.suffix(resolvePlain(data == null ? null : data.getSuffix()));
        String nameColor = TagColorUtil.colorTagForMiniMessage(data == null ? null : resolveRaw(data.getPrefix()));
        tabBridge.apply(player, prefix + nameColor, suffix);
    }

    private void applyTeam(Player player) {
        if (!plugin.getConfig().getBoolean("display.nametag-and-tablist", true)) return;

        PlayerTagData data = plugin.getData(player.getUniqueId());
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        String name = teamName(player.getUniqueId());
        Team team = board.getTeam(name);
        if (team == null) team = board.registerNewTeam(name);

        team.setPrefix(limitTeamPart(TagSpacingUtil.prefix(resolveLegacy(data == null ? null : data.getPrefix()))));
        team.setSuffix(limitTeamPart(TagSpacingUtil.suffix(resolveLegacy(data == null ? null : data.getSuffix()))));
        ChatColor color = TagColorUtil.deriveLastColor(data == null ? null : resolveRaw(data.getPrefix()));
        if (color != null) {
            try {
                team.setColor(color);
            } catch (NoSuchMethodError ignored) {
                // Older Bukkit builds still render the legacy colors in prefix/suffix.
            }
        }

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    private String resolveLegacy(String stored) {
        return MiniMessageSanitizer.toLegacy(resolveRaw(stored));
    }

    private String resolvePlain(String stored) {
        return resolveRaw(stored);
    }

    private String resolveRaw(String stored) {
        return plugin.renderStoredRaw(stored);
    }

    private String teamName(UUID uuid) {
        return TEAM_PREFIX + uuid;
    }

    private String limitTeamPart(String value) {
        if (value == null) return "";
        return value.length() <= 64 ? value : value.substring(0, 64);
    }
}
