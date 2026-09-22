package com.glims;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

public class GUIMainPanel extends JPanel {

    public JPanel ribbonPanel;
    public JPanel contentPanel;
    public JPanel menuButtonsPanel;


    public GUIMainPanel(
        Branding branding,
        JButton rbbnLogoutBtn,
        JButton rbbnUserBtn,
        JButton rbbnAboutBtn,
        JButton ctntBorrowItemBtn,
        JButton ctntBorrowerListBtn,
        JButton ctntUpdateInventoryBtn,
        JButton ctntTransactionHistoryBtn
    ) {
        this.setBackground(Color.black);
        this.setLayout(new GridBagLayout());

        // --- Ribbon Panel ---
        ribbonPanel = new JPanel();
        ribbonPanel.setBackground(branding.maroon);
        ribbonPanel.setLayout(new GridLayout(1, 1));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        JLabel titleLabel1 = new JLabel("UP TACLOBAN");
        JLabel titleLabel2 = new JLabel("General Laboratory Database");
        titleLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel1.setFont(branding.sizedFontPalatinoBig);
        titleLabel2.setFont(branding.sizedFontPalatinoSmall);
        titleLabel1.setForeground(branding.white);
        titleLabel2.setForeground(branding.white);
        titlePanel.add(titleLabel1);
        titlePanel.add(titleLabel2);

        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 35, 8));
        menuPanel.setBackground(branding.lightgray);
        menuPanel.setOpaque(false);
        menuPanel.add(rbbnUserBtn);
        menuPanel.add(rbbnAboutBtn);
        menuPanel.add(rbbnLogoutBtn);

        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(new ImageIcon(branding.rbbnUPLogoResized));

        JPanel container1 = new JPanel(new BorderLayout());
        container1.setOpaque(false);
        container1.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        container1.add(titlePanel, BorderLayout.NORTH);
        container1.add(menuPanel, BorderLayout.SOUTH);

        JPanel container2 = new JPanel(new BorderLayout());
        container2.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
        container2.add(logoLabel, BorderLayout.WEST);
        container2.setOpaque(false);

        JPanel overlayPanel = new JPanel();
        overlayPanel.setLayout(new OverlayLayout(overlayPanel));
        overlayPanel.setOpaque(false);
        overlayPanel.add(container1);
        overlayPanel.add(container2);

        ribbonPanel.add(overlayPanel);

        // --- Content Panel ---
        contentPanel = new JPanel();
        contentPanel.setBackground(branding.white);
        contentPanel.setLayout(new GridBagLayout());

        menuButtonsPanel = new JPanel();
        menuButtonsPanel.setPreferredSize(new Dimension(600, 400));
        menuButtonsPanel.setBorder(BorderFactory.createMatteBorder(40, 5, 5, 5, branding.maroon));
        menuButtonsPanel.setBackground(branding.lightgray);
        menuButtonsPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 25, 0);
        gbc.gridx = 0;

        gbc.gridy = 0;
        menuButtonsPanel.add(ctntBorrowItemBtn, gbc);
        gbc.gridy = 1;
        menuButtonsPanel.add(ctntBorrowerListBtn, gbc);
        gbc.gridy = 2;
        menuButtonsPanel.add(ctntUpdateInventoryBtn, gbc);
        gbc.gridy = 3;
        menuButtonsPanel.add(ctntTransactionHistoryBtn, gbc);

        contentPanel.add(menuButtonsPanel);

        GridBagConstraints mainGBC = new GridBagConstraints();
        mainGBC.fill = GridBagConstraints.BOTH;
        mainGBC.weightx = 1;

        mainGBC.gridy = 0;
        mainGBC.weighty = 0.01;
        this.add(ribbonPanel, mainGBC);

        mainGBC.gridy = 1;
        mainGBC.weighty = 1.0;
        this.add(contentPanel, mainGBC);
    }
}