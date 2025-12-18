package artcreator.creator.impl;

import artcreator.domain.ArtworkConfig;
import artcreator.domain.OutputSize;
import artcreator.domain.Template;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PDFGenerator {
    private static final float MM_TO_POINTS = 72f / 25.4f;
    private static final float MARGIN = 20 * MM_TO_POINTS;

    public void generate(Template template, File outputFile) throws IOException {
        var config = template.getConfig();
        var image = template.getDisplayImage();

        try (var doc = new PDDocument()) {
            // Title page
            addTitlePage(doc, image, config);

            // Instructions page with legend
            addInstructionsPage(doc, image, config);

            // Grid pages (with tiling if needed)
            addGridPages(doc, image, config);

            doc.save(outputFile);
        }
    }

    private void addTitlePage(PDDocument doc, BufferedImage image, ArtworkConfig config) throws IOException {
        var page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        try (var cs = new PDPageContentStream(doc, page)) {
            var fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            var font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // Title
            cs.beginText();
            cs.setFont(fontBold, 24);
            cs.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN - 24);
            cs.showText("Pixelator3D - Bauanleitung");
            cs.endText();

            // Config info
            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN - 60);
            cs.showText("Raster: " + config.getGridWidth() + " x " + config.getGridHeight());
            cs.newLineAtOffset(0, -18);
            cs.showText("Farben: " + config.getColorCount());
            cs.newLineAtOffset(0, -18);
            cs.showText("Modus: " + (config.isMode3D() ? "3D" : "2D"));
            cs.newLineAtOffset(0, -18);
            cs.showText("Ausgabe: " + config.getOutputSize());
            cs.endText();

            // Preview image
            var previewWidth = 400f;
            var previewHeight = previewWidth * image.getHeight() / image.getWidth();
            var pdImage = LosslessFactory.createFromImage(doc, image);
            cs.drawImage(pdImage, MARGIN, page.getMediaBox().getHeight() - MARGIN - 150 - previewHeight,
                    previewWidth, previewHeight);
        }
    }

    private void addInstructionsPage(PDDocument doc, BufferedImage image, ArtworkConfig config) throws IOException {
        var page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        var palette = ColorQuantizer.getPalette(image, config.getColorCount());
        var colorCounts = countColors(image, palette);

        try (var cs = new PDPageContentStream(doc, page)) {
            var fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            var font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float y = page.getMediaBox().getHeight() - MARGIN;

            // Title
            cs.beginText();
            cs.setFont(fontBold, 18);
            cs.newLineAtOffset(MARGIN, y - 18);
            cs.showText("Materialliste & Legende");
            cs.endText();
            y -= 50;

            // Legend table
            var colorIndex = 0;
            for (var entry : colorCounts.entrySet()) {
                var color = entry.getKey();
                var count = entry.getValue();
                var code = getColorCode(colorIndex);

                // Color swatch
                cs.setNonStrokingColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
                cs.addRect(MARGIN, y - 15, 20, 15);
                cs.fill();

                // Border
                cs.setStrokingColor(0, 0, 0);
                cs.addRect(MARGIN, y - 15, 20, 15);
                cs.stroke();

                // Text
                cs.beginText();
                cs.setFont(font, 11);
                cs.setNonStrokingColor(0, 0, 0);
                cs.newLineAtOffset(MARGIN + 30, y - 12);
                cs.showText(code + " = RGB(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() +
                        ") - " + count + " Stück");
                cs.endText();

                y -= 22;
                colorIndex++;

                if (y < MARGIN + 50) break; // Prevent overflow
            }

            // Instructions
            y -= 30;
            cs.beginText();
            cs.setFont(fontBold, 14);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("Anleitung:");
            cs.endText();

            y -= 25;
            cs.beginText();
            cs.setFont(font, 11);
            cs.newLineAtOffset(MARGIN, y);
            cs.showText("1. Verwende das Raster auf den folgenden Seiten als Vorlage.");
            cs.newLineAtOffset(0, -16);
            cs.showText("2. Jede Zelle zeigt den Farbcode (z.B. A, B, C...).");
            cs.newLineAtOffset(0, -16);
            cs.showText("3. Lege die entsprechenden Materialien nach der Legende.");
            cs.endText();
        }
    }

    private void addGridPages(PDDocument doc, BufferedImage image, ArtworkConfig config) throws IOException {
        var outputSize = config.getOutputSize();
        var gridW = config.getGridWidth();
        var gridH = config.getGridHeight();

        var palette = ColorQuantizer.getPalette(image, config.getColorCount());

        if (outputSize == OutputSize.A4) {
            addSingleGridPage(doc, image, config, palette, gridW, gridH, 1, 1);
        } else {
            // Calculate tiling
            var tilesX = (int) Math.ceil(outputSize.getWidthMM() / (double) OutputSize.A4.getWidthMM());
            var tilesY = (int) Math.ceil(outputSize.getHeightMM() / (double) OutputSize.A4.getHeightMM());

            var cellsPerTileX = (int) Math.ceil(gridW / (double) tilesX);
            var cellsPerTileY = (int) Math.ceil(gridH / (double) tilesY);

            var totalPages = tilesX * tilesY;
            var pageNum = 1;

            for (var ty = 0; ty < tilesY; ty++) {
                for (var tx = 0; tx < tilesX; tx++) {
                    addTiledGridPage(doc, image, config, palette,
                            tx * cellsPerTileX, ty * cellsPerTileY,
                            cellsPerTileX, cellsPerTileY,
                            pageNum, totalPages);
                    pageNum++;
                }
            }
        }
    }

    private void addSingleGridPage(PDDocument doc, BufferedImage image, ArtworkConfig config,
                                   List<Color> palette, int gridW, int gridH,
                                   int pageNum, int totalPages) throws IOException {
        var page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        var pageWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
        var pageHeight = page.getMediaBox().getHeight() - 2 * MARGIN - 30;

        var cellSize = Math.min(pageWidth / gridW, pageHeight / gridH);
        var gridWidthPx = cellSize * gridW;
        var gridHeightPx = cellSize * gridH;
        var startX = MARGIN + (pageWidth - gridWidthPx) / 2;
        var startY = page.getMediaBox().getHeight() - MARGIN - 30;

        try (var cs = new PDPageContentStream(doc, page)) {
            var font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // Page header
            cs.beginText();
            cs.setFont(font, 10);
            cs.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);
            cs.showText("Rastervorlage - Seite " + pageNum + "/" + totalPages);
            cs.endText();

            // Draw grid
            drawGrid(cs, image, config, palette, startX, startY, cellSize, 0, 0, gridW, gridH, font);
        }
    }

    private void addTiledGridPage(PDDocument doc, BufferedImage image, ArtworkConfig config,
                                  List<Color> palette, int startCellX, int startCellY,
                                  int cellsW, int cellsH, int pageNum, int totalPages) throws IOException {
        var page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        var pageWidth = page.getMediaBox().getWidth() - 2 * MARGIN;
        var pageHeight = page.getMediaBox().getHeight() - 2 * MARGIN - 30;

        var cellSize = Math.min(pageWidth / cellsW, pageHeight / cellsH);
        var startX = MARGIN;
        var startY = page.getMediaBox().getHeight() - MARGIN - 30;

        try (var cs = new PDPageContentStream(doc, page)) {
            var font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // Page header
            cs.beginText();
            cs.setFont(font, 10);
            cs.newLineAtOffset(MARGIN, page.getMediaBox().getHeight() - MARGIN);
            cs.showText("Rastervorlage - Seite " + pageNum + "/" + totalPages +
                    " (Zellen " + startCellX + "-" + (startCellX + cellsW - 1) +
                    ", " + startCellY + "-" + (startCellY + cellsH - 1) + ")");
            cs.endText();

            drawGrid(cs, image, config, palette, startX, startY, cellSize,
                    startCellX, startCellY, cellsW, cellsH, font);
        }
    }

    private void drawGrid(PDPageContentStream cs, BufferedImage image, ArtworkConfig config,
                          List<Color> palette, float startX, float startY, float cellSize,
                          int offsetX, int offsetY, int cellsW, int cellsH,
                          PDType1Font font) throws IOException {
        var imgW = image.getWidth();
        var imgH = image.getHeight();
        var gridW = config.getGridWidth();
        var gridH = config.getGridHeight();
        var imgCellW = imgW / gridW;
        var imgCellH = imgH / gridH;

        for (var gy = 0; gy < cellsH && (offsetY + gy) < gridH; gy++) {
            for (var gx = 0; gx < cellsW && (offsetX + gx) < gridW; gx++) {
                var imgX = (offsetX + gx) * imgCellW + imgCellW / 2;
                var imgY = (offsetY + gy) * imgCellH + imgCellH / 2;

                if (imgX >= imgW) imgX = imgW - 1;
                if (imgY >= imgH) imgY = imgH - 1;

                var pixelColor = new Color(image.getRGB(imgX, imgY));
                var colorIndex = findNearestColorIndex(pixelColor, palette);
                var code = getColorCode(colorIndex);

                var x = startX + gx * cellSize;
                var y = startY - (gy + 1) * cellSize;

                // Fill cell with color
                var c = palette.get(colorIndex);
                cs.setNonStrokingColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
                cs.addRect(x, y, cellSize, cellSize);
                cs.fill();

                // Draw border
                cs.setStrokingColor(0.5f, 0.5f, 0.5f);
                cs.addRect(x, y, cellSize, cellSize);
                cs.stroke();

                // Draw code if cell is large enough
                if (cellSize > 10) {
                    var brightness = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                    cs.setNonStrokingColor(brightness > 128 ? 0 : 1, brightness > 128 ? 0 : 1, brightness > 128 ? 0 : 1);
                    cs.beginText();
                    cs.setFont(font, Math.min(8, cellSize * 0.6f));
                    cs.newLineAtOffset(x + cellSize * 0.2f, y + cellSize * 0.3f);
                    cs.showText(code);
                    cs.endText();
                }
            }
        }
    }

    private Map<Color, Integer> countColors(BufferedImage image, List<Color> palette) {
        var counts = new LinkedHashMap<Color, Integer>();
        for (var c : palette) counts.put(c, 0);

        for (var y = 0; y < image.getHeight(); y++) {
            for (var x = 0; x < image.getWidth(); x++) {
                var pixel = new Color(image.getRGB(x, y));
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
}
