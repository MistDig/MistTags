package com.mistdig.misttags;

public record AnimationSpec(String id, String text) {

    public static AnimationSpec fromInput(String input) {
        if (input == null || !input.toLowerCase().startsWith("anim:")) return null;
        String body = input.substring(5).trim();
        int colon = body.indexOf(':');
        int space = firstWhitespace(body);
        int splitAt;
        if (colon >= 0 && space >= 0) splitAt = Math.min(colon, space);
        else splitAt = Math.max(colon, space);

        if (splitAt < 0) return new AnimationSpec(body.toLowerCase(), "");
        String id = body.substring(0, splitAt).toLowerCase();
        String text = body.substring(splitAt + 1).trim();
        return new AnimationSpec(id, MiniMessageSanitizer.plainText(text));
    }

    public static AnimationSpec parse(String stored) {
        if (stored == null || !stored.toLowerCase().startsWith("anim:")) return null;
        String body = stored.substring(5);
        int colon = body.indexOf(':');
        if (colon < 0) return new AnimationSpec(body.toLowerCase(), "");
        return new AnimationSpec(body.substring(0, colon).toLowerCase(), body.substring(colon + 1));
    }

    public String store() {
        return text == null || text.isBlank() ? "anim:" + id : "anim:" + id + ":" + text;
    }

    private static int firstWhitespace(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) return i;
        }
        return -1;
    }
}
