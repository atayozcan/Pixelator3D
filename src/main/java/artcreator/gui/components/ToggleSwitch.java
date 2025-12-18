package artcreator.gui.components;

import artcreator.gui.UIConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ToggleSwitch extends JComponent {
    private boolean selected = false;
    private final String leftLabel;
    private final String rightLabel;
    private boolean hovered = false;

    public ToggleSwitch(String leftLabel, String rightLabel) {
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
        setPreferredSize(new Dimension(80, 32));
        setCursor(UIConfig.HAND);
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = !selected;
                repaint();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    protected void paintComponent(Graphics g) {
        var g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        var w = getWidth();
        var h = getHeight();
        var r = h;

        // Background track
        var trackColor = selected
            ? (hovered ? UIConfig.ACCENT_HOVER : UIConfig.ACCENT)
            : (hovered ? UIConfig.BG_DARK : UIConfig.BG_SECONDARY);
        g2.setColor(trackColor);
        g2.fillRoundRect(0, 0, w, h, r, r);

        // Border
        g2.setColor(UIConfig.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, r, r);

        // Knob
        var knobSize = h - 6;
        var knobX = selected ? w - knobSize - 3 : 3;
        g2.setColor(Color.WHITE);
        g2.fillOval(knobX, 3, knobSize, knobSize);

        // Knob shadow
        g2.setColor(new Color(0, 0, 0, 20));
        g2.drawOval(knobX, 3, knobSize, knobSize);

        // Labels
        g2.setFont(UIConfig.FONT_BOLD.deriveFont(11f));
        var fm = g2.getFontMetrics();
        var textY = (h + fm.getAscent() - fm.getDescent()) / 2;

        // Left label (visible when not selected)
        if (!selected) {
            g2.setColor(UIConfig.TEXT_PRIMARY);
            g2.drawString(leftLabel, knobSize + 10, textY);
        }

        // Right label (visible when selected)
        if (selected) {
            g2.setColor(Color.WHITE);
            g2.drawString(rightLabel, 10, textY);
        }

        g2.dispose();
    }
}
