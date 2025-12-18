package artcreator.creator.impl;

import artcreator.domain.Template;
import artcreator.domain.port.Domain;
import artcreator.statemachine.port.State.S;
import artcreator.statemachine.port.StateMachine;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreatorImpl {
    private static final Logger LOG = Logger.getLogger(CreatorImpl.class.getName());

    private final StateMachine stateMachine;
    private final Template template;
    private final PixelationEngine engine = new PixelationEngine();

    public CreatorImpl(StateMachine stateMachine, Domain domain) {
        this.stateMachine = stateMachine;
        this.template = domain.mkTemplate();
    }

    public void loadImage(File file) {
        try {
            var image = ImageIO.read(file);
            if (image == null) {
                LOG.log(Level.WARNING, "Failed to load: " + file.getName());
                return;
            }
            template.setOriginalImage(image);
            stateMachine.setState(S.IMAGE_LOADED);
            LOG.log(Level.INFO, "Loaded: " + file.getName());
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error loading image", e);
        }
    }

    public void pixelate(int pixelSize) {
        if (!template.hasOriginalImage()) {
            LOG.log(Level.WARNING, "No image loaded");
            return;
        }
        var pixelated = engine.pixelate(template.getOriginalImage(), pixelSize);
        template.setPixelatedImage(pixelated);
        template.setLastPixelSize(pixelSize);
        stateMachine.setState(S.PIXELATED);
        LOG.log(Level.INFO, "Pixelated with size: " + pixelSize);
    }

    public void reset() {
        template.setOriginalImage(null);
        template.setPixelatedImage(null);
        stateMachine.setState(S.HOME);
    }

    public Template getTemplate() { return template; }
}
