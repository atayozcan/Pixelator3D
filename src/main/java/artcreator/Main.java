package artcreator;

import artcreator.gui.CreatorFrame;
import artcreator.gui.UIConfig;

import javax.swing.*;

public class Main {
    static void main() {
        System.setProperty("sun.java2d.uiScale", "2");
        System.setProperty("GDK_BACKEND", "wayland");
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("jdk.gtk.version", "4");
        System.setProperty("GTK_USE_PORTAL", "1");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                UIManager.put("Slider.paintValue", Boolean.FALSE);
                UIConfig.init();
                new CreatorFrame().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
