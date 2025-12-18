package artcreator.creator.impl;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PixelationEngine {
    public BufferedImage pixelate(BufferedImage original, int pixelSize) {
        if (original == null) throw new IllegalArgumentException("Image cannot be null");

        var width = original.getWidth();
        var height = original.getHeight();
        var result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (var y = 0; y < height; y += pixelSize)
            for (var x = 0; x < width; x += pixelSize) {
                var color = calculateAverageColor(original, x, y, pixelSize);
                fillBlock(result, x, y, pixelSize, color, width, height);
            }
        return result;
    }

    private Color calculateAverageColor(BufferedImage img, int startX, int startY, int size) {
        var totalR = 0L;
        var totalG = 0L;
        var totalB = 0L;
        var count = 0;
        var maxX = Math.min(startX + size, img.getWidth());
        var maxY = Math.min(startY + size, img.getHeight());

        for (var y = startY; y < maxY; y++)
            for (var x = startX; x < maxX; x++) {
                var c = new Color(img.getRGB(x, y));
                totalR += c.getRed();
                totalG += c.getGreen();
                totalB += c.getBlue();
                count++;
            }
        return new Color((int) (totalR / count), (int) (totalG / count), (int) (totalB / count));
    }

    private void fillBlock(BufferedImage img, int startX, int startY, int size, Color color, int maxW, int maxH) {
        var rgb = color.getRGB();
        for (var dy = 0; dy < size && (startY + dy) < maxH; dy++)
            for (var dx = 0; dx < size && (startX + dx) < maxW; dx++) img.setRGB(startX + dx, startY + dy, rgb);
    }
}
