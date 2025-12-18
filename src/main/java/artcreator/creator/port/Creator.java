package artcreator.creator.port;

import artcreator.domain.ArtworkConfig;
import artcreator.domain.Template;

import java.io.File;

public interface Creator {
    void loadImage(File file);
    void pixelate(int pixelSize);
    void applyConfig(ArtworkConfig config);
    void generatePDF(File outputFile);
    void reset();
    Template getTemplate();
}
