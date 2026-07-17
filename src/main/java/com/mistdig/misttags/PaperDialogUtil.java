package com.mistdig.misttags;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

public final class PaperDialogUtil {

    private PaperDialogUtil() {
    }

    public static boolean showManageDialog(MistTags plugin, Player player, PlayerTagData data) {
        return showDialog(plugin, player, () -> createDialog(buildManageBase(plugin, data), buildManageType(data)));
    }

    public static boolean showEditDialog(MistTags plugin, Player player, PlayerTagData data, String type) {
        return showDialog(plugin, player, () -> createDialog(buildEditBase(data, type), buildEditType(data, type)));
    }

    public static boolean showTimeDialog(MistTags plugin, Player player, PlayerTagData data, String type) {
        return showDialog(plugin, player, () -> createDialog(buildTimeBase(data, type), buildTimeType(data)));
    }

    private static boolean showDialog(MistTags plugin, Player player, DialogSupplier supplier) {
        try {
            Object dialog = supplier.get();
            Class<?> audienceClass = Class.forName("net.kyori.adventure.audience.Audience");
            Class<?> dialogLikeClass = Class.forName("net.kyori.adventure.dialog.DialogLike");
            audienceClass.getMethod("showDialog", dialogLikeClass).invoke(player, dialog);
            return true;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            plugin.getLogger().warning("Paper Dialog UI unavailable, falling back to inventory GUI: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    private static Object createDialog(Object base, Object type) throws ReflectiveOperationException {
        Class<?> dialogClass = Class.forName("io.papermc.paper.dialog.Dialog");
        return dialogClass.getMethod("create", Consumer.class).invoke(null, (Consumer<Object>) factory -> {
            try {
                Object entryBuilder = factory.getClass().getMethod("empty").invoke(factory);
                invoke(entryBuilder, "base", base);
                invoke(entryBuilder, "type", type);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static Object buildManageBase(MistTags plugin, PlayerTagData data) throws ReflectiveOperationException {
        List<Object> body = List.of(
                plainBody(mini("Prefix: " + renderedMini(plugin, data.getPrefix())), 300),
                plainBody(mini("Suffix: " + renderedMini(plugin, data.getSuffix())), 300)
        );
        Object builder = dialogBaseBuilder("MistTags: " + data.getName(), body, List.of());
        return builder.getClass().getMethod("build").invoke(builder);
    }

    private static Object buildManageType(PlayerTagData data) throws ReflectiveOperationException {
        Class<?> dialogTypeClass = Class.forName("io.papermc.paper.registry.data.dialog.type.DialogType");
        List<Object> actions = List.of(
                runButton("Delete Prefix", "Remove this prefix", "mt removeprefix " + data.getName()),
                runButton("Delete Suffix", "Remove this suffix", "mt removesuffix " + data.getName()),
                runButton("Edit Prefix", "Open prefix editor", "mt editprefix " + data.getName()),
                runButton("Edit Suffix", "Open suffix editor", "mt editsuffix " + data.getName()),
                runButton("Prefix Time", "Show prefix time left", "mt checktime " + data.getName() + " prefix"),
                runButton("Suffix Time", "Show suffix time left", "mt checktime " + data.getName() + " suffix")
        );
        return dialogTypeClass.getMethod("multiAction", List.class, actionButtonClass(), int.class)
                .invoke(null, actions, null, 2);
    }

    private static Object buildEditBase(PlayerTagData data, String type) throws ReflectiveOperationException {
        String stored = type.equals("prefix") ? data.getPrefix() : data.getSuffix();
        long expireAt = type.equals("prefix") ? data.getPrefixExpire() : data.getSuffixExpire();
        Object tagInput = textInput("tag", "New " + capitalize(type), raw(stored), 256);
        Object durationInput = textInput("duration", "Duration", defaultDuration(stored, expireAt), 32);
        List<Object> body = List.of(plainBody(text("Use 30m, 1h, 7d, 2w, or perm."), 300));
        Object builder = dialogBaseBuilder("Edit " + capitalize(type), body, List.of(tagInput, durationInput));
        return builder.getClass().getMethod("build").invoke(builder);
    }

    private static Object buildEditType(PlayerTagData data, String type) throws ReflectiveOperationException {
        Class<?> dialogTypeClass = Class.forName("io.papermc.paper.registry.data.dialog.type.DialogType");
        Object save = commandTemplateButton("Save", "Save this " + type,
                "mt set" + type + "raw " + data.getName() + " $(duration) $(tag)");
        Object cancel = runButton("Cancel", "Back to tag menu", "mt check " + data.getName());
        return dialogTypeClass.getMethod("confirmation", actionButtonClass(), actionButtonClass())
                .invoke(null, save, cancel);
    }

    private static Object buildTimeBase(PlayerTagData data, String type) throws ReflectiveOperationException {
        String stored = type.equals("prefix") ? data.getPrefix() : data.getSuffix();
        long expireAt = type.equals("prefix") ? data.getPrefixExpire() : data.getSuffixExpire();
        List<Object> body = List.of(
                plainBody(text(data.getName() + "'s " + type + " time left:"), 300),
                plainBody(text(timeLeft(stored, expireAt)), 300)
        );
        Object builder = dialogBaseBuilder(capitalize(type) + " Time", body, List.of());
        return builder.getClass().getMethod("build").invoke(builder);
    }

    private static Object buildTimeType(PlayerTagData data) throws ReflectiveOperationException {
        Class<?> dialogTypeClass = Class.forName("io.papermc.paper.registry.data.dialog.type.DialogType");
        Object back = runButton("Back", "Back to tag menu", "mt check " + data.getName());
        Object close = runButton("Close", "Close this dialog", "mt noop");
        return dialogTypeClass.getMethod("confirmation", actionButtonClass(), actionButtonClass())
                .invoke(null, back, close);
    }

    private static Object dialogBaseBuilder(String title, List<Object> body, List<Object> inputs) throws ReflectiveOperationException {
        Class<?> dialogBaseClass = Class.forName("io.papermc.paper.registry.data.dialog.DialogBase");
        Object builder = dialogBaseClass.getMethod("builder", componentClass()).invoke(null, text(title));
        invoke(builder, "body", body);
        invoke(builder, "inputs", inputs);
        return builder;
    }

    private static Object textInput(String key, String label, String initial, int maxLength) throws ReflectiveOperationException {
        Class<?> dialogInputClass = Class.forName("io.papermc.paper.registry.data.dialog.input.DialogInput");
        Object builder = dialogInputClass.getMethod("text", String.class, componentClass()).invoke(null, key, text(label));
        invoke(builder, "width", 300);
        invoke(builder, "initial", initial);
        invoke(builder, "maxLength", maxLength);
        return builder.getClass().getMethod("build").invoke(builder);
    }

    private static Object plainBody(Object component, int width) throws ReflectiveOperationException {
        Class<?> dialogBodyClass = Class.forName("io.papermc.paper.registry.data.dialog.body.DialogBody");
        return dialogBodyClass.getMethod("plainMessage", componentClass(), int.class).invoke(null, component, width);
    }

    private static Object noticeType() throws ReflectiveOperationException {
        Class<?> dialogTypeClass = Class.forName("io.papermc.paper.registry.data.dialog.type.DialogType");
        return dialogTypeClass.getMethod("notice").invoke(null);
    }

    private static Object runButton(String label, String tooltip, String command) throws ReflectiveOperationException {
        Class<?> clickEventClass = Class.forName("net.kyori.adventure.text.event.ClickEvent");
        Object clickEvent = clickEventClass.getMethod("runCommand", String.class).invoke(null, "/" + command);
        return button(label, tooltip, staticAction(clickEvent));
    }

    private static Object commandTemplateButton(String label, String tooltip, String command) throws ReflectiveOperationException {
        Class<?> dialogActionClass = Class.forName("io.papermc.paper.registry.data.dialog.action.DialogAction");
        Object action = dialogActionClass.getMethod("commandTemplate", String.class).invoke(null, command);
        return button(label, tooltip, action);
    }

    private static Object staticAction(Object clickEvent) throws ReflectiveOperationException {
        Class<?> dialogActionClass = Class.forName("io.papermc.paper.registry.data.dialog.action.DialogAction");
        Class<?> clickEventClass = Class.forName("net.kyori.adventure.text.event.ClickEvent");
        return dialogActionClass.getMethod("staticAction", clickEventClass).invoke(null, clickEvent);
    }

    private static Object button(String label, String tooltip, Object action) throws ReflectiveOperationException {
        Object builder = actionButtonClass().getMethod("builder", componentClass()).invoke(null, text(label));
        invoke(builder, "tooltip", text(tooltip));
        invoke(builder, "width", 150);
        invoke(builder, "action", action);
        return builder.getClass().getMethod("build").invoke(builder);
    }

    private static Class<?> componentClass() throws ClassNotFoundException {
        return Class.forName("net.kyori.adventure.text.Component");
    }

    private static Class<?> actionButtonClass() throws ClassNotFoundException {
        return Class.forName("io.papermc.paper.registry.data.dialog.ActionButton");
    }

    private static Object text(String value) throws ReflectiveOperationException {
        return componentClass().getMethod("text", String.class).invoke(null, value);
    }

    private static Object mini(String value) throws ReflectiveOperationException {
        try {
            Class<?> miniClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            Object mini = miniClass.getMethod("miniMessage").invoke(null);
            return mini.getClass().getMethod("deserialize", String.class).invoke(mini, value);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return legacy(MiniMessageSanitizer.toLegacy(value));
        }
    }

    private static Object legacy(String value) throws ReflectiveOperationException {
        try {
            Class<?> serializerClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            Object serializer = serializerClass.getMethod("legacySection").invoke(null);
            return serializer.getClass().getMethod("deserialize", String.class).invoke(serializer, value);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return text(MiniMessageSanitizer.plainText(value));
        }
    }

    private static void invoke(Object target, String method, Object arg) throws ReflectiveOperationException {
        for (Method candidate : target.getClass().getMethods()) {
            if (!candidate.getName().equals(method) || candidate.getParameterCount() != 1) continue;
            candidate.invoke(target, arg);
            return;
        }
        throw new NoSuchMethodException(target.getClass().getName() + "." + method);
    }

    private static String renderedMini(MistTags plugin, String stored) {
        return stored == null ? "<gray>(none)</gray>" : plugin.renderStoredRaw(stored);
    }

    private static String raw(String stored) {
        return stored == null ? "" : stored;
    }

    private static String timeLeft(String stored, long expireAt) {
        if (stored == null) return "no active tag";
        if (expireAt <= 0) return "permanent";
        long millis = expireAt - System.currentTimeMillis();
        if (millis <= 0) return "expired";
        return DurationParser.describe(Duration.ofMillis(millis));
    }

    private static String defaultDuration(String stored, long expireAt) {
        if (stored != null && expireAt > 0) {
            long millis = expireAt - System.currentTimeMillis();
            if (millis > 0) return DurationParser.describe(Duration.ofMillis(millis)).replace(" ", "");
        }
        return "30m";
    }

    private static String capitalize(String value) {
        return value == null || value.isEmpty() ? "" : value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private interface DialogSupplier {
        Object get() throws ReflectiveOperationException;
    }
}
