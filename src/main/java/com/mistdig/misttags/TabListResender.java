package com.mistdig.misttags;

import java.util.UUID;

/** Re-sends TAB's current format without replacing its configuration or API overrides. */
final class TabListResender {
    private TabListResender() {}

    static boolean resend(Object api, UUID uuid, Class<?> playerType) throws ReflectiveOperationException {
        Object player = api.getClass().getMethod("getPlayer", UUID.class).invoke(api, uuid);
        if (player == null || !Boolean.TRUE.equals(playerType.getMethod("isLoaded").invoke(player))) return false;
        Object manager = api.getClass().getMethod("getTabListFormatManager").invoke(api);
        if (manager == null) return false;
        String prefix = (String) manager.getClass().getMethod("getOriginalRawPrefix", playerType).invoke(manager, player);
        String suffix = (String) manager.getClass().getMethod("getOriginalRawSuffix", playerType).invoke(manager, player);
        if (!usesMistTags(prefix) && !usesMistTags(suffix)) return false;

        // TAB's public setter also forces a display-name packet when the value is unchanged.
        // Round-trip the existing override (including null), preserving all configured fields.
        Object name = manager.getClass().getMethod("getCustomName", playerType).invoke(manager, player);
        manager.getClass().getMethod("setName", playerType, String.class).invoke(manager, player, name);
        return true;
    }

    private static boolean usesMistTags(String value) {
        return value != null && value.contains("%misttags_");
    }
}
