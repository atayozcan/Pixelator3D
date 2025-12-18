package artcreator.gui;

import artcreator.creator.CreatorFactory;
import artcreator.creator.port.Creator;
import artcreator.gui.components.ContentPage;
import artcreator.gui.components.ControlPanel;
import artcreator.gui.components.ImagePreviewPanel;
import artcreator.gui.components.TabBar;
import artcreator.statemachine.StateMachineFactory;
import artcreator.statemachine.port.Observer;
import artcreator.statemachine.port.State;
import artcreator.statemachine.port.Subject;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.TooManyListenersException;

public class CreatorFrame extends JFrame implements Observer {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient Creator creator = CreatorFactory.FACTORY.creator();
    private final ControlPanel controlPanel;
    private final ImagePreviewPanel imagePreviewPanel;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final CardLayout homeCardLayout = new CardLayout();
    private final JPanel homeContent = new JPanel(homeCardLayout);
    private final TabBar tabBar;

    public CreatorFrame() throws TooManyListenersException {
        super("Pixelator3D");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        Subject subject = StateMachineFactory.FACTORY.subject();
        subject.attach(this);

        var controller = new Controller(this, subject, creator);
        controlPanel = new ControlPanel(controller::onLoadImage, controller::onApply, controller::onGeneratePDF);
        imagePreviewPanel = new ImagePreviewPanel();

        // Welcome view
        var welcomePage = new ContentPage("Welcome to Pixelator3D", null, null, ContentPage.createActionButton("Select Image", controller::onLoadImage));

        // Editor view
        var editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(controlPanel.getPanel(), BorderLayout.NORTH);
        editorPanel.add(imagePreviewPanel.getPanel(), BorderLayout.CENTER);

        // Home content switches between welcome and editor
        homeContent.add(welcomePage.getPanel(), "welcome");
        homeContent.add(editorPanel, "editor");

        var helpPage = new ContentPage("How to Use Pixelator3D", null, """
                1. Load Image: Click 'Load Image' to select an image file (JPG, PNG, GIF, BMP)
                2. Configure: Set grid resolution, color count (8/16/32), and 2D/3D mode
                3. Apply: Click 'Apply' to see the preview with your settings
                4. Generate PDF: Click 'Generate PDF' to create building instructions

                The PDF includes a material list, legend, and grid template.
                For sizes larger than A4, the grid is automatically tiled.
                """, null);
        var aboutPage = new ContentPage("Pixelator3D", "Version 1.0", """
                A simple application for you to create your dream art!
                
                Made by Atay Ozcan (95270) and Jose Acena (85534).
                
                Copyright 2025
                """, null);

        content.add(homeContent, "home");
        content.add(helpPage.getPanel(), "help");
        content.add(aboutPage.getPanel(), "about");

        tabBar = new TabBar(cardLayout, content);
        tabBar.addTab("Home", "home");
        tabBar.addTab("Help", "help");
        tabBar.addTab("About", "about");

        setLayout(new BorderLayout());
        add(tabBar.getPanel(), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    @Override
    public void update(State newState) {
        SwingUtilities.invokeLater(() -> {
            switch (newState) {
                case State.S.HOME -> {
                    tabBar.setSelected(0);
                    cardLayout.show(content, "home");
                    homeCardLayout.show(homeContent, "welcome");
                    controlPanel.setButtonsEnabled(false);
                }
                case State.S.IMAGE_LOADED, State.S.PIXELATED -> {
                    tabBar.setSelected(0);
                    cardLayout.show(content, "home");
                    homeCardLayout.show(homeContent, "editor");
                    controlPanel.setButtonsEnabled(true);
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
