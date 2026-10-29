import com.mistdig.misttags.MiniMessageSanitizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

/** Run with the compiled plugin and Adventure API/MiniMessage jars on the classpath. */
public final class RenderingSmokeTest {
    private record Glyph(char value, TextColor color, boolean bold) {}

    public static void main(String[] args) throws Exception {
        List<Glyph> scoped = render("<red>A</red>B");
        require(scoped.size() == 2 && scoped.get(0).color() != null && scoped.get(1).color() == null,
                "closing color scope must restore the parent style");
        List<Glyph> gradient = render("<gradient:#ff0000:#0000ff>VIP</gradient>");
        require(gradient.size() == 3 && gradient.get(0).color().value() == 0xff0000
                && gradient.get(2).color().value() == 0x0000ff, "gradient must retain its endpoint colors");
        require(render("<color:#12ab34>VIP</color>").get(0).color().value() == 0x12ab34,
                "color alias must survive sanitization");
        List<Glyph> legacy = render("&lA&cB");
        require(legacy.get(0).bold() && !legacy.get(1).bold(), "legacy color must clear bold");
        require(!MiniMessageSanitizer.toSafeString("<click:run_command:'/op x'>VIP</click>").contains("click"),
                "interactive tags must be removed");
        require(MiniMessageSanitizer.toLegacy("<gradient:#ff0000:#0000ff>VIP</gradient>").contains("\u00a7x"),
                "legacy output must preserve RGB formatting on Adventure runtimes");
        System.out.println("Rendering smoke tests passed");
    }

    private static List<Glyph> render(String value) throws Exception {
        List<Glyph> result = new ArrayList<>();
        collect((Component) MiniMessageSanitizer.toComponent(value), null, false, result);
        return result;
    }

    private static void collect(Component component, TextColor inheritedColor, boolean inheritedBold,
                                List<Glyph> result) {
        TextColor color = component.color() == null ? inheritedColor : component.color();
        TextDecoration.State state = component.decoration(TextDecoration.BOLD);
        boolean bold = state == TextDecoration.State.NOT_SET ? inheritedBold : state == TextDecoration.State.TRUE;
        if (component instanceof TextComponent text) {
            for (char c : text.content().toCharArray()) result.add(new Glyph(c, color, bold));
        }
        for (Component child : component.children()) collect(child, color, bold, result);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
