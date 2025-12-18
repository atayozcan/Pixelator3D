package artcreator.gui;

import artcreator.creator.CreatorFactory;
import artcreator.creator.port.Creator;
import artcreator.gui.components.ContentPage;
import artcreator.gui.components.ControlPanel;
import artcreator.gui.components.ImagePreviewPanel;
import artcreator.gui.components.NavigationBar;
import artcreator.statemachine.StateMachineFactory;
import artcreator.statemachine.port.Observer;
import artcreator.statemachine.port.State;
import artcreator.statemachine.port.State.S;
import artcreator.statemachine.port.Subject;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.TooManyListenersException;

public class CreatorFrame extends JFrame implements Observer {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final String HOME = "home", HELP = "help", ABOUT = "about", EDITOR = "editor";
    private final transient Creator creator = CreatorFactory.FACTORY.creator();
    private final NavigationBar navigationBar;
    private final ControlPanel controlPanel;
    private final ImagePreviewPanel imagePreviewPanel;
    private final JPanel topPanel;
    private final JPanel centerPanel;
    private final CardLayout cardLayout;

    public CreatorFrame() throws TooManyListenersException {
        super("Pixelator3D");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        Subject subject = StateMachineFactory.FACTORY.subject();
        subject.attach(this);

        var controller = new Controller(this, subject, creator);
        navigationBar = new NavigationBar(this::showHome, this::showHelp, this::showAbout);
        controlPanel = new ControlPanel(controller::onLoadImage, controller::onPixelate);
        imagePreviewPanel = new ImagePreviewPanel();

        var homePage = new ContentPage("Welcome to Pixelator3D", null, null, ContentPage.createActionButton("Select Image", controller::onLoadImage));
        var helpPage = new ContentPage("How to Use Pixelator3D", null, """
                1. Load Image: Click the 'Select Image' button to choose an image file
                2. Adjust Pixel Size: Use the slider to set the desired pixelation level (2-50)
                3. Pixelate: Click the 'Pixelate' button to apply the effect
                
                Supported formats: JPG, PNG, GIF, BMP
                """, null);
        var aboutPage = new ContentPage("Pixelator3D", "Version 1.0", """
                A simple application for you to create your dream art!
                
                Made by Atay Ozcan (95270) and Jose Acena (85534).
                
                Copyright 2025
                """, null);

        topPanel = new JPanel(new BorderLayout());
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.add(homePage.getPanel(), HOME);
        centerPanel.add(helpPage.getPanel(), HELP);
        centerPanel.add(aboutPage.getPanel(), ABOUT);
        centerPanel.add(imagePreviewPanel.getPanel(), EDITOR);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        showHome();
    }

    private void showPage(String page, boolean showControls, int tabIndex) {
        navigationBar.setSelected(tabIndex);
        topPanel.removeAll();
        topPanel.add(navigationBar.getPanel(), BorderLayout.NORTH);
        if (showControls) topPanel.add(controlPanel.getPanel(), BorderLayout.CENTER);
        topPanel.revalidate();
        topPanel.repaint();
        cardLayout.show(centerPanel, page);
    }

    public void showHome() {
        showPage(HOME, false, 0);
    }

    public void showHelp() {
        showPage(HELP, false, 1);
    }

    public void showAbout() {
        showPage(ABOUT, false, 2);
    }

    public void showEditor() {
        showPage(EDITOR, true, 0);
    }

    @Override
    public void update(State newState) {
        SwingUtilities.invokeLater(() -> {
            switch (newState) {
                case S.HOME -> {
                    showHome();
                    controlPanel.setPixelateButtonEnabled(false);
                }
                case S.IMAGE_LOADED, S.PIXELATED -> {
                    showEditor();
                    controlPanel.setPixelateButtonEnabled(true);
                    imagePreviewPanel.displayImage(creator.getTemplate().getDisplayImage());
                }
                case null, default -> {
                }
            }
        });
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }
}
