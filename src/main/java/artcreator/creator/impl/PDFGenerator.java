package artcreator.creator.impl;

import artcreator.domain.ArtworkConfig;
import artcreator.domain.OutputSize;
import artcreator.domain.Template;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

public class PDFGenerator {
    private static final float MM_TO_POINTS = 72f / 25.4f;
    private static final float PAGE_WIDTH = 595.28f;  // A4 width in points
    private static final float PAGE_HEIGHT = 841.89f; // A4 height in points
    private static final float MARGIN = 20 * MM_TO_POINTS;

    public void generate(Template template, File outputFile) throws IOException {
        var config = template.getConfig();
        var image = template.getDisplayImage();
        var palette = ColorQuantizer.getPalette(image, config.getColorCount());

        try (var out = new FileOutputStream(outputFile)) {
            var writer = new PDFWriter(out);

            // Title page
            writeTitlePage(writer, config);

            // Instructions page with legend
            writeInstructionsPage(writer, image, config, palette);

            // Grid pages
            writeGridPages(writer, image, config, palette);

            writer.finish();
        }
    }

    private void writeTitlePage(PDFWriter writer, ArtworkConfig config) throws IOException {
        var content = new StringBuilder();
        var y = PAGE_HEIGHT - MARGIN;

        // Title
        content.append("BT\n");
        content.append("/F1 24 Tf\n");
        content.append(String.format(Locale.US, "%.2f %.2f Td\n", MARGIN, y - 24));
        content.append("(Pixelator3D - Bauanleitung) Tj\n");
        content.append("ET\n");

        // Config info
        y -= 60;
        content.append("BT\n");
        content.append("/F1 12 Tf\n");
        content.append(String.format(Locale.US, "%.2f %.2f Td\n", MARGIN, y));
        content.append("(Pixelgroesse: " + config.getPixelSize() + ") Tj\n");
        content.append("0 -18 Td\n");
        content.append("(Farben: " + config.getColorCount() + ") Tj\n");
        content.append("0 -18 Td\n");
        content.append("(Modus: " + (config.isMode3D() ? "3D" : "2D") + ") Tj\n");
        content.append("0 -18 Td\n");
        content.append("(Ausgabe: " + config.getOutputSize() + ") Tj\n");
        content.append("ET\n");

        writer.addPage(content.toString());
    }

    private void writeInstructionsPage(PDFWriter writer, BufferedImage image, ArtworkConfig config,
                                       List<Color> palette) throws IOException {
        var colorCounts = countColors(image, palette, config.getPixelSize());
        var content = new StringBuilder();
        var y = PAGE_HEIGHT - MARGIN;

        // Title
        content.append("BT\n");
        content.append("/F1 18 Tf\n");
        content.append(String.format(Locale.US, "%.2f %.2f Td\n", MARGIN, y - 18));
        content.append("(Materialliste & Legende) Tj\n");
        content.append("ET\n");
        y -= 50;

        // Legend table
        var colorIndex = 0;
        for (var entry : colorCounts.entrySet()) {
            var color = entry.getKey();
            var count = entry.getValue();
            var code = getColorCode(colorIndex);

            // Color swatch
            content.append(String.format(Locale.US, "%.3f %.3f %.3f rg\n",
                    color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f));
            content.append(String.format(Locale.US, "%.2f %.2f 20 15 re f\n", MARGIN, y - 15));

            // Border
            content.append("0 0 0 RG\n");
            content.append(String.format(Locale.US, "%.2f %.2f 20 15 re S\n", MARGIN, y - 15));

            // Text
            content.append("BT\n");
            content.append("0 0 0 rg\n");
            content.append("/F1 11 Tf\n");
            content.append(String.format(Locale.US, "%.2f %.2f Td\n", MARGIN + 30, y - 12));
            content.append("(" + code + " = RGB\\(" + color.getRed() + "," + color.getGreen() + "," +
                    color.getBlue() + "\\) - " + count + " Stueck) Tj\n");
            content.append("ET\n");

            y -= 22;
            colorIndex++;
            if (y < MARGIN + 100) break;
        }

        // 3D mode: add stick count
        if (config.isMode3D()) {
            var totalPixels = colorCounts.values().stream().mapToInt(Integer::intValue).sum();
            y -= 10;
            content.append("BT\n");
            content.append("/F1 12 Tf\n");
            content.append(String.format(Locale.US, "%.2f %.2f Td\n", MARGIN, y));
            content.append("(3D-Modus: " + totalPixels + " Staebchen benoetigt) Tj\n");
            content.append("ET\n");
            y -= 20;
        }

        // Instructions
        y -= 30;
        content.append("BT\n");
        content.append("/F1 14 Tf\n");
        content.append(String.format(Locale.US, "%.2f %.2f Td\n", MARGIN, y));
        content.append("(Anleitung:) Tj\n");
        content.append("ET\n");

        y -= 25;
        content.append("BT\n");
        content.append("/F1 11 Tf\n");
        content.append(String.format(Locale.US, "%.2f %.2f Td\n", MARGIN, y));
        content.append("(1. Verwende das Raster auf den folgenden Seiten als Vorlage.) Tj\n");
        content.append("0 -16 Td\n");
        content.append("(2. Jede Zelle zeigt den Farbcode \\(z.B. A, B, C...\\).) Tj\n");
        content.append("0 -16 Td\n");
        if (config.isMode3D()) {
            content.append("(3. Fuer 3D: Staebchen unter jede Zelle setzen.) Tj\n");
        } else {
            content.append("(3. Lege die entsprechenden Materialien nach der Legende.) Tj\n");
        }
        content.append("ET\n");

        writer.addPage(content.toString());
    }

