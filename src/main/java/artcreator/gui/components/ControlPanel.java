package artcreator.gui.components;

import artcreator.domain.ArtworkConfig;
import artcreator.domain.OutputSize;
import artcreator.gui.UIConfig;

import javax.swing.*;
import java.awt.*;

public class ControlPanel {
    private final JPanel panel;
    private final JSlider pixelSizeSlider;
    private final JComboBox<Integer> colorCountCombo;
    private final JCheckBox mode3DCheck;
    private final JComboBox<OutputSize> outputSizeCombo;
    private final JButton applyButton;
    private final JButton pdfButton;

    public ControlPanel(Runnable onLoad, Runnable onApply, Runnable onGeneratePDF) {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // Row 1: Load + Pixel Size Slider
        var row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        row1.add(UIConfig.button("Load Image", onLoad));

        row1.add(new JLabel("Pixel Size:"));
        pixelSizeSlider = new JSlider(2, 50, 10);
        pixelSizeSlider.setPaintTrack(true);
        pixelSizeSlider.setPaintTicks(false);
        pixelSizeSlider.setPaintLabels(false);
        pixelSizeSlider.putClientProperty("Slider.paintValue", Boolean.FALSE);
        pixelSizeSlider.setPreferredSize(new Dimension(120, 20));
        row1.add(pixelSizeSlider);
        var pixelSizeLabel = new JLabel("10");
        pixelSizeLabel.setFont(UIConfig.FONT_BOLD);
        pixelSizeSlider.addChangeListener(_ -> pixelSizeLabel.setText(String.valueOf(pixelSizeSlider.getValue())));
        row1.add(pixelSizeLabel);

        row1.add(new JLabel("Colors:"));
        colorCountCombo = new JComboBox<>(new Integer[]{8, 16, 32});
        colorCountCombo.setSelectedItem(16);
        row1.add(colorCountCombo);

        // Row 2: 3D, Output Size, Buttons
        var row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));

        var modeGroup = new ButtonGroup();
        var mode2DBtn = new JToggleButton("2D", true);
        var mode3DBtn = new JToggleButton("3D", false);
        modeGroup.add(mode2DBtn);
        modeGroup.add(mode3DBtn);
        mode3DCheck = new JCheckBox();
        mode3DCheck.setVisible(false);
        mode2DBtn.addActionListener(_ -> mode3DCheck.setSelected(false));
        mode3DBtn.addActionListener(_ -> mode3DCheck.setSelected(true));
        row2.add(mode2DBtn);
        row2.add(mode3DBtn);

        row2.add(new JLabel("Output:"));
        outputSizeCombo = new JComboBox<>(OutputSize.values());
        row2.add(outputSizeCombo);

        applyButton = UIConfig.handCursor(new JButton("Apply"));
        applyButton.setFont(UIConfig.FONT_BOLD);
        applyButton.setEnabled(false);
        applyButton.addActionListener(_ -> onApply.run());
        row2.add(applyButton);

        pdfButton = UIConfig.handCursor(new JButton("Generate PDF"));
        pdfButton.setEnabled(false);
        pdfButton.addActionListener(_ -> onGeneratePDF.run());
        row2.add(pdfButton);

        panel.add(row1);
        panel.add(row2);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setButtonsEnabled(boolean enabled) {
        applyButton.setEnabled(enabled);
        pdfButton.setEnabled(enabled);
    }

    public void applyToConfig(ArtworkConfig config) {
        config.setPixelSize(pixelSizeSlider.getValue());
        config.setColorCount((Integer) colorCountCombo.getSelectedItem());
        config.setMode3D(mode3DCheck.isSelected());
        config.setOutputSize((OutputSize) outputSizeCombo.getSelectedItem());
    }
}
