package artcreator.gui.components;

import artcreator.gui.UIConfig;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class TabBar extends JComponent {
    private final CardLayout cardLayout;
    private final JPanel content;
    private final List<Tab> tabs = new ArrayList<>();
    private int selectedIndex = 0;
    private int hoveredIndex = -1;

    public TabBar(CardLayout cardLayout, JPanel content) {
        this.cardLayout = cardLayout;
        this.content = content;
        setPreferredSize(new Dimension(0, 44));
        setOpaque(false);
        setCursor(UIConfig.HAND);

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                var idx = getTabAt(e.getX());
                if (idx >= 0 && idx < tabs.size()) {
                    setSelected(idx);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                hoveredIndex = -1;
                repaint();
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                var idx = getTabAt(e.getX());
                if (idx != hoveredIndex) {
                    hoveredIndex = idx;
                    repaint();
                }
            }
        });
    }

    public void addTab(String name, String cardName) {
        tabs.add(new Tab(name, cardName));
        repaint();
    }

    public void setSelected(int index) {
        if (index >= 0 && index < tabs.size()) {
            selectedIndex = index;
            cardLayout.show(content, tabs.get(index).cardName);
            repaint();
        }
    }

    private int getTabAt(int x) {
        if (tabs.isEmpty()) return -1;
        var tabWidth = getWidth() / tabs.size();
        return Math.min(x / tabWidth, tabs.size() - 1);
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        var w = getWidth();
        var h = getHeight();

        // Background
        g2.setColor(UIConfig.BG_DARK);
        g2.fillRect(0, 0, w, h);

        if (tabs.isEmpty()) {
            g2.dispose();
            return;
        }

        var tabWidth = w / tabs.size();
        g2.setFont(UIConfig.FONT_BOLD);
        var fm = g2.getFontMetrics();

        for (var i = 0; i < tabs.size(); i++) {
            var tab = tabs.get(i);
            var x = i * tabWidth;
            var isSelected = i == selectedIndex;
            var isHovered = i == hoveredIndex && !isSelected;

            // Selected tab background blends with content
            if (isSelected) {
                g2.setColor(UIConfig.BG_PRIMARY);
                g2.fillRoundRect(x + 4, 4, tabWidth - 8, h - 4, 12, 12);
            } else if (isHovered) {
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillRoundRect(x + 4, 4, tabWidth - 8, h - 8, 10, 10);
            }

            // Text
            g2.setColor(isSelected ? UIConfig.TEXT_PRIMARY : UIConfig.TEXT_SECONDARY);
            var textX = x + (tabWidth - fm.stringWidth(tab.name)) / 2;
            var textY = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(tab.name, textX, textY);
        }

        g2.dispose();
    }

    public JPanel getPanel() {
        var panel = new JPanel(new BorderLayout());
        panel.add(this, BorderLayout.CENTER);
        return panel;
    }

    private record Tab(String name, String cardName) {}
}
