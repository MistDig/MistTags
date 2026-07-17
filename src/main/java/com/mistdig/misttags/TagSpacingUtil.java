package com.mistdig.misttags;

public final class TagSpacingUtil {

    private TagSpacingUtil() {
    }

    public static String prefix(String value) {
        if (value == null || value.isEmpty()) return "";
        return value.endsWith(" ") ? value : value + " ";
    }

    public static String suffix(String value) {
        if (value == null || value.isEmpty()) return "";
        return value.startsWith(" ") ? value : " " + value;
    }
}
