package com.mistdig.misttags;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public final class IntegrationAutoConfigurator {

    private static final String DISPLAY_PREFIX = "%misttags_display_prefix%";
    private static final String DISPLAY_SUFFIX = "%misttags_display_suffix%";
    private static final Set<String> TAB_FIELDS = Set.of("tabprefix", "tagprefix", "tabsuffix", "tagsuffix");

    private final MistTags plugin;

    public IntegrationAutoConfigurator(MistTags plugin) {
        this.plugin = plugin;
    }

    public boolean configureDetectedIntegrations() {
        if (!plugin.getConfig().getBoolean("integrations.auto-configure", true)) return false;
        if (!isPluginPresent("PlaceholderAPI")) {
            plugin.getLogger().warning("Integration auto-config skipped: PlaceholderAPI is required for TAB/LPC placeholders.");
            return false;
        }

        boolean changed = false;
        if (plugin.getConfig().getBoolean("integrations.tab.enabled", true) && isPluginPresent("TAB")) {
            changed |= configureTab();
        }
        if (plugin.getConfig().getBoolean("integrations.lpc.enabled", true) && isPluginPresent("LPC")) {
            changed |= configureLpc();
        }

        if (changed && plugin.getConfig().getBoolean("integrations.auto-reload", true)) {
            plugin.getScheduler().runGlobalDelayed(this::reloadDetectedIntegrations, 60L);
        }
        return changed;
    }

    private boolean configureTab() {
        File file = new File("plugins/TAB/groups.yml");
        if (!file.isFile()) {
            plugin.getLogger().warning("TAB detected, but plugins/TAB/groups.yml was not found. TAB auto-config skipped.");
            return false;
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        boolean changed = false;
        ConfigurationSection defaults = cfg.getConfigurationSection("_DEFAULT_");
        if (defaults == null) defaults = cfg.createSection("_DEFAULT_");
        changed |= setIfDifferent(defaults, "tabprefix", DISPLAY_PREFIX);
        changed |= setIfDifferent(defaults, "tagprefix", DISPLAY_PREFIX);
        changed |= setIfDifferent(defaults, "tabsuffix", DISPLAY_SUFFIX);
        changed |= setIfDifferent(defaults, "tagsuffix", DISPLAY_SUFFIX);

        for (String key : cfg.getKeys(false)) {
            if ("_DEFAULT_".equalsIgnoreCase(key)) continue;
            ConfigurationSection section = cfg.getConfigurationSection(key);
            if (section != null) changed |= configureTabSection(section);
        }

        if (!changed) return false;
        try {
            backupOnce(file);
            cfg.save(file);
            plugin.getLogger().info("TAB groups.yml auto-configured with MistTags smart placeholders.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Could not auto-configure TAB groups.yml: " + e.getMessage());
            return false;
        }
    }

    private boolean configureTabSection(ConfigurationSection section) {
        boolean changed = false;
        boolean hasTabField = TAB_FIELDS.stream().anyMatch(section::contains);
        if (hasTabField) {
            changed |= setIfDifferent(section, "tabprefix", DISPLAY_PREFIX);
            changed |= setIfDifferent(section, "tagprefix", DISPLAY_PREFIX);
            changed |= setIfDifferent(section, "tabsuffix", DISPLAY_SUFFIX);
            changed |= setIfDifferent(section, "tagsuffix", DISPLAY_SUFFIX);
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection child = section.getConfigurationSection(key);
            if (child != null) changed |= configureTabSection(child);
        }
        return changed;
    }

    private boolean configureLpc() {
        File file = new File("plugins/LPC/config.yml");
        if (!file.isFile()) {
            plugin.getLogger().warning("LPC detected, but plugins/LPC/config.yml was not found. LPC auto-config skipped.");
            return false;
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String format = DISPLAY_PREFIX + "{name}" + DISPLAY_SUFFIX + "<dark_gray> »<reset> {message}";
        boolean changed = setIfDifferent(cfg, "chat-format", format);
        changed |= configureLpcFormatSection(cfg.getConfigurationSection("group-formats"));
        changed |= configureLpcFormatSection(cfg.getConfigurationSection("track-formats"));
        if (!changed) return false;

        try {
            backupOnce(file);
            cfg.save(file);
            plugin.getLogger().info("LPC config.yml auto-configured with MistTags smart placeholders.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Could not auto-configure LPC config.yml: " + e.getMessage());
            return false;
        }
    }

    private void reloadDetectedIntegrations() {
        if (isPluginPresent("TAB")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab reload");
        if (isPluginPresent("LPC")) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lpc reload");
    }

    private boolean configureLpcFormatSection(ConfigurationSection section) {
        if (section == null) return false;
        boolean changed = false;
        for (String key : section.getKeys(false)) {
            String value = section.getString(key);
            if (value == null) continue;
            String updated = value
                    .replace("{prefixes}", DISPLAY_PREFIX)
                    .replace("{prefix}", DISPLAY_PREFIX)
                    .replace("{suffixes}", DISPLAY_SUFFIX)
                    .replace("{suffix}", DISPLAY_SUFFIX);
            changed |= setIfDifferent(section, key, updated);
        }
        return changed;
    }

    private boolean setIfDifferent(ConfigurationSection cfg, String path, String value) {
        if (value.equals(cfg.getString(path))) return false;
        cfg.set(path, value);
        return true;
    }

    private boolean isPluginPresent(String name) {
        return Bukkit.getPluginManager().getPlugin(name) != null;
    }

    private void backupOnce(File file) throws IOException {
        File backup = new File(file.getParentFile(), file.getName() + ".misttags-backup");
        if (!backup.exists()) {
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
        }
    }
}
