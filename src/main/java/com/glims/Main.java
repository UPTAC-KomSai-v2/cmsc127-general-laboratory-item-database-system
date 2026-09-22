package com.glims;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    public static void main(String[] args) {
        // Force Metal instead of the platform L&F: on macOS, Aqua's button
        // UI ignores setBackground()/setForeground() and renders its own
        // native chrome, so every custom-colored JButton in this app (which
        // was evidently built/tested against Windows, where the default L&F
        // does respect those colors) renders as a blank white pill with
        // invisible text. Metal honors them directly, everywhere.
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Could not set Metal look and feel, falling back to default: " + e.getMessage());
        }

        SwingUtilities.invokeLater(GraphicalUserInterface::new);
    }
}