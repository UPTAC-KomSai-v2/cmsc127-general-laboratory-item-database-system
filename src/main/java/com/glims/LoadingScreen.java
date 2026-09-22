package com.glims;

import java.awt.*;
import javax.swing.*;

public class LoadingScreen extends JFrame {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Timer animationTimer;
    private String baseStatusText = "";
    private int dotCount = 0;
    private Branding branding = new Branding();

    public LoadingScreen() {
        //setUndecorated(true);
        branding.setAppIcon(this);
        setTitle("Loading...");
        setSize(400, 120);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        


        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(300, 30));
        progressBar.setBackground(branding.white);
        progressBar.setForeground(branding.maroon);

        statusLabel = new JLabel("Starting...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
        statusLabel.setForeground(branding.maroon);
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.add(statusLabel, BorderLayout.WEST);

        JPanel mainLoadingPanel = new JPanel(new GridBagLayout());
        GridBagConstraints mainLoadingPanelGBC = new GridBagConstraints();
        mainLoadingPanelGBC.fill = GridBagConstraints.HORIZONTAL;
        mainLoadingPanelGBC.insets = new Insets(5, 0, 5, 0);
        mainLoadingPanelGBC.gridy = 0;
        mainLoadingPanel.add(statusPanel, mainLoadingPanelGBC);
        mainLoadingPanelGBC.gridy++;
        mainLoadingPanel.add(progressBar, mainLoadingPanelGBC);
        add(mainLoadingPanel);

        startAnimation();
    }

    public void updateProgress(int progress) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
    }

    public void updateStatus(String text) {
        SwingUtilities.invokeLater(() -> {
            baseStatusText = text;
            statusLabel.setText(baseStatusText);  // Reset to base text when changed
            dotCount = 0;
        });
    }

    private void startAnimation() {
        animationTimer = new Timer(200, e -> {
            dotCount = (dotCount + 1) % 4;  // cycle 0 → 3
            String dots = ".".repeat(dotCount);
            statusLabel.setText(baseStatusText + dots);
        });
        animationTimer.start();
    }

    public void stopAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}
