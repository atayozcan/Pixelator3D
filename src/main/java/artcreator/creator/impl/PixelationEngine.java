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
        var gridW = config.getGridWidth();
        var gridH = config.getGridHeight();
        var colorCount = config.getColorCount();
        var mode3D = config.isMode3D();

        var pixelated = pixelateGrid(original, gridW, gridH);
        var quantized = ColorQuantizer.quantize(pixelated, colorCount);

        if (mode3D) {
            return apply3DEffect(quantized, gridW, gridH);
        }
        return quantized;
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

    private BufferedImage pixelateGrid(BufferedImage original, int gridW, int gridH) {
        var width = original.getWidth();
        var height = original.getHeight();
        var cellW = Math.max(1, width / gridW);
        var cellH = Math.max(1, height / gridH);
        var result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (var gy = 0; gy < gridH; gy++) {
            for (var gx = 0; gx < gridW; gx++) {
                var x = gx * cellW;
                var y = gy * cellH;
                var color = calculateAverageColor(original, x, y, cellW, cellH);
                fillBlock(result, x, y, cellW, cellH, color, width, height);
            }
        }
        return result;
    }

    private BufferedImage apply3DEffect(BufferedImage image, int gridW, int gridH) {
        var width = image.getWidth();
        var height = image.getHeight();
        var cellW = Math.max(1, width / gridW);
        var cellH = Math.max(1, height / gridH);
        var result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var g = result.createGraphics();
        g.drawImage(image, 0, 0, null);

        for (var gy = 0; gy < gridH; gy++) {
            for (var gx = 0; gx < gridW; gx++) {
                var x = gx * cellW;
                var y = gy * cellH;

                // Shadow (bottom-right)
                g.setColor(new Color(0, 0, 0, 60));
                g.fillRect(x + cellW - 2, y + 2, 2, cellH - 2);
                g.fillRect(x + 2, y + cellH - 2, cellW - 2, 2);

                // Highlight (top-left)
                g.setColor(new Color(255, 255, 255, 40));
                g.drawLine(x, y, x + cellW - 1, y);
                g.drawLine(x, y, x, y + cellH - 1);
            }
        }
        g.dispose();
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