    private void writeGridPages(PDFWriter writer, BufferedImage image, ArtworkConfig config,
                                List<Color> palette) throws IOException {
        var outputSize = config.getOutputSize();
        var pixelSize = config.getPixelSize();
        var gridW = image.getWidth() / pixelSize;
        var gridH = image.getHeight() / pixelSize;

        if (outputSize == OutputSize.A4) {
            writeSingleGridPage(writer, image, config, palette, gridW, gridH, 0, 0, gridW, gridH, 1, 1);
        } else {
            var tilesX = (int) Math.ceil(outputSize.getWidthMM() / (double) OutputSize.A4.getWidthMM());
            var tilesY = (int) Math.ceil(outputSize.getHeightMM() / (double) OutputSize.A4.getHeightMM());
            var cellsPerTileX = (int) Math.ceil(gridW / (double) tilesX);
            var cellsPerTileY = (int) Math.ceil(gridH / (double) tilesY);
            var totalPages = tilesX * tilesY;
            var pageNum = 1;

            for (var ty = 0; ty < tilesY; ty++) {
                for (var tx = 0; tx < tilesX; tx++) {
                    writeSingleGridPage(writer, image, config, palette, gridW, gridH,
                            tx * cellsPerTileX, ty * cellsPerTileY,
                            cellsPerTileX, cellsPerTileY, pageNum, totalPages);
                    pageNum++;
                }
            }
        }
    }

    private void writeSingleGridPage(PDFWriter writer, BufferedImage image, ArtworkConfig config,
                                     List<Color> palette, int gridW, int gridH,
                                     int startCellX, int startCellY, int cellsW, int cellsH,
                                     int pageNum, int totalPages) throws IOException {
        var content = new StringBuilder();
        var pageWidth = PAGE_WIDTH - 2 * MARGIN;
        var pageHeight = PAGE_HEIGHT - 2 * MARGIN - 30;
        var pixelSize = config.getPixelSize();

        var actualCellsW = Math.min(cellsW, gridW - startCellX);
        var actualCellsH = Math.min(cellsH, gridH - startCellY);

        var cellSize = Math.min(pageWidth / actualCellsW, pageHeight / actualCellsH);
        var gridWidthPx = cellSize * actualCellsW;
        var startX = MARGIN + (pageWidth - gridWidthPx) / 2;
        var startY = PAGE_HEIGHT - MARGIN - 30;

        // Page header
        content.append("BT\n");
        content.append("/F1 10 Tf\n");
        content.append(String.format(Locale.US, "%.2f %.2f Td\n", MARGIN, PAGE_HEIGHT - MARGIN));
        content.append("(Rastervorlage - Seite " + pageNum + "/" + totalPages + ") Tj\n");
        content.append("ET\n");

        // Draw grid
        for (var gy = 0; gy < actualCellsH; gy++) {
            for (var gx = 0; gx < actualCellsW; gx++) {
                var imgX = (startCellX + gx) * pixelSize + pixelSize / 2;
                var imgY = (startCellY + gy) * pixelSize + pixelSize / 2;

                if (imgX >= image.getWidth()) imgX = image.getWidth() - 1;
                if (imgY >= image.getHeight()) imgY = image.getHeight() - 1;

                var pixelColor = new Color(image.getRGB(imgX, imgY));
                var colorIndex = findNearestColorIndex(pixelColor, palette);
                var code = getColorCode(colorIndex);
                var c = palette.get(colorIndex);

                var x = startX + gx * cellSize;
                var y = startY - (gy + 1) * cellSize;

                // Fill cell
                content.append(String.format(Locale.US, "%.3f %.3f %.3f rg\n",
                        c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f));
                content.append(String.format(Locale.US, "%.2f %.2f %.2f %.2f re f\n", x, y, cellSize, cellSize));

                // Border
                content.append("0.5 0.5 0.5 RG\n");
                content.append(String.format(Locale.US, "%.2f %.2f %.2f %.2f re S\n", x, y, cellSize, cellSize));

                // Code text if cell is large enough
                if (cellSize > 10) {
                    var brightness = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                    var textColor = brightness > 128 ? "0 0 0" : "1 1 1";
                    content.append("BT\n");
                    content.append(textColor + " rg\n");
                    content.append(String.format(Locale.US, "/F1 %.1f Tf\n", Math.min(8, cellSize * 0.6f)));
                    content.append(String.format(Locale.US, "%.2f %.2f Td\n", x + cellSize * 0.2f, y + cellSize * 0.3f));
                    content.append("(" + code + ") Tj\n");
                    content.append("ET\n");
                }
            }
        }

        writer.addPage(content.toString());
    }

