package artcreator.gui;

import javax.swing.*;
import java.awt.*;

public final class UIConfig {
    private UIConfig() {}

    public static final Font FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_LARGE = new Font("SansSerif", Font.PLAIN, 16);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 36);

    public static final Cursor HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    public static void init() {
        var defaults = UIManager.getDefaults();
        for (var key : defaults.keySet()) {
            var k = key.toString();
            if (k.endsWith(".font")) UIManager.put(key, FONT);
        }

        // Center alignment for components
        UIManager.put("Label.horizontalAlignment", SwingConstants.CENTER);
        UIManager.put("Button.horizontalAlignment", SwingConstants.CENTER);
        UIManager.put("TextField.horizontalAlignment", SwingConstants.CENTER);
        UIManager.put("TextArea.horizontalAlignment", SwingConstants.CENTER);
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
        if (!(component instanceof JLabel label)) return component;
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return component;
    }

    public static javax.swing.border.Border padding(int all) {
        return BorderFactory.createEmptyBorder(all, all, all, all);
    }

    public static javax.swing.border.Border padding(int vertical, int horizontal) {
        return BorderFactory.createEmptyBorder(vertical, horizontal, vertical, horizontal);
    }
}
