package com.mistdig.misttags;

import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class ClickableMessageUtil {

    private ClickableMessageUtil() {
    }

    public static void sendRunCommand(Player player, String text, String command, String hoverText) {
        if (!send(player, text, "RUN_COMMAND", command, hoverText)) player.sendMessage(text);
    }

    public static void sendSuggestCommand(Player player, String text, String command) {
        if (!send(player, text, "SUGGEST_COMMAND", command, null)) player.sendMessage(text + " " + command);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean send(Player player, String text, String clickAction, String command, String hoverText) {
        if (sendAdventure(player, text, clickAction, command, hoverText)) return true;
        try {
            Class<?> textComponentClass = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Class<?> baseComponentClass = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> clickEventClass = Class.forName("net.md_5.bungee.api.chat.ClickEvent");
            Class<?> clickActionClass = Class.forName("net.md_5.bungee.api.chat.ClickEvent$Action");

            Object component = textComponentClass.getConstructor(String.class).newInstance(text);
            Object action = Enum.valueOf((Class<Enum>) clickActionClass.asSubclass(Enum.class), clickAction);
            Object clickEvent = clickEventClass.getConstructor(clickActionClass, String.class).newInstance(action, command);
            textComponentClass.getMethod("setClickEvent", clickEventClass).invoke(component, clickEvent);

            if (hoverText != null && !hoverText.isBlank()) applyHover(textComponentClass, baseComponentClass, component, hoverText);

            Object spigot = player.getClass().getMethod("spigot").invoke(player);
            Object array = Array.newInstance(baseComponentClass, 1);
            Array.set(array, 0, component);
            Method send = spigot.getClass().getMethod("sendMessage", array.getClass());
            send.invoke(spigot, array);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean sendAdventure(Player player, String text, String clickAction, String command, String hoverText) {
        try {
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            Class<?> clickEventClass = Class.forName("net.kyori.adventure.text.event.ClickEvent");
            Class<?> hoverEventClass = Class.forName("net.kyori.adventure.text.event.HoverEvent");

            Object component = legacyComponent(text);
            String clickMethod = clickAction.equals("SUGGEST_COMMAND") ? "suggestCommand" : "runCommand";
            Object clickEvent = clickEventClass.getMethod(clickMethod, String.class).invoke(null, command);
            component = componentClass.getMethod("clickEvent", clickEventClass).invoke(component, clickEvent);

            if (hoverText != null && !hoverText.isBlank()) {
                try {
                    Object hoverComponent = legacyComponent(hoverText);
                    Object hoverEvent = hoverEventClass.getMethod("showText", componentClass).invoke(null, hoverComponent);
                    component = componentClass.getMethod("hoverEvent", Class.forName("net.kyori.adventure.text.event.HoverEventSource"))
                            .invoke(component, hoverEvent);
                } catch (ReflectiveOperationException ignored) {
                    // Click still matters more than hover. Older/relocated Adventure builds vary here.
                }
            }

            Class<?> audienceClass = Class.forName("net.kyori.adventure.audience.Audience");
            audienceClass.getMethod("sendMessage", componentClass).invoke(player, component);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    private static Object legacyComponent(String text) throws ReflectiveOperationException {
        Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
        try {
            Class<?> serializerClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            Object serializer = serializerClass.getMethod("legacySection").invoke(null);
            return serializer.getClass().getMethod("deserialize", String.class).invoke(serializer, text);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return componentClass.getMethod("text", String.class).invoke(null, text);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void applyHover(Class<?> textComponentClass, Class<?> baseComponentClass, Object component, String hoverText)
            throws ReflectiveOperationException {
        Class<?> hoverEventClass = Class.forName("net.md_5.bungee.api.chat.HoverEvent");
        Class<?> hoverActionClass = Class.forName("net.md_5.bungee.api.chat.HoverEvent$Action");
        Object hoverAction = Enum.valueOf((Class<Enum>) hoverActionClass.asSubclass(Enum.class), "SHOW_TEXT");

        try {
            Class<?> textContentClass = Class.forName("net.md_5.bungee.api.chat.hover.content.Text");
            Object content = textContentClass.getConstructor(String.class).newInstance(hoverText);
            Constructor<?> constructor = hoverEventClass.getConstructor(hoverActionClass, Class.forName("net.md_5.bungee.api.chat.hover.content.Content"));
            Object hoverEvent = constructor.newInstance(hoverAction, content);
            textComponentClass.getMethod("setHoverEvent", hoverEventClass).invoke(component, hoverEvent);
            return;
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            Object hoverComponent = textComponentClass.getConstructor(String.class).newInstance(hoverText);
            Object array = Array.newInstance(baseComponentClass, 1);
            Array.set(array, 0, hoverComponent);
            Object hoverEvent = hoverEventClass.getConstructor(hoverActionClass, array.getClass()).newInstance(hoverAction, array);
            textComponentClass.getMethod("setHoverEvent", hoverEventClass).invoke(component, hoverEvent);
        }
    }
}
