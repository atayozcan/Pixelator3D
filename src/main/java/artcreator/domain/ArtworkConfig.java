package artcreator.domain;

public class ArtworkConfig {
    private int gridWidth = 50;
    private int gridHeight = 50;
    private int colorCount = 16;
    private boolean mode3D = false;
    private OutputSize outputSize = OutputSize.A4;

    public int getGridWidth() { return gridWidth; }
    public void setGridWidth(int gridWidth) { this.gridWidth = Math.max(10, Math.min(200, gridWidth)); }

    public int getGridHeight() { return gridHeight; }
    public void setGridHeight(int gridHeight) { this.gridHeight = Math.max(10, Math.min(200, gridHeight)); }

    public int getColorCount() { return colorCount; }
    public void setColorCount(int colorCount) {
        if (colorCount == 8 || colorCount == 16 || colorCount == 32) {
            this.colorCount = colorCount;
        }
    }

    public boolean isMode3D() { return mode3D; }
    public void setMode3D(boolean mode3D) { this.mode3D = mode3D; }

    public OutputSize getOutputSize() { return outputSize; }
    public void setOutputSize(OutputSize outputSize) { this.outputSize = outputSize; }
}
