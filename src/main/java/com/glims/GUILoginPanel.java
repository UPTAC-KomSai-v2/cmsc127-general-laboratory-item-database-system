package com.glims;


import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class GUILoginPanel extends JPanel{

    public JLabel lgnStatusLabel;
    public JPasswordField lgnInputPasswordField;
    public JTextField lgnInputUsernameField;

    public GUILoginPanel(Branding branding, JButton lgnLoginBtn){

        // Main login panel with maroon background
        this.setBackground(branding.maroon);
        this.setLayout(new GridBagLayout());
        
        // Title section with logo and text
        JPanel lgnTitlePanel = new JPanel();
        lgnTitlePanel.setBackground(branding.white);
        lgnTitlePanel.setLayout(new GridBagLayout());
        lgnTitlePanel.setOpaque(false); // Transparent panel for layering

        // UP logo
        JLabel lgnUPLogo = new JLabel();
        lgnUPLogo.setIcon(new ImageIcon(branding.lgnUPLogoResized));
        
        // Title labels
        JLabel lgnTitleLabel1 = new JLabel("UP TACLOBAN");
        JLabel lgnTitleLabel2 = new JLabel("General Laboratory Database");
        
        lgnTitleLabel1.setFont(branding.sizedFontPalatinoBig);
        lgnTitleLabel2.setFont(branding.sizedFontPalatinoSmall);
        lgnTitleLabel1.setForeground(branding.white);
        lgnTitleLabel2.setForeground(branding.white);

        // Position the elements inside title panel
        GridBagConstraints lgnTitlePanelGBC = new GridBagConstraints();
        lgnTitlePanelGBC.insets = new Insets(0, 0, 30, 0);
        lgnTitlePanelGBC.gridy = 0;
        lgnTitlePanel.add(lgnUPLogo, lgnTitlePanelGBC);
        lgnTitlePanelGBC.insets = new Insets(0, 0, 15, 0);
        lgnTitlePanelGBC.gridy = 1;
        lgnTitlePanel.add(lgnTitleLabel1, lgnTitlePanelGBC);
        lgnTitlePanelGBC.gridy = 2;
        lgnTitlePanel.add(lgnTitleLabel2, lgnTitlePanelGBC);
        
        // Input section for username and password
        JPanel lgnInputPanel = new JPanel();
        lgnInputPanel.setBackground(branding.lightgray);
        lgnInputPanel.setLayout(new GridBagLayout());
        lgnInputPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // "Login" title inside input panel
        JLabel lgnLoginLabel = new JLabel("Login", SwingConstants.CENTER);
        lgnLoginLabel.setFont(new Font("Roboto", Font.PLAIN, 25));
        lgnLoginLabel.setForeground(branding.maroon);

        // Username and password labels
        GridLayout gridLayoutForLabels = new GridLayout(1,2);
        JPanel inputUserPanel = new JPanel(gridLayoutForLabels);
        JPanel inputPasswordPanel = new JPanel(gridLayoutForLabels);
        JLabel lgnIputUsernameLabel = new JLabel("Enter Username: ");
        JLabel lgnIputPasswordLabel = new JLabel("Enter Password: ");
        inputUserPanel.add(lgnIputUsernameLabel);
        inputPasswordPanel.add(lgnIputPasswordLabel);
        lgnIputUsernameLabel.setForeground(branding.maroon);
        lgnIputPasswordLabel.setForeground(branding.maroon);

        // Input fields
        lgnInputUsernameField = new JTextField(10);
        lgnInputPasswordField = new JPasswordField(10);
        lgnInputUsernameField.setPreferredSize(new Dimension(200, 30));
        lgnInputPasswordField.setPreferredSize(new Dimension(200, 30));
        lgnInputUsernameField.addActionListener(e -> lgnLoginBtn.doClick());
        lgnInputPasswordField.addActionListener(e -> lgnLoginBtn.doClick());

         // Status label for feedback (e.g. "Login Confirmed" or "Login Denied")
        lgnStatusLabel = new JLabel("Please enter login credentials.", SwingConstants.CENTER); //.setText("Login Confirmed, Login Denied")
        lgnStatusLabel.setForeground(branding.maroon);
        
        // Position the components inside lgnInputPanel
        GridBagConstraints lgnInputPanelGBC = new GridBagConstraints();
        lgnInputPanelGBC.fill = GridBagConstraints.HORIZONTAL;
        lgnInputPanelGBC.weightx = 0.01;
        lgnInputPanelGBC.insets = new Insets(0, 0, 20, 0);
        lgnInputPanelGBC.gridy = 0;
        lgnInputPanel.add(lgnLoginLabel, lgnInputPanelGBC);
        lgnInputPanelGBC.insets = new Insets(0, 30, 10, 30);
        lgnInputPanelGBC.gridy = 1;
        lgnInputPanel.add(inputUserPanel, lgnInputPanelGBC);
        lgnInputPanelGBC.gridy = 2;
        lgnInputPanel.add(lgnInputUsernameField, lgnInputPanelGBC);
        lgnInputPanelGBC.gridy = 3;
        lgnInputPanel.add(inputPasswordPanel, lgnInputPanelGBC);
        lgnInputPanelGBC.gridy = 4;
        lgnInputPanel.add(lgnInputPasswordField, lgnInputPanelGBC);
        lgnInputPanelGBC.insets = new Insets(30, 30, 0, 30);
        lgnInputPanelGBC.gridy = 5;
        lgnInputPanel.add(lgnStatusLabel, lgnInputPanelGBC);
        lgnInputPanelGBC.insets = new Insets(70, 30, 0, 30);
        lgnInputPanelGBC.gridy = 6;
        lgnInputPanel.add(lgnLoginBtn, lgnInputPanelGBC);

        // Container for title and input panel
        JPanel lgnContainer = new JPanel();
        lgnContainer.setPreferredSize(new Dimension(900, 400));
        lgnContainer.setLayout(new GridBagLayout());
        lgnContainer.setOpaque(false);

        // Position title and input panels inside container
        GridBagConstraints lgnContainerGBC = new GridBagConstraints();
        lgnContainerGBC.fill = GridBagConstraints.BOTH;
        lgnContainerGBC.weightx = 1;
        lgnContainerGBC.weighty = 1;
        lgnContainerGBC.gridx = 0;
        lgnContainer.add(lgnTitlePanel, lgnContainerGBC);
        lgnContainerGBC.weightx = 0.2;
        lgnContainerGBC.gridx = 1;
        lgnContainer.add(lgnInputPanel, lgnContainerGBC);

        // Add login container to login panel and show it in the main frame
        this.add(lgnContainer);
    }
}
