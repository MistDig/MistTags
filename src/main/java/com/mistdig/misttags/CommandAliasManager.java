package com.mistdig.misttags;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class CommandAliasManager implements Listener {

    private static final Set<String> ALLOWED_TARGETS = Set.of(
            "misttags", "addprefix", "addsuffix", "removeprefix", "removesuffix"
    );

    private final MistTags plugin;
    private final Map<String, String> aliases = new HashMap<>();

    public CommandAliasManager(MistTags plugin) {
        this.plugin = plugin;
    }

    public void load() {
        aliases.clear();
        File file = new File(plugin.getDataFolder(), "commands.yml");
        if (!file.exists()) plugin.saveResource("commands.yml", false);

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection commands = cfg.getConfigurationSection("commands");
        if (commands == null) {
            plugin.getLogger().warning("commands.yml is missing section 'commands'. Custom command aliases disabled.");
            return;
        }

        for (String key : commands.getKeys(false)) {
            String path = "commands." + key + ".";
            String target = clean(cfg.getString(path + "target", key));
            if (!ALLOWED_TARGETS.contains(target)) {
                plugin.getLogger().warning("commands.yml alias group '" + key + "' uses invalid target '" + target + "'.");
                continue;
            }
            for (String alias : cfg.getStringList(path + "aliases")) {
                String cleaned = clean(alias);
                if (cleaned.isEmpty()) continue;
                aliases.put(cleaned, target);
            }
        }

        plugin.getLogger().info("Loaded " + aliases.size() + " MistTags command alias(es).");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String rewritten = rewrite(event.getMessage());
        if (rewritten != null) event.setMessage(rewritten);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent event) {
        String rewritten = rewrite("/" + event.getCommand());
        if (rewritten != null) event.setCommand(rewritten.substring(1));
    }

    private String rewrite(String commandLine) {
        if (commandLine == null || !commandLine.startsWith("/")) return null;
        String withoutSlash = commandLine.substring(1);
        String[] split = withoutSlash.split(" ", 2);
        String alias = clean(split[0]);
        String target = aliases.get(alias);
        if (target == null || target.equals(alias)) return null;
        return "/" + target + (split.length > 1 ? " " + split[1] : "");
    }

    private String clean(String value) {
        if (value == null) return "";
        String cleaned = value.trim().toLowerCase(Locale.ROOT);
        while (cleaned.startsWith("/")) cleaned = cleaned.substring(1);
        return cleaned;
    }
}