    private Map<Color, Integer> countColors(BufferedImage image, List<Color> palette, int pixelSize) {
        var counts = new LinkedHashMap<Color, Integer>();
        for (var c : palette) counts.put(c, 0);

        for (var y = 0; y < image.getHeight(); y += pixelSize) {
            for (var x = 0; x < image.getWidth(); x += pixelSize) {
                var imgX = x + pixelSize / 2;
                var imgY = y + pixelSize / 2;
                if (imgX >= image.getWidth()) imgX = image.getWidth() - 1;
                if (imgY >= image.getHeight()) imgY = image.getHeight() - 1;
                var pixel = new Color(image.getRGB(imgX, imgY));
                var nearest = findNearest(pixel, palette);
                counts.merge(nearest, 1, Integer::sum);
            }
        }
        return counts;
    }

    private Color findNearest(Color target, List<Color> palette) {
        return palette.stream()
                .min(Comparator.comparingInt(c -> colorDistance(target, c)))
                .orElse(target);
    }

    private int findNearestColorIndex(Color target, List<Color> palette) {
        var minDist = Integer.MAX_VALUE;
        var minIndex = 0;
        for (var i = 0; i < palette.size(); i++) {
            var dist = colorDistance(target, palette.get(i));
            if (dist < minDist) {
                minDist = dist;
                minIndex = i;
            }
        }
        return minIndex;
    }

    private int colorDistance(Color a, Color b) {
        var dr = a.getRed() - b.getRed();
        var dg = a.getGreen() - b.getGreen();
        var db = a.getBlue() - b.getBlue();
        return dr * dr + dg * dg + db * db;
    }

    private String getColorCode(int index) {
        if (index < 26) return String.valueOf((char) ('A' + index));
        return String.valueOf((char) ('A' + index / 26 - 1)) + (char) ('A' + index % 26);
    }

    private static class PDFWriter {
        private final OutputStream out;
        private final List<Long> objectOffsets = new ArrayList<>();
        private final List<Integer> pageObjectIds = new ArrayList<>();
        private long currentOffset = 0;
        // Reserved: 1=Pages, 2=Font, 3=Catalog
        private int nextObjectId = 4;

        PDFWriter(OutputStream out) throws IOException {
            this.out = out;
            write("%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n");
        }

        void addPage(String content) throws IOException {
            var contentBytes = content.getBytes(StandardCharsets.ISO_8859_1);

            // Content stream object
            var contentObjId = nextObjectId++;
            objectOffsets.add(currentOffset);
            write(contentObjId + " 0 obj\n");
            write("<< /Length " + contentBytes.length + " >>\n");
            write("stream\n");
            out.write(contentBytes);
            currentOffset += contentBytes.length;
            write("\nendstream\n");
            write("endobj\n");

            // Page object
            var pageObjId = nextObjectId++;
            objectOffsets.add(currentOffset);
            pageObjectIds.add(pageObjId);
            write(pageObjId + " 0 obj\n");
            write("<< /Type /Page /Parent 1 0 R /MediaBox [0 0 595.28 841.89] ");
            write("/Contents " + contentObjId + " 0 R /Resources << /Font << /F1 2 0 R >> >> >>\n");
            write("endobj\n");
        }

        void finish() throws IOException {
            // Font object (object 2 - referenced by pages)
            var fontOffset = currentOffset;
            write("2 0 obj\n");
            write("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\n");
            write("endobj\n");

            // Pages object (object 1)
            var pagesOffset = currentOffset;
            write("1 0 obj\n");
            write("<< /Type /Pages /Kids [");
            for (var pageId : pageObjectIds) {
                write(pageId + " 0 R ");
            }
            write("] /Count " + pageObjectIds.size() + " >>\n");
            write("endobj\n");

            // Catalog object (object 3)
            var catalogOffset = currentOffset;
            write("3 0 obj\n");
            write("<< /Type /Catalog /Pages 1 0 R >>\n");
            write("endobj\n");

            // Cross-reference table
            var xrefOffset = currentOffset;
            write("xref\n");
            write("0 " + nextObjectId + "\n");
            write("0000000000 65535 f \n");
            write(String.format("%010d 00000 n \n", pagesOffset));
            write(String.format("%010d 00000 n \n", fontOffset));
            write(String.format("%010d 00000 n \n", catalogOffset));
            for (var offset : objectOffsets) {
                write(String.format("%010d 00000 n \n", offset));
            }

            // Trailer
            write("trailer\n");
            write("<< /Size " + nextObjectId + " /Root 3 0 R >>\n");
            write("startxref\n");
            write(xrefOffset + "\n");
            write("%%EOF\n");
        }

        private void write(String s) throws IOException {
            var bytes = s.getBytes(StandardCharsets.ISO_8859_1);
            out.write(bytes);
            currentOffset += bytes.length;
        }
    }
}
