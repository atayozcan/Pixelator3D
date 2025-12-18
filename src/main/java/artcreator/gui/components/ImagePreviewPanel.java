package artcreator.gui.components;

import artcreator.gui.UIConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

public class ImagePreviewPanel {
    private final JPanel container;
    private final JLabel imageLabel;
    private BufferedImage currentImage;

    public ImagePreviewPanel() {
        container = new JPanel(new GridBagLayout());

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        var placeholder = new JLabel("Load an image to get started");
        placeholder.setFont(UIConfig.FONT_LARGE);
        container.add(placeholder);

        container.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (currentImage != null) rescale();
            }
        });
    }

    public JPanel getPanel() { return container; }

    public void displayImage(BufferedImage image) {
        if (image == null) return;
        currentImage = image;
        rescale();
    }

    private void rescale() {
        var w = container.getWidth() - 20;
        var h = container.getHeight() - 20;
        if (w <= 0 || h <= 0) return;

        var scaled = ImageUtils.scaleImage(currentImage, w, h);
        imageLabel.setIcon(new ImageIcon(scaled));

        container.removeAll();
        container.add(imageLabel);
        container.revalidate();
        container.repaint();
    }
}
