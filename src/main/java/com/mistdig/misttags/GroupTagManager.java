package com.mistdig.misttags;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Per-permission-group fallback prefixes/suffixes, resolved by MistTags itself instead of
 * left as raw PlaceholderAPI/LuckPerms text. See groups.yml for why that distinction matters:
 * an animated group tag defined here always comes back as finished text, so it renders
 * correctly wherever %misttags_display_prefix%/%misttags_display_suffix% end up (chat
 * included), unlike TAB's own %animation:name% syntax pasted into a LuckPerms prefix meta
 * value, which only TAB's own renderer understands.
 */
public class GroupTagManager {

    private final MistTags plugin;
    private final Map<String, String> prefixes = new LinkedHashMap<>();
    private final Map<String, String> suffixes = new LinkedHashMap<>();

    public GroupTagManager(MistTags plugin) {
        this.plugin = plugin;
    }

    public void load() {
        prefixes.clear();
        suffixes.clear();
        File file = new File(plugin.getDataFolder(), "groups.yml");
        if (!file.exists()) plugin.saveResource("groups.yml", false);

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = cfg.getConfigurationSection("groups");
        if (section == null) return;

        for (String group : section.getKeys(false)) {
            String key = group.toLowerCase(Locale.ROOT);
            String prefix = section.getString(group + ".prefix", "");
            String suffix = section.getString(group + ".suffix", "");
            if (prefix != null && !prefix.isBlank()) prefixes.put(key, prefix);
            if (suffix != null && !suffix.isBlank()) suffixes.put(key, suffix);
        }
        plugin.getLogger().info("Loaded " + prefixes.size() + " group prefix(es) and "
                + suffixes.size() + " group suffix(es) from groups.yml.");
    }

    public String getPrefix(Player player) {
        String stored = lookup(player, prefixes);
        return stored == null ? null : render(player, stored);
    }

    public String getSuffix(Player player) {
        String stored = lookup(player, suffixes);
        return stored == null ? null : render(player, stored);
    }

    private String lookup(Player player, Map<String, String> byGroup) {
        if (byGroup.isEmpty()) return null;
        String group = primaryGroup(player);
        return group == null ? null : byGroup.get(group);
    }

    // AnimationSpec (used by MistTags#renderStoredRaw for player custom tags too, not just
    // groups.yml) requires "anim:" to be the literal start of the value -- not worth relaxing
    // that shared contract just for this. So a groups.yml value can lead with arbitrary badge
    // text before an "anim:<name>" reference, e.g. "%tick_linked%anim:sovereign": the badge
    // portion is split off and resolved here, the "anim:..." remainder goes to MistTags' own
    // renderer as usual, and the combined result gets one PlaceholderAPI pass so any
    // placeholder in either half -- a Discord-link checkmark, anything -- comes back as
    // finished text, same as every other %misttags_display_prefix%/suffix% consumer expects.
    private String render(Player player, String stored) {
        int animAt = stored.toLowerCase(Locale.ROOT).indexOf("anim:");
        String badge = animAt > 0 ? stored.substring(0, animAt) : "";
        String remainder = animAt >= 0 ? stored.substring(animAt) : stored;
        String combined = badge + plugin.renderStoredRaw(remainder);
        return resolvePlaceholders(player, combined);
    }

    private String resolvePlaceholders(Player player, String input) {
        if (input == null || input.isEmpty()) return input;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return input;
        String resolved = PlaceholderAPI.setPlaceholders(player, input);
        return resolved == null ? input : resolved;
    }

    private String primaryGroup(Player player) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) return null;
        String group = PlaceholderAPI.setPlaceholders(player, "%luckperms_primary_group_name%");
        if (group == null || group.isBlank() || group.equalsIgnoreCase("%luckperms_primary_group_name%")) {
            return null;
        }
        return group.toLowerCase(Locale.ROOT);
    }
}
