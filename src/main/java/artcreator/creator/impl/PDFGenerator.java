package artcreator.creator.impl;

import artcreator.domain.ArtworkConfig;
import artcreator.domain.OutputSize;
import artcreator.domain.Template;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PDFGenerator {
    private static final float MM_TO_POINTS = 72f / 25.4f;
    private static final float PAGE_WIDTH = 595.28f;  // A4 width in points
    private static final float PAGE_HEIGHT = 841.89f; // A4 height in points
    private static final float MARGIN = 20 * MM_TO_POINTS;

    private static String fmt(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }

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
        content.append("BT\n").append("/F1 24 Tf\n").append(fmt("%.2f %.2f Td\n", MARGIN, y - 24)).append("(Pixelator3D - Bauanleitung) Tj\n").append("ET\n");

        // Config info
        y -= 60;
        content.append("BT\n").append("/F1 12 Tf\n").append(fmt("%.2f %.2f Td\n", MARGIN, y)).append("(Pixelgroesse: ").append(config.getPixelSize()).append(") Tj\n").append("0 -18 Td\n").append("(Farben: ").append(config.getColorCount()).append(") Tj\n").append("0 -18 Td\n").append("(Modus: ").append(config.isMode3D() ? "3D" : "2D").append(") Tj\n").append("0 -18 Td\n").append("(Ausgabe: ").append(config.getOutputSize()).append(") Tj\n").append("ET\n");

        writer.addPage(content.toString());
    }

    private void writeInstructionsPage(PDFWriter writer, BufferedImage image, ArtworkConfig config, List<Color> palette) throws IOException {
        var colorCounts = countColors(image, palette, config.getPixelSize());
        var content = new StringBuilder();
        var y = PAGE_HEIGHT - MARGIN;

        // Title
        content.append("BT\n").append("/F1 18 Tf\n").append(fmt("%.2f %.2f Td\n", MARGIN, y - 18)).append("(Materialliste & Legende) Tj\n").append("ET\n");
        y -= 50;

        // Legend table
        var colorIndex = 0;
        for (var entry : colorCounts.entrySet()) {
            var color = entry.getKey();
            var count = entry.getValue();
            var code = getColorCode(colorIndex);

            // Color swatch + border
            content.append(fmt("%.3f %.3f %.3f rg\n", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f)).append(fmt("%.2f %.2f 20 15 re f\n", MARGIN, y - 15)).append("0 0 0 RG\n").append(fmt("%.2f %.2f 20 15 re S\n", MARGIN, y - 15));

            // Text
            content.append("BT\n").append("0 0 0 rg\n").append("/F1 11 Tf\n").append(fmt("%.2f %.2f Td\n", MARGIN + 30, y - 12)).append(fmt("(%s = RGB\\(%d,%d,%d\\) - %d Stueck) Tj\n", code, color.getRed(), color.getGreen(), color.getBlue(), count)).append("ET\n");

            y -= 22;
            colorIndex++;
            if (y < MARGIN + 100) break;
        }

        // 3D mode: add stick count
        if (config.isMode3D()) {
            var totalPixels = colorCounts.values().stream().mapToInt(Integer::intValue).sum();
            y -= 10;
            content.append("BT\n").append("/F1 12 Tf\n").append(fmt("%.2f %.2f Td\n", MARGIN, y)).append(fmt("(3D-Modus: %d Staebchen benoetigt) Tj\n", totalPixels)).append("ET\n");
            y -= 20;
        }

        // Instructions
        y -= 30;
        content.append("BT\n").append("/F1 14 Tf\n").append(fmt("%.2f %.2f Td\n", MARGIN, y)).append("(Anleitung:) Tj\n").append("ET\n");

        y -= 25;
        content.append("BT\n").append("/F1 11 Tf\n").append(fmt("%.2f %.2f Td\n", MARGIN, y)).append("(1. Verwende das Raster auf den folgenden Seiten als Vorlage.) Tj\n").append("0 -16 Td\n").append("(2. Jede Zelle zeigt den Farbcode \\(z.B. A, B, C...\\).) Tj\n").append("0 -16 Td\n").append(config.isMode3D() ? "(3. Fuer 3D: Staebchen unter jede Zelle setzen.) Tj\n" : "(3. Lege die entsprechenden Materialien nach der Legende.) Tj\n").append("ET\n");

        writer.addPage(content.toString());
    }

    private void writeGridPages(PDFWriter writer, BufferedImage image, ArtworkConfig config, List<Color> palette) throws IOException {
        var outputSize = config.getOutputSize();
        var pixelSize = config.getPixelSize();
        var gridW = image.getWidth() / pixelSize;
        var gridH = image.getHeight() / pixelSize;

        if (outputSize == OutputSize.A4) {
            writeSingleGridPage(writer, image, config, palette, gridW, gridH, 0, 0, gridW, gridH, 1, 1);
            return;
        }
        var tilesX = (int) Math.ceil(outputSize.getWidthMM() / (double) OutputSize.A4.getWidthMM());
        var tilesY = (int) Math.ceil(outputSize.getHeightMM() / (double) OutputSize.A4.getHeightMM());
        var cellsPerTileX = (int) Math.ceil(gridW / (double) tilesX);
        var cellsPerTileY = (int) Math.ceil(gridH / (double) tilesY);
        var totalPages = tilesX * tilesY;
        var pageNum = 1;

        for (var ty = 0; ty < tilesY; ty++)
            for (var tx = 0; tx < tilesX; tx++) {
                writeSingleGridPage(writer, image, config, palette, gridW, gridH, tx * cellsPerTileX, ty * cellsPerTileY, cellsPerTileX, cellsPerTileY, pageNum, totalPages);
                pageNum++;
            }
    }

    private void writeSingleGridPage(PDFWriter writer, BufferedImage image, ArtworkConfig config, List<Color> palette, int gridW, int gridH, int startCellX, int startCellY, int cellsW, int cellsH, int pageNum, int totalPages) throws IOException {
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
        content.append("BT\n").append("/F1 10 Tf\n").append(fmt("%.2f %.2f Td\n", MARGIN, PAGE_HEIGHT - MARGIN)).append(fmt("(Rastervorlage - Seite %d/%d) Tj\n", pageNum, totalPages)).append("ET\n");

        // Draw grid
        for (var gy = 0; gy < actualCellsH; gy++)
            for (var gx = 0; gx < actualCellsW; gx++) {
                var imgX = Math.min((startCellX + gx) * pixelSize + pixelSize / 2, image.getWidth() - 1);
                var imgY = Math.min((startCellY + gy) * pixelSize + pixelSize / 2, image.getHeight() - 1);

                var pixelColor = new Color(image.getRGB(imgX, imgY));
                var colorIndex = findNearestColorIndex(pixelColor, palette);
                var code = getColorCode(colorIndex);
                var c = palette.get(colorIndex);

                var x = startX + gx * cellSize;
                var y = startY - (gy + 1) * cellSize;

                // Fill cell + border
                content.append(fmt("%.3f %.3f %.3f rg\n", c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f)).append(fmt("%.2f %.2f %.2f %.2f re f\n", x, y, cellSize, cellSize)).append("0.5 0.5 0.5 RG\n").append(fmt("%.2f %.2f %.2f %.2f re S\n", x, y, cellSize, cellSize));

                // Code text if cell is large enough
                if (!(cellSize > 10)) continue;
                var brightness = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                var textColor = brightness > 128 ? "0 0 0" : "1 1 1";
                content.append("BT\n").append(textColor).append(" rg\n").append(fmt("/F1 %.1f Tf\n", Math.min(8, cellSize * 0.6f))).append(fmt("%.2f %.2f Td\n", x + cellSize * 0.2f, y + cellSize * 0.3f)).append(fmt("(%s) Tj\n", code)).append("ET\n");
            }

        writer.addPage(content.toString());
    }

    private Map<Color, Integer> countColors(BufferedImage image, List<Color> palette, int pixelSize) {
        var counts = new LinkedHashMap<Color, Integer>();
        palette.forEach(c -> counts.put(c, 0));

        for (var y = 0; y < image.getHeight(); y += pixelSize)
            for (var x = 0; x < image.getWidth(); x += pixelSize) {
                var imgX = Math.min(x + pixelSize / 2, image.getWidth() - 1);
                var imgY = Math.min(y + pixelSize / 2, image.getHeight() - 1);
                var pixel = new Color(image.getRGB(imgX, imgY));
                counts.merge(findNearest(pixel, palette), 1, Integer::sum);
            }
        return counts;
    }

    private Color findNearest(Color target, List<Color> palette) {
        return palette.stream().min(Comparator.comparingInt(c -> colorDistance(target, c))).orElse(target);
    }

    private int findNearestColorIndex(Color target, List<Color> palette) {
        var minDist = Integer.MAX_VALUE;
        var minIndex = 0;
        for (var i = 0; i < palette.size(); i++) {
            var dist = colorDistance(target, palette.get(i));
            if (dist >= minDist) continue;
            minDist = dist;
            minIndex = i;
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
            write("%PDF-2.0\n%âãÏÓ\n");
        }

        void addPage(String content) throws IOException {
            var contentBytes = content.getBytes(StandardCharsets.ISO_8859_1);

            // Content stream object
            var contentObjId = nextObjectId++;
            objectOffsets.add(currentOffset);
            write("%d 0 obj\n<< /Length %d >>\nstream\n".formatted(contentObjId, contentBytes.length));
            out.write(contentBytes);
            currentOffset += contentBytes.length;
            write("\nendstream\nendobj\n");

            // Page object
            var pageObjId = nextObjectId++;
            objectOffsets.add(currentOffset);
            pageObjectIds.add(pageObjId);
            write("""
                    %d 0 obj
                    << /Type /Page /Parent 1 0 R /MediaBox [0 0 595.28 841.89] /Contents %d 0 R /Resources << /Font << /F1 2 0 R >> >> >>
                    endobj
                    """.formatted(pageObjId, contentObjId));
        }

        void finish() throws IOException {
            // Font object (object 2 - referenced by pages)
            var fontOffset = currentOffset;
            write("""
                    2 0 obj
                    << /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>
                    endobj
                    """);

            // Pages object (object 1)
            var pagesOffset = currentOffset;
            var kids = pageObjectIds.stream().map(id -> id + " 0 R").collect(Collectors.joining(" "));
            write("1 0 obj\n<< /Type /Pages /Kids [%s] /Count %d >>\nendobj\n".formatted(kids, pageObjectIds.size()));

            // Catalog object (object 3)
            var catalogOffset = currentOffset;
            write("""
                    3 0 obj
                    << /Type /Catalog /Pages 1 0 R >>
                    endobj
                    """);

            // Cross-reference table
            var xrefOffset = currentOffset;
            var xrefEntries = new StringBuilder().append("xref\n").append("0 ").append(nextObjectId).append("\n").append("0000000000 65535 f \n").append("%010d 00000 n \n".formatted(pagesOffset)).append("%010d 00000 n \n".formatted(fontOffset)).append("%010d 00000 n \n".formatted(catalogOffset));
            objectOffsets.forEach(offset -> xrefEntries.append("%010d 00000 n \n".formatted(offset)));
            write(xrefEntries.toString());

            // Trailer
            write("""
                    trailer
                    << /Size %d /Root 3 0 R >>
                    startxref
                    %d
                    %%%%EOF
                    """.formatted(nextObjectId, xrefOffset));
        }

        private void write(String s) throws IOException {
            var bytes = s.getBytes(StandardCharsets.ISO_8859_1);
            out.write(bytes);
            currentOffset += bytes.length;
        }
    }
}
