package com.mistdig.misttags;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses duration strings. Supports single units ("30m", "12h", "7d") as well as combined
 * ones ("1d12h", "2w3d4h30m"), unlike the original single-unit-only regex.
 */
public final class DurationParser {

    private static final Pattern SEGMENT = Pattern.compile("(\\d+)([smhdw])");

    private DurationParser() {}

    public static Duration parse(String input) {
        if (input == null || input.isEmpty()) return null;
        String normalized = input.toLowerCase();
        Matcher matcher = SEGMENT.matcher(normalized);

        long totalSeconds = 0;
        boolean matchedAny = false;
        int lastEnd = 0;

        while (matcher.find()) {
            // Reject stray characters between segments (e.g. "1d x2h") instead of silently
            // ignoring them, so staff get a clear error rather than a wrong duration.
            if (matcher.start() != lastEnd) return null;
            matchedAny = true;

            long value = Long.parseLong(matcher.group(1));
            switch (matcher.group(2)) {
                case "s" -> totalSeconds += value;
                case "m" -> totalSeconds += value * 60;
                case "h" -> totalSeconds += value * 3600;
                case "d" -> totalSeconds += value * 86400;
                case "w" -> totalSeconds += value * 604800;
            }
            lastEnd = matcher.end();
        }

        if (!matchedAny || lastEnd != normalized.length()) return null;
        return Duration.ofSeconds(totalSeconds);
    }

    public static String describe(Duration d) {
        long totalSeconds = d.getSeconds();
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0 || sb.isEmpty()) sb.append(minutes).append("m");
        return sb.toString().trim();
    }
}
