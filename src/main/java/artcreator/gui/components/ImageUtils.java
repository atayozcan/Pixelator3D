package artcreator.gui.components;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class ImageUtils {
    private ImageUtils() {}

    public static BufferedImage scaleImage(BufferedImage img, int maxWidth, int maxHeight) {
        var width = img.getWidth();
        var height = img.getHeight();
        var scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
        if (scale >= 1) return img;

        var newWidth = (int) (width * scale);
        var newHeight = (int) (height * scale);
        var scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        var g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaled;
    }
}
