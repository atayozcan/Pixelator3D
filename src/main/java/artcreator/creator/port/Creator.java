package artcreator.creator.port;

import artcreator.domain.Template;

import java.io.File;

public interface Creator {
    void loadImage(File file);
    void pixelate(int pixelSize);
    void reset();
    Template getTemplate();
}
