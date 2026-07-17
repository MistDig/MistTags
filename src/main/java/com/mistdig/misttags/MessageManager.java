package com.mistdig.misttags;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MessageManager {

    private final MistTags plugin;
    private FileConfiguration config;

    public MessageManager(MistTags plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "msg.yml");
        if (!file.exists()) plugin.saveResource("msg.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
        try (InputStream stream = plugin.getResource("msg.yml")) {
            if (stream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
                config.setDefaults(defaults);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not load default msg.yml fallback: " + e.getMessage());
        }
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Collections.emptyMap());
    }

    public void send(CommandSender sender, String key, Map<String, String> replacements) {
        List<String> lines = config.getStringList("messages." + key);
        if (!lines.isEmpty()) {
            for (String line : lines) sender.sendMessage(format(line, replacements));
            return;
        }
        sender.sendMessage(format(config.getString("messages." + key, key), replacements));
    }

    public String raw(String key, Map<String, String> replacements) {
        return format(config.getString("messages." + key, key), replacements);
    }

    public String prefix() {
        return color(config.getString("prefix", "&8[&bMistTags&8]&r"));
    }

    private String format(String message, Map<String, String> replacements) {
        String out = message.replace("{prefix}", prefix());
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            out = out.replace("{" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }
        return color(out);
    }

    @SuppressWarnings("deprecation")
    private String color(String raw) {
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
