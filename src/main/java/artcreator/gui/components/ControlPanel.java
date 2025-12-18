package artcreator.gui.components;

import artcreator.gui.UIConfig;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

public class ControlPanel {
    private final JPanel panel;
    private final JSlider pixelSizeSlider;
    private final JButton pixelateButton;

    public ControlPanel(Runnable onLoad, Runnable onPixelate) {
        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBorder(UIConfig.padding(5, 10));

        pixelSizeSlider = createSlider();

        pixelateButton = UIConfig.handCursor(new JButton("Pixelate"));
        pixelateButton.setFont(UIConfig.FONT_BOLD);
        pixelateButton.setFocusPainted(false);
        pixelateButton.setEnabled(false);
        pixelateButton.addActionListener(_ -> onPixelate.run());

        panel.add(UIConfig.button("Load Image", onLoad));
        panel.add(new JLabel("Pixel Size:"));
        panel.add(pixelSizeSlider);
        panel.add(pixelateButton);
    }

    private JSlider createSlider() {
        var slider = new JSlider(JSlider.HORIZONTAL, 2, 50, 10);
        slider.setPreferredSize(new Dimension(300, 70));
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(2);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        var labels = new Hashtable<Integer, JLabel>();
        for (var i : new int[]{2, 10, 20, 30, 40, 50}) labels.put(i, new JLabel(String.valueOf(i)));
        slider.setLabelTable(labels);
        return slider;
    }

    public JPanel getPanel() {
        return panel;
    }

    public int getPixelSize() {
        return pixelSizeSlider.getValue();
    }

    public void setPixelateButtonEnabled(boolean enabled) {
        pixelateButton.setEnabled(enabled);
    }
}
