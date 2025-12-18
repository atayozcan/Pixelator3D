package artcreator.gui.components;

import artcreator.gui.UIConfig;

import javax.swing.*;
import java.awt.*;

public final class TabBar {
    private final JPanel panel;
    private final ButtonGroup group = new ButtonGroup();
    private final CardLayout cardLayout;
    private final JPanel content;

    public TabBar(CardLayout cardLayout, JPanel content) {
        this.cardLayout = cardLayout;
        this.content = content;
        panel = new JPanel(new GridLayout(1, 0));
    }

    public void addTab(String name, String cardName) {
        var btn = new JToggleButton(name);
        btn.setFocusPainted(false);
        btn.setCursor(UIConfig.HAND);
        btn.addActionListener(_ -> cardLayout.show(content, cardName));
        group.add(btn);
        panel.add(btn);
        if (group.getButtonCount() == 1) btn.setSelected(true);
    }

    public void setSelected(int index) {
        var btn = (JToggleButton) panel.getComponent(index);
        btn.setSelected(true);
    }

    public JPanel getPanel() { return panel; }
}
