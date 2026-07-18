package com.mistdig.misttags;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Bukkit-compatible standalone chat formatting. Only registered in standalone mode.
 */
public class ChatListener implements Listener {

    private final MistTags plugin;

    public ChatListener(MistTags plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerTagData data = plugin.getData(player.getUniqueId());
        if (data == null || data.isEmpty()) return;

        String prefix = TagSpacingUtil.prefix(renderStored(data.getPrefix()));
        String suffix = TagSpacingUtil.suffix(renderStored(data.getSuffix()));
        ChatColor nameColor = TagColorUtil.deriveLastColor(renderRaw(data.getPrefix()));
        String coloredName = (nameColor == null ? "" : nameColor.toString()) + "%1$s";
        event.setFormat(escapeFormat(prefix + coloredName + suffix + ChatColor.RESET + ": %2$s"));
    }

    private String renderStored(String stored) {
        return MiniMessageSanitizer.toLegacy(renderRaw(stored));
    }

    private String renderRaw(String stored) {
        return plugin.renderStoredRaw(stored);
    }

    private String escapeFormat(String value) {
        return value.replace("%", "%%").replace("%%1$s", "%1$s").replace("%%2$s", "%2$s");
    }
}
