package artcreator.creator.impl;

import artcreator.domain.ArtworkConfig;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class PixelationEngine {

    public BufferedImage pixelate(BufferedImage original, int pixelSize) {
        return pixelateSimple(original, pixelSize);
    }

    public BufferedImage pixelate(BufferedImage original, ArtworkConfig config) {
        var pixelSize = config.getPixelSize();
        var colorCount = config.getColorCount();

        var pixelated = pixelateSimple(original, pixelSize);
        return ColorQuantizer.quantize(pixelated, colorCount);
    }

    public List<Color> getPalette(BufferedImage image, int colorCount) {
        return ColorQuantizer.getPalette(image, colorCount);
    }

    private BufferedImage pixelateSimple(BufferedImage original, int pixelSize) {
        var width = original.getWidth();
        var height = original.getHeight();
        var result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (var y = 0; y < height; y += pixelSize) {
            for (var x = 0; x < width; x += pixelSize) {
                var color = calculateAverageColor(original, x, y, pixelSize, pixelSize);
                fillBlock(result, x, y, pixelSize, pixelSize, color, width, height);
            }
        }
        return result;
    }

    private Color calculateAverageColor(BufferedImage img, int startX, int startY, int sizeW, int sizeH) {
        var totalR = 0L;
        var totalG = 0L;
        var totalB = 0L;
        var count = 0;
        var maxX = Math.min(startX + sizeW, img.getWidth());
        var maxY = Math.min(startY + sizeH, img.getHeight());

        for (var y = startY; y < maxY; y++) {
            for (var x = startX; x < maxX; x++) {
                var c = new Color(img.getRGB(x, y));
                totalR += c.getRed();
                totalG += c.getGreen();
                totalB += c.getBlue();
                count++;
            }
        }
        if (count == 0) return Color.BLACK;
        return new Color((int)(totalR/count), (int)(totalG/count), (int)(totalB/count));
    }

    private void fillBlock(BufferedImage img, int startX, int startY, int sizeW, int sizeH, Color color, int maxW, int maxH) {
        var rgb = color.getRGB();
        for (var dy = 0; dy < sizeH && (startY + dy) < maxH; dy++) {
            for (var dx = 0; dx < sizeW && (startX + dx) < maxW; dx++) {
                img.setRGB(startX + dx, startY + dy, rgb);
            }
        }
    }
}
