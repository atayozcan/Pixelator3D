package artcreator.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public final class UIConfig {
    // Libadwaita-inspired colors
    public static final Color BG_PRIMARY = new Color(250, 250, 250);
    public static final Color BG_SECONDARY = new Color(242, 242, 242);
    public static final Color BG_DARK = new Color(222, 221, 227);
    public static final Color ACCENT = new Color(181, 131, 141);
    public static final Color ACCENT_HOVER = new Color(161, 111, 121);
    public static final Color TEXT_PRIMARY = new Color(46, 46, 46);
    public static final Color TEXT_SECONDARY = new Color(119, 118, 123);
    public static final Color BORDER = new Color(213, 213, 213);

    // Fonts
    public static final Font FONT = new Font("Cantarell", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("Cantarell", Font.BOLD, 14);
    public static final Font FONT_LARGE = new Font("Cantarell", Font.PLAIN, 16);
    public static final Font FONT_TITLE = new Font("Cantarell", Font.BOLD, 32);

    public static final Cursor HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    public static final int RADIUS = 8;

    private UIConfig() {}

    public static void init() {
        var defaults = UIManager.getDefaults();
        for (var key : defaults.keySet()) {
            var k = key.toString();
            if (k.endsWith(".font")) UIManager.put(key, FONT);
        }
        UIManager.put("Panel.background", BG_PRIMARY);
        UIManager.put("Button.arc", RADIUS * 2);
    }

    public static <T extends AbstractButton> T handCursor(T button) {
        button.setCursor(HAND);
        return button;
    }

    public static JButton button(String text, Runnable action) {
        var btn = handCursor(new JButton(text));
        btn.addActionListener(_ -> action.run());
        return btn;
    }

    public static JButton button(String text, Runnable action, Insets margin) {
        var btn = button(text, action);
        btn.setMargin(margin);
        return btn;
    }

    public static Component spacing(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }

    public static <T extends JComponent> T centered(T component) {
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (component instanceof JLabel label) {
            label.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return component;
    }

    public static Border padding(int all) {
        return BorderFactory.createEmptyBorder(all, all, all, all);
    }

    public static Border padding(int vertical, int horizontal) {
        return BorderFactory.createEmptyBorder(vertical, horizontal, vertical, horizontal);
    }

    public static Border padding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    // Draw rounded rectangle helper
    public static void drawRoundedRect(Graphics2D g, int x, int y, int w, int h, int r, Color fill, Color border) {
        if (fill != null) {
            g.setColor(fill);
            g.fillRoundRect(x, y, w, h, r, r);
        }
        if (border != null) {
            g.setColor(border);
            g.drawRoundRect(x, y, w, h, r, r);
        }
    }
}
