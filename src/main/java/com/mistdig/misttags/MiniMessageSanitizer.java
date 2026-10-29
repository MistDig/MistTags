package com.mistdig.misttags;

import org.bukkit.ChatColor;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bukkit-compatible cosmetic tag sanitizer. It keeps simple MiniMessage color/decoration
 * tags and strips interactive tags such as click/hover/run_command. Standalone Bukkit
 * rendering converts the safe cosmetic tags to legacy section colors.
 */
public final class MiniMessageSanitizer {

    private static final Pattern TAG = Pattern.compile("<(/?)([a-zA-Z_#][a-zA-Z0-9_#-]*)(:[^>]*)?>");
    private static final Pattern LEGACY = Pattern.compile("(?i)[&\\u00A7][0-9a-fk-or]");
    private static final Set<String> ALLOWED = Set.of(
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "grey", "dark_gray", "dark_grey", "blue", "green", "aqua",
            "red", "light_purple", "yellow", "white", "bold", "b", "italic", "i",
            "underlined", "u", "strikethrough", "st", "obfuscated",
            "obf", "reset", "gradient", "rainbow", "color", "colour", "c"
    );
    private static final Map<String, String> LEGACY_CODES = Map.ofEntries(
            Map.entry("black", "0"), Map.entry("dark_blue", "1"), Map.entry("dark_green", "2"),
            Map.entry("dark_aqua", "3"), Map.entry("dark_red", "4"), Map.entry("dark_purple", "5"),
            Map.entry("gold", "6"), Map.entry("gray", "7"), Map.entry("grey", "7"),
            Map.entry("dark_gray", "8"), Map.entry("dark_grey", "8"), Map.entry("blue", "9"),
            Map.entry("green", "a"), Map.entry("aqua", "b"), Map.entry("red", "c"),
            Map.entry("light_purple", "d"), Map.entry("yellow", "e"), Map.entry("white", "f"),
            Map.entry("bold", "l"), Map.entry("b", "l"), Map.entry("italic", "o"),
            Map.entry("i", "o"), Map.entry("underlined", "n"), Map.entry("u", "n"),
            Map.entry("strikethrough", "m"), Map.entry("st", "m"), Map.entry("obfuscated", "k"),
            Map.entry("obf", "k"), Map.entry("reset", "r")
    );

    private MiniMessageSanitizer() {}

    public static String toSafeString(String raw) {
        if (raw == null) return "";
        Matcher matcher = TAG.matcher(raw);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(2).toLowerCase(Locale.ROOT);
            boolean hex = name.matches("#[0-9a-f]{6}");
            boolean allowed = hex || ALLOWED.contains(name);
            matcher.appendReplacement(out, allowed ? Matcher.quoteReplacement(matcher.group()) : "");
        }
        matcher.appendTail(out);
        return out.toString();
    }

    @SuppressWarnings("deprecation")
    public static String toLegacy(String raw) {
        try {
            Object component = toComponent(raw);
            Class<?> serializerClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            Object builder = serializerClass.getMethod("builder").invoke(null);
            Class<?> builderClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder");
            builderClass.getMethod("hexColors").invoke(builder);
            builderClass.getMethod("useUnusualXRepeatedCharacterHexFormat").invoke(builder);
            Object serializer = builderClass.getMethod("build").invoke(builder);
            return (String) serializerClass.getMethod("serialize", Class.forName("net.kyori.adventure.text.Component"))
                    .invoke(serializer, component);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            // Older Bukkit runtimes may not provide Adventure.
        }
        String safe = ChatColor.translateAlternateColorCodes('&', toSafeString(raw));
        Matcher matcher = TAG.matcher(safe);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String close = matcher.group(1);
            String name = matcher.group(2).toLowerCase(Locale.ROOT);
            String replacement = "";
            if (close.isEmpty()) {
                if (name.matches("#[0-9a-f]{6}")) {
                    replacement = hexToLegacy(name);
                } else if (LEGACY_CODES.containsKey(name)) {
                    replacement = ChatColor.COLOR_CHAR + LEGACY_CODES.get(name);
                }
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    public static String plainText(String raw) {
        String noTags = TAG.matcher(raw == null ? "" : raw).replaceAll("");
        return LEGACY.matcher(noTags).replaceAll("");
    }

    public static Object toComponent(String raw) throws ReflectiveOperationException {
        String safe = toSafeString(raw);
        Matcher codes = LEGACY.matcher(safe);
        StringBuffer converted = new StringBuffer();
        while (codes.find()) {
            String code = codes.group().substring(1).toLowerCase(Locale.ROOT);
            String name = LEGACY_CODES.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(code))
                    .map(Map.Entry::getKey).findFirst().orElse("reset");
            String reset = code.matches("[0-9a-f]") ? "<reset>" : "";
            codes.appendReplacement(converted, Matcher.quoteReplacement(reset + "<" + name + ">"));
        }
        codes.appendTail(converted);
        Class<?> miniClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
        Object mini = miniClass.getMethod("miniMessage").invoke(null);
        Class<?> resolverClass = Class.forName("net.kyori.adventure.text.minimessage.tag.resolver.TagResolver");
        Object resolver = resolverClass.getMethod("empty").invoke(null);
        return miniClass.getMethod("deserialize", String.class, resolverClass)
                .invoke(mini, converted.toString(), resolver);
    }

    private static String hexToLegacy(String hex) {
        String digits = hex.substring(1);
        StringBuilder out = new StringBuilder("\u00A7x");
        for (char c : digits.toCharArray()) {
            out.append('\u00A7').append(c);
        }
        return out.toString();
    }
}
