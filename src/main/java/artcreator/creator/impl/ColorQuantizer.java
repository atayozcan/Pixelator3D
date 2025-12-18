package artcreator.creator.impl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public final class ColorQuantizer {
    private ColorQuantizer() {}

    public static BufferedImage quantize(BufferedImage image, int colorCount) {
        var colors = extractColors(image);
        var palette = medianCut(colors, colorCount);
        return applyPalette(image, palette);
    }

    public static List<Color> getPalette(BufferedImage image, int colorCount) {
        var colors = extractColors(image);
        return medianCut(colors, colorCount);
    }

    private static List<Color> extractColors(BufferedImage image) {
        var colors = new ArrayList<Color>();
        for (var y = 0; y < image.getHeight(); y++) {
            for (var x = 0; x < image.getWidth(); x++) {
                colors.add(new Color(image.getRGB(x, y)));
            }
        }
        return colors;
    }

    private static List<Color> medianCut(List<Color> colors, int targetCount) {
        var buckets = new ArrayList<List<Color>>();
        buckets.add(new ArrayList<>(colors));

        while (buckets.size() < targetCount) {
            var largest = findLargestBucket(buckets);
            if (largest.size() < 2) break;

            var channel = findWidestChannel(largest);
            largest.sort(Comparator.comparingInt(c -> getChannel(c, channel)));

            var mid = largest.size() / 2;
            var left = new ArrayList<>(largest.subList(0, mid));
            var right = new ArrayList<>(largest.subList(mid, largest.size()));

            buckets.remove(largest);
            buckets.add(left);
            buckets.add(right);
        }

        return buckets.stream()
                .map(ColorQuantizer::averageColor)
                .toList();
    }

    private static List<Color> findLargestBucket(List<List<Color>> buckets) {
        return buckets.stream()
                .max(Comparator.comparingInt(List::size))
                .orElse(buckets.getFirst());
    }

    private static int findWidestChannel(List<Color> colors) {
        var minR = 255; var maxR = 0;
        var minG = 255; var maxG = 0;
        var minB = 255; var maxB = 0;

        for (var c : colors) {
            minR = Math.min(minR, c.getRed()); maxR = Math.max(maxR, c.getRed());
            minG = Math.min(minG, c.getGreen()); maxG = Math.max(maxG, c.getGreen());
            minB = Math.min(minB, c.getBlue()); maxB = Math.max(maxB, c.getBlue());
        }

        var rangeR = maxR - minR;
        var rangeG = maxG - minG;
        var rangeB = maxB - minB;

        if (rangeR >= rangeG && rangeR >= rangeB) return 0;
        if (rangeG >= rangeR && rangeG >= rangeB) return 1;
        return 2;
    }

    private static int getChannel(Color c, int channel) {
        return switch (channel) {
            case 0 -> c.getRed();
            case 1 -> c.getGreen();
            default -> c.getBlue();
        };
    }

    private static Color averageColor(List<Color> colors) {
        if (colors.isEmpty()) return Color.BLACK;
        var r = 0L; var g = 0L; var b = 0L;
        for (var c : colors) {
            r += c.getRed();
            g += c.getGreen();
            b += c.getBlue();
        }
        var n = colors.size();
        return new Color((int)(r/n), (int)(g/n), (int)(b/n));
    }

    private static BufferedImage applyPalette(BufferedImage image, List<Color> palette) {
        var result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (var y = 0; y < image.getHeight(); y++) {
            for (var x = 0; x < image.getWidth(); x++) {
                var original = new Color(image.getRGB(x, y));
                var nearest = findNearest(original, palette);
                result.setRGB(x, y, nearest.getRGB());
            }
        }
        return result;
    }

    private static Color findNearest(Color target, List<Color> palette) {
        return palette.stream()
                .min(Comparator.comparingInt(c -> colorDistance(target, c)))
                .orElse(target);
    }

    private static int colorDistance(Color a, Color b) {
        var dr = a.getRed() - b.getRed();
        var dg = a.getGreen() - b.getGreen();
        var db = a.getBlue() - b.getBlue();
        return dr*dr + dg*dg + db*db;
    }
}
