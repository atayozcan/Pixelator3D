package artcreator.gui.components;

import artcreator.gui.UIConfig;

import javax.swing.*;
import java.awt.*;

public class NavigationBar {
    private final JPanel navBar;
    private final JButton[] tabs;
    private int selectedIndex = 0;

    public NavigationBar(Runnable onHome, Runnable onHelp, Runnable onAbout) {
        navBar = new JPanel(new GridLayout(1, 3, 0, 0));

        tabs = new JButton[] {
            createTab("Home", 0, onHome),
            createTab("Help", 1, onHelp),
            createTab("About", 2, onAbout)
        };

        for (var tab : tabs) navBar.add(tab);
        updateSelection();
    }

    private JButton createTab(String text, int index, Runnable action) {
        var btn = UIConfig.handCursor(new JButton(text));
        btn.setFocusPainted(false);
        btn.addActionListener(_ -> {
            selectedIndex = index;
            updateSelection();
            action.run();
        });
        return btn;
    }

    private void updateSelection() {
        var bg = navBar.getBackground();
        for (var i = 0; i < tabs.length; i++) {
            if (i == selectedIndex) {
                tabs[i].setBackground(bg);
                tabs[i].setContentAreaFilled(false);
                tabs[i].setOpaque(true);
                tabs[i].setBorderPainted(false);
            } else {
                tabs[i].setContentAreaFilled(true);
                tabs[i].setOpaque(true);
                tabs[i].setBorderPainted(true);
            }
        }
    }

    public void setSelected(int index) {
        selectedIndex = index;
        updateSelection();
    }

    public JPanel getPanel() { return navBar; }
}
