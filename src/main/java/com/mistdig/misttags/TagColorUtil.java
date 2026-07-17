package com.mistdig.misttags;

import org.bukkit.ChatColor;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TagColorUtil {

    private static final Pattern MINI_COLOR = Pattern.compile("<(/?)(#[0-9a-fA-F]{6}|[a-zA-Z_]+)(:[^>]*)?>");
    private static final Pattern LEGACY_COLOR = Pattern.compile("(?i)[&§]([0-9a-f])");
    private static final Map<Character, ChatColor> LEGACY = Map.ofEntries(
            Map.entry('0', ChatColor.BLACK), Map.entry('1', ChatColor.DARK_BLUE),
            Map.entry('2', ChatColor.DARK_GREEN), Map.entry('3', ChatColor.DARK_AQUA),
            Map.entry('4', ChatColor.DARK_RED), Map.entry('5', ChatColor.DARK_PURPLE),
            Map.entry('6', ChatColor.GOLD), Map.entry('7', ChatColor.GRAY),
            Map.entry('8', ChatColor.DARK_GRAY), Map.entry('9', ChatColor.BLUE),
            Map.entry('a', ChatColor.GREEN), Map.entry('b', ChatColor.AQUA),
            Map.entry('c', ChatColor.RED), Map.entry('d', ChatColor.LIGHT_PURPLE),
            Map.entry('e', ChatColor.YELLOW), Map.entry('f', ChatColor.WHITE)
    );
    private static final Map<String, ChatColor> NAMED = Map.ofEntries(
            Map.entry("black", ChatColor.BLACK), Map.entry("dark_blue", ChatColor.DARK_BLUE),
            Map.entry("dark_green", ChatColor.DARK_GREEN), Map.entry("dark_aqua", ChatColor.DARK_AQUA),
            Map.entry("dark_red", ChatColor.DARK_RED), Map.entry("dark_purple", ChatColor.DARK_PURPLE),
            Map.entry("gold", ChatColor.GOLD), Map.entry("gray", ChatColor.GRAY),
            Map.entry("grey", ChatColor.GRAY), Map.entry("dark_gray", ChatColor.DARK_GRAY),
            Map.entry("dark_grey", ChatColor.DARK_GRAY), Map.entry("blue", ChatColor.BLUE),
            Map.entry("green", ChatColor.GREEN), Map.entry("aqua", ChatColor.AQUA),
            Map.entry("red", ChatColor.RED), Map.entry("light_purple", ChatColor.LIGHT_PURPLE),
            Map.entry("yellow", ChatColor.YELLOW), Map.entry("white", ChatColor.WHITE)
    );

    private TagColorUtil() {}

    public static ChatColor deriveLastColor(String stored) {
        if (stored == null) return null;

        ChatColor color = null;
        Matcher legacyMatcher = LEGACY_COLOR.matcher(stored);
        while (legacyMatcher.find()) {
            color = LEGACY.get(Character.toLowerCase(legacyMatcher.group(1).charAt(0)));
        }

        Matcher miniMatcher = MINI_COLOR.matcher(stored);
        while (miniMatcher.find()) {
            if (!miniMatcher.group(1).isEmpty()) continue;
            String name = miniMatcher.group(2).toLowerCase(Locale.ROOT);
            if (!name.startsWith("#") && NAMED.containsKey(name)) color = NAMED.get(name);
        }

        return color;
    }

    public static String colorTagForMiniMessage(String stored) {
        ChatColor color = deriveLastColor(stored);
        return color == null ? "" : color.toString();
    }
}
