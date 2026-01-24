package artcreator.creator.impl;

import artcreator.domain.ArtworkConfig;
import artcreator.domain.Template;
import artcreator.domain.port.Domain;
import artcreator.statemachine.port.State.S;
import artcreator.statemachine.port.StateMachine;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class CreatorImpl {
    private final StateMachine stateMachine;
    private final Template template;
    private final PixelationEngine engine = new PixelationEngine();
    private final PDFGenerator pdfGenerator = new PDFGenerator();

    public CreatorImpl(StateMachine stateMachine, Domain domain) {
        this.stateMachine = stateMachine;
        this.template = domain.mkTemplate();
    }

    public void loadImage(File file) {
        try {
            var image = ImageIO.read(file);
            if (image == null) return;
            template.setOriginalImage(image);
            stateMachine.setState(S.IMAGE_LOADED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pixelate(int pixelSize) {
        if (!template.hasOriginalImage()) return;
        var pixelated = engine.pixelate(template.getOriginalImage(), pixelSize);
        template.setPixelatedImage(pixelated);
        template.setLastPixelSize(pixelSize);
        stateMachine.setState(S.PIXELATED);
    }

    public void applyConfig(ArtworkConfig config) {
        if (!template.hasOriginalImage()) return;
        // Copy config values to template's config
        var templateConfig = template.getConfig();
        templateConfig.setPixelSize(config.getPixelSize());
        templateConfig.setColorCount(config.getColorCount());
        templateConfig.setMode3D(config.isMode3D());
        templateConfig.setOutputSize(config.getOutputSize());

        var pixelated = engine.pixelate(template.getOriginalImage(), templateConfig);
        template.setPixelatedImage(pixelated);
        stateMachine.setState(S.PIXELATED);
    }

    public void generatePDF(File outputFile) {
        if (template.getDisplayImage() == null) return;
        try {
            pdfGenerator.generate(template, outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        template.setOriginalImage(null);
        template.setPixelatedImage(null);
        stateMachine.setState(S.HOME);
    }

    public Template getTemplate() {
        return template;
    }
}
