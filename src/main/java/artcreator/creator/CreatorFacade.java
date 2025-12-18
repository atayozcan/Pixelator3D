package artcreator.creator;

import artcreator.creator.impl.CreatorImpl;
import artcreator.creator.port.Creator;
import artcreator.domain.ArtworkConfig;
import artcreator.domain.DomainFactory;
import artcreator.domain.Template;
import artcreator.statemachine.StateMachineFactory;
import artcreator.statemachine.port.State.S;
import artcreator.statemachine.port.StateMachine;

import java.io.File;

public class CreatorFacade implements CreatorFactory, Creator {
    private CreatorImpl creator;
    private StateMachine stateMachine;

    @Override
    public Creator creator() {
        if (this.creator != null) return this;
        this.stateMachine = StateMachineFactory.FACTORY.stateMachine();
        this.creator = new CreatorImpl(stateMachine, DomainFactory.FACTORY.domain());
        return this;
    }

    @Override
    public synchronized void loadImage(File file) {
        if (!this.stateMachine.getState().isSubStateOf(S.HOME)) return;
        this.creator.loadImage(file);
    }

    @Override
    public synchronized void pixelate(int pixelSize) {
        if (!this.stateMachine.getState().isSubStateOf(S.IMAGE_LOADED)) return;
        this.creator.pixelate(pixelSize);
    }

    @Override
    public synchronized void applyConfig(ArtworkConfig config) {
        if (!this.stateMachine.getState().isSubStateOf(S.IMAGE_LOADED)) return;
        this.creator.applyConfig(config);
    }

    @Override
    public synchronized void generatePDF(File outputFile) {
        if (!this.stateMachine.getState().isSubStateOf(S.IMAGE_LOADED)) return;
        this.creator.generatePDF(outputFile);
    }

    @Override
    public synchronized void reset() {
        if (!this.stateMachine.getState().isSubStateOf(S.HOME)) return;
        this.creator.reset();
    }

    @Override
    public synchronized Template getTemplate() {
        return this.creator.getTemplate();
    }
}
