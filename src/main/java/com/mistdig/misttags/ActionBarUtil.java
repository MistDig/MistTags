package com.mistdig.misttags;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class ActionBarUtil {

    private ActionBarUtil() {}

    public static void send(Player player, String message) {
        try {
            Object component = MiniMessageSanitizer.toComponent(message);
            Class.forName("net.kyori.adventure.audience.Audience")
                    .getMethod("sendActionBar", Class.forName("net.kyori.adventure.text.Component"))
                    .invoke(player, component);
            return;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            // Retain the Bukkit fallback when Adventure is unavailable.
        }
        String legacy = MiniMessageSanitizer.toLegacy(message);
        if (tryStringActionBar(player, legacy)) return;
        if (trySpigotActionBar(player, legacy)) return;
        player.sendMessage(legacy);
    }

    private static boolean tryStringActionBar(Player player, String message) {
        try {
            Method method = player.getClass().getMethod("sendActionBar", String.class);
            method.invoke(player, message);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static boolean trySpigotActionBar(Player player, String message) {
        try {
            Object spigot = player.getClass().getMethod("spigot").invoke(player);
            Class<?> chatMessageType = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Object actionBar = Enum.valueOf((Class<Enum>) chatMessageType.asSubclass(Enum.class), "ACTION_BAR");
            Class<?> baseComponent = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> textComponent = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Object components = textComponent.getMethod("fromLegacyText", String.class).invoke(null, message);
            Method send = spigot.getClass().getMethod("sendMessage", chatMessageType, components.getClass());
            send.invoke(spigot, actionBar, components);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }
}
