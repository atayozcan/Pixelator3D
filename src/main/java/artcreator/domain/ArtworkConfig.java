package artcreator.domain;

public class ArtworkConfig {
    private int pixelSize = 10;
    private int colorCount = 16;
    private boolean mode3D = false;
    private OutputSize outputSize = OutputSize.A4;

    public int getPixelSize() { return pixelSize; }
    public void setPixelSize(int pixelSize) { this.pixelSize = Math.max(2, Math.min(50, pixelSize)); }

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
