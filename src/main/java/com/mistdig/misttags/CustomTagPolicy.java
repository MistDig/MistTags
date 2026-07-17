package com.mistdig.misttags;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Restrictions that apply ONLY to self-service (misttags.custom) prefix/suffix changes --
 * banned content, a length range, and a permission-driven duration cap. Staff acting through
 * any misttags.manage.* node bypass every check in this class entirely; see
 * TagCommand#handleAdd, which only consults this policy on the self-service path.
 *
 * All values are read from config.yml's "custom" section and can be changed without a
 * rebuild; call reload() after the underlying FileConfiguration has been reloaded.
 */
public class CustomTagPolicy {

    /** One rank/permission tier and the duration cap it unlocks. */
    public record DurationTier(String permission, Duration maxDuration) {}

    private final MistTags plugin;

    private int cooldownSeconds;
    private int minLength;
    private int maxLength;
    private final List<String> bannedWords = new ArrayList<>();
    private final List<Pattern> bannedPatterns = new ArrayList<>();
    private final List<DurationTier> durationTiers = new ArrayList<>();
    private Duration defaultMaxDuration = Duration.ofDays(30);

    public CustomTagPolicy(MistTags plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration cfg = plugin.getConfig();

        cooldownSeconds = Math.max(0, cfg.getInt("custom.cooldown-seconds", 300));
        minLength = Math.max(0, cfg.getInt("custom.min-length", 1));
        maxLength = Math.max(minLength, cfg.getInt("custom.max-length", 16));

        bannedWords.clear();
        for (String w : cfg.getStringList("custom.banned-words")) {
            if (w != null && !w.isBlank()) bannedWords.add(w.toLowerCase());
        }

        bannedPatterns.clear();
        for (String raw : cfg.getStringList("custom.banned-patterns")) {
            if (raw == null || raw.isBlank()) continue;
            try {
                bannedPatterns.add(Pattern.compile(raw, Pattern.CASE_INSENSITIVE));
            } catch (PatternSyntaxException e) {
                plugin.getLogger().warning("Skipping invalid custom.banned-patterns entry '"
                        + raw + "': " + e.getMessage());
            }
        }

        durationTiers.clear();
        if (cfg.isList("custom.duration-tiers")) {
            for (Object obj : cfg.getList("custom.duration-tiers")) {
                if (!(obj instanceof Map<?, ?> map)) continue;
                Object permObj = map.get("permission");
                Object durObj = map.get("max-duration");
                if (permObj == null || durObj == null) continue;
                Duration parsed = DurationParser.parse(String.valueOf(durObj));
                if (parsed == null) {
                    plugin.getLogger().warning("Skipping duration-tier with unparsable max-duration '" + durObj + "'");
                    continue;
                }
                durationTiers.add(new DurationTier(String.valueOf(permObj), parsed));
            }
        }

        Duration parsedDefault = DurationParser.parse(cfg.getString("custom.default-max-duration", "30d"));
        defaultMaxDuration = (parsedDefault != null && !parsedDefault.isZero()) ? parsedDefault : Duration.ofDays(30);
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    /**
     * The highest duration cap the player qualifies for. Checks every configured tier (not
     * just the first match) and returns the largest one whose permission the player holds,
     * so ranks can simply stack more duration-tier permissions as players rank up instead of
     * needing them granted in a specific order. Falls back to custom.default-max-duration
     * (30 days out of the box) if the player holds none of the tier permissions.
     */
    public Duration resolveMaxDuration(Player player) {
        Duration best = defaultMaxDuration;
        for (DurationTier tier : durationTiers) {
            if (player.hasPermission(tier.permission()) && tier.maxDuration().compareTo(best) > 0) {
                best = tier.maxDuration();
            }
        }
        return best;
    }

    /**
     * Checks tag content against the length range and banned word/pattern lists.
     * Length and word checks run against the plain-text rendering (formatting tags stripped)
     * so players can't dodge a length cap or a banned word by wrapping it in color tags.
     *
     * @return null if the content is acceptable, otherwise a player-facing rejection reason.
     */
    public String validate(String miniMessageTag) {
        String plain = MiniMessageSanitizer.plainText(miniMessageTag).trim();

        if (plain.isEmpty()) {
            return plugin.messages().raw("validation-empty", Map.of());
        }
        if (plain.length() < minLength) {
            return plugin.messages().raw("validation-too-short", Map.of(
                    "min", String.valueOf(minLength),
                    "plural", minLength == 1 ? "" : "s"
            ));
        }
        if (plain.length() > maxLength) {
            return plugin.messages().raw("validation-too-long", Map.of("max", String.valueOf(maxLength)));
        }

        String lower = plain.toLowerCase();
        for (String banned : bannedWords) {
            if (lower.contains(banned)) {
                return plugin.messages().raw("validation-banned-word", Map.of());
            }
        }
        for (Pattern pattern : bannedPatterns) {
            if (pattern.matcher(plain).find()) {
                return plugin.messages().raw("validation-banned-pattern", Map.of());
            }
        }

        return null;
    }
}
