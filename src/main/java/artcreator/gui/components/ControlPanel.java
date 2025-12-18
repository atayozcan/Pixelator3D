package artcreator.gui.components;

import artcreator.domain.ArtworkConfig;
import artcreator.domain.OutputSize;
import artcreator.gui.UIConfig;

import javax.swing.*;
import java.awt.*;

public class ControlPanel {
    private final JPanel panel;
    private final JSlider pixelSizeSlider;
    private final JLabel pixelSizeLabel;
    private final JComboBox<Integer> colorCountCombo;
    private final ToggleSwitch modeToggle;
    private final JComboBox<OutputSize> outputSizeCombo;
    private final JButton applyButton;
    private final JButton pdfButton;

    public ControlPanel(Runnable onLoad, Runnable onApply, Runnable onGeneratePDF) {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIConfig.BG_PRIMARY);
        panel.setBorder(UIConfig.padding(5, 15));

        // Row 1: Load, Pixel Size, Colors
        var row1 = createRow();
        row1.add(UIConfig.button("Load Image", onLoad));
        row1.add(createLabel("Pixel Size:"));

        pixelSizeSlider = createSlider(2, 50, 10);
        row1.add(pixelSizeSlider);

        pixelSizeLabel = new JLabel("10");
        pixelSizeLabel.setFont(UIConfig.FONT_BOLD);
        pixelSizeLabel.setForeground(UIConfig.TEXT_PRIMARY);
        pixelSizeSlider.addChangeListener(_ -> pixelSizeLabel.setText(String.valueOf(pixelSizeSlider.getValue())));
        row1.add(pixelSizeLabel);

        row1.add(createLabel("Colors:"));
        colorCountCombo = new JComboBox<>(new Integer[]{8, 16, 32});
        colorCountCombo.setSelectedItem(16);
        row1.add(colorCountCombo);

        // Row 2: Mode, Output, Actions
        var row2 = createRow();

        modeToggle = new ToggleSwitch("2D", "3D");
        row2.add(modeToggle);

        row2.add(createLabel("Output:"));
        outputSizeCombo = new JComboBox<>(OutputSize.values());
        row2.add(outputSizeCombo);

        applyButton = UIConfig.button("Apply", onApply);
        applyButton.setFont(UIConfig.FONT_BOLD);
        applyButton.setEnabled(false);
        row2.add(applyButton);

        pdfButton = UIConfig.button("Generate PDF", onGeneratePDF);
        pdfButton.setEnabled(false);
        row2.add(pdfButton);

        panel.add(row1);
        panel.add(row2);
    }

    private JPanel createRow() {
        var row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        row.setOpaque(false);
        return row;
    }

    private JLabel createLabel(String text) {
        var label = new JLabel(text);
        label.setForeground(UIConfig.TEXT_SECONDARY);
        return label;
    }

    private JSlider createSlider(int min, int max, int value) {
        var slider = new JSlider(min, max, value);
        slider.setPaintTrack(true);
        slider.setPaintTicks(false);
        slider.setPaintLabels(false);
        slider.putClientProperty("Slider.paintValue", Boolean.FALSE);
        slider.setPreferredSize(new Dimension(120, 20));
        slider.setOpaque(false);
        return slider;
    }

    public JPanel getPanel() { return panel; }

    public void setButtonsEnabled(boolean enabled) {
        applyButton.setEnabled(enabled);
        pdfButton.setEnabled(enabled);
    }

    public void applyToConfig(ArtworkConfig config) {
        config.setPixelSize(pixelSizeSlider.getValue());
        config.setColorCount((Integer) colorCountCombo.getSelectedItem());
        config.setMode3D(modeToggle.isSelected());
        config.setOutputSize((OutputSize) outputSizeCombo.getSelectedItem());
    }
}
