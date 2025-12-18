package artcreator.gui.components;

import artcreator.gui.UIConfig;

import javax.swing.*;
import java.awt.*;

public class ContentPage {
    private final JPanel panel;

    public ContentPage(String title, String subtitle, String content, JButton actionButton) {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(UIConfig.padding(50));

        var titleLabel = UIConfig.centered(new JLabel(title));
        titleLabel.setFont(UIConfig.FONT_TITLE);
        panel.add(titleLabel);

        if (subtitle != null) {
            panel.add(UIConfig.spacing(10));
            var subtitleLabel = UIConfig.centered(new JLabel(subtitle));
            subtitleLabel.setFont(UIConfig.FONT_LARGE);
            panel.add(subtitleLabel);
        }

        if (content != null) {
            panel.add(UIConfig.spacing(20));
            var html = "<html><center>" + content.replace("\n", "<br>") + "</center></html>";
            var contentLabel = UIConfig.centered(new JLabel(html));
            panel.add(contentLabel);
        }

        if (actionButton != null) {
            panel.add(UIConfig.spacing(30));
            panel.add(UIConfig.centered(actionButton));
        }
    }

    public static JButton createActionButton(String text, Runnable action) {
        var button = UIConfig.button(text, action, new Insets(10, 40, 10, 40));
        button.setFont(UIConfig.FONT_LARGE);
        return button;
    }

    public JPanel getPanel() { return panel; }
}
