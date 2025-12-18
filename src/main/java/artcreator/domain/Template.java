package artcreator.domain;

import java.awt.image.BufferedImage;

public class Template {
    private BufferedImage originalImage;
    private BufferedImage pixelatedImage;
    private int lastPixelSize;
    private final ArtworkConfig config = new ArtworkConfig();

    public Template() {
        this.originalImage = null;
        this.pixelatedImage = null;
        this.lastPixelSize = 10;
    }

    public ArtworkConfig getConfig() { return config; }

    public BufferedImage getOriginalImage() {
        return originalImage;
    }

    public void setOriginalImage(BufferedImage image) {
        this.originalImage = image;
        this.pixelatedImage = null;
    }

    public void setPixelatedImage(BufferedImage image) {
        this.pixelatedImage = image;
    }

    public void setLastPixelSize(int size) {
        this.lastPixelSize = size;
    }

    public boolean hasOriginalImage() {
        return originalImage != null;
    }

    public BufferedImage getDisplayImage() {
        return pixelatedImage != null ? pixelatedImage : originalImage;
    }
}
