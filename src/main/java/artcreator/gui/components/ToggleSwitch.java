package artcreator.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ToggleSwitch extends JComponent {
    private boolean selected = false;
    private final String leftLabel;
    private final String rightLabel;

    public ToggleSwitch(String leftLabel, String rightLabel) {
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
        setPreferredSize(new Dimension(70, 28));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selected = !selected;
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
        var radius = h;

        // Background track
        g2.setColor(selected ? new Color(180, 140, 160) : new Color(200, 200, 200));
        g2.fillRoundRect(0, 0, w, h, radius, radius);

        // Knob
        var knobSize = h - 4;
        var knobX = selected ? w - knobSize - 2 : 2;
        g2.setColor(Color.WHITE);
        g2.fillOval(knobX, 2, knobSize, knobSize);

        // Labels
        g2.setFont(getFont().deriveFont(Font.BOLD, 10f));
        var fm = g2.getFontMetrics();

        g2.setColor(selected ? new Color(255, 255, 255, 150) : new Color(80, 80, 80));
        g2.drawString(leftLabel, 8, (h + fm.getAscent() - fm.getDescent()) / 2);

        g2.setColor(selected ? new Color(80, 80, 80) : new Color(255, 255, 255, 150));
        g2.drawString(rightLabel, w - fm.stringWidth(rightLabel) - 8, (h + fm.getAscent() - fm.getDescent()) / 2);

        g2.dispose();
    }
}
