package artcreator.gui;

import artcreator.creator.port.Creator;
import artcreator.statemachine.port.Observer;
import artcreator.statemachine.port.State;
import artcreator.statemachine.port.Subject;

import java.awt.*;
import java.io.File;
import java.util.concurrent.CompletableFuture;

public class Controller implements Observer {
    private final CreatorFrame view;
    private final Creator model;

    public Controller(CreatorFrame view, Subject subject, Creator model) {
        this.view = view;
        this.model = model;
        subject.attach(this);
    }

    public void onLoadImage() {
        var dialog = new FileDialog(view, "Open Image File", FileDialog.LOAD);
        dialog.setFilenameFilter((_, name) -> {
            var lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                   lower.endsWith(".png") || lower.endsWith(".gif") ||
                   lower.endsWith(".bmp");
        });
        dialog.setVisible(true);

        var dir = dialog.getDirectory();
        var file = dialog.getFile();
        if (dir == null || file == null) return;
        CompletableFuture.runAsync(() -> model.loadImage(new File(dir, file)));
    }

    public void onPixelate() {
        var pixelSize = view.getControlPanel().getPixelSize();
        CompletableFuture.runAsync(() -> model.pixelate(pixelSize));
    }

    @Override
    public void update(State newState) {}
}
