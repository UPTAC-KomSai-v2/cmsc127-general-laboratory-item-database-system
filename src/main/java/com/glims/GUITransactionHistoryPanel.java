package com.glims;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class GUITransactionHistoryPanel extends JPanel {
    private Branding branding;
    private JPanel screen1, scrn1TransactionContentPanel;
    private JButton screen1BackButton, toggleTransactionTypeButton;
    private Controller ctrl;
    private List<String[]> allEntries;
    // 0 = all, 1 = borrows, 2 = returns
    private int currentView = 0;

    public GUITransactionHistoryPanel(Controller ctrl, Branding branding, JButton tranBackBtn) {
        this.ctrl = ctrl;
        this.branding = branding;
        this.screen1BackButton = tranBackBtn;
        this.allEntries = new ArrayList<>();
        
        this.setLayout(new GridBagLayout());
        this.setBackground(branding.lightgray);
        this.setPreferredSize(new Dimension(900, 500));
        this.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, branding.maroon));
        
        initializeScreen1();

        GridBagConstraints transactionHistoryPanelGBC = new GridBagConstraints();
        transactionHistoryPanelGBC.fill = GridBagConstraints.BOTH;
        transactionHistoryPanelGBC.weightx = 1;
        transactionHistoryPanelGBC.weighty = 1;
        this.add(screen1, transactionHistoryPanelGBC);
    }
    
    public void initializeScreen1() {
        screen1 = new JPanel();
        screen1.setLayout(new GridBagLayout());
        screen1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        screen1.setBackground(branding.lightgray);
        
        scrn1TransactionContentPanel = new JPanel();
        scrn1TransactionContentPanel.setLayout(new BoxLayout(scrn1TransactionContentPanel, BoxLayout.Y_AXIS));
        scrn1TransactionContentPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));

        JScrollPane scrollContentPanel = new JScrollPane(scrn1TransactionContentPanel);
        scrollContentPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollContentPanel.getVerticalScrollBar().setUnitIncrement(16);
        scrollContentPanel.setBorder(null);
        branding.reskinScrollBar(scrollContentPanel, branding.gray);

        // Add headers panel - start with all transactions view
        JPanel headersPanel = createHeadersPanel();

        // Create toggle button panel
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        togglePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 28));
        togglePanel.setBackground(branding.maroon);
        togglePanel.setOpaque(false);

        // Toggle transaction type button - start with "View Borrows"
        toggleTransactionTypeButton = new JButton("View Borrows");
        toggleTransactionTypeButton.setPreferredSize(new Dimension(150, 30));
        toggleTransactionTypeButton.setBackground(branding.maroon);
        toggleTransactionTypeButton.setForeground(branding.lightergray);
        toggleTransactionTypeButton.addActionListener(e -> toggleTransactionType());
        togglePanel.add(toggleTransactionTypeButton);

        // Back button
        screen1BackButton.setPreferredSize(new Dimension(150, 30));
        screen1BackButton.setBackground(branding.maroon);
        screen1BackButton.setForeground(branding.lightergray);
        togglePanel.add(screen1BackButton);

        GridBagConstraints screen1GBC = new GridBagConstraints();
        screen1GBC.fill = GridBagConstraints.BOTH;
        screen1GBC.weightx = 1;
        screen1GBC.weighty = 0.05; // Reduced to make room for headers
        screen1GBC.gridx = 0;
        screen1GBC.gridy = 0;
        screen1GBC.insets = new Insets(0, 0, 10, 0); 
        screen1.add(headersPanel, screen1GBC);

        screen1GBC.weighty = 0.9;
        screen1GBC.gridy = 1;
        screen1.add(scrollContentPanel, screen1GBC);

        screen1GBC.weighty = 0.01;
        screen1GBC.gridy = 2;
        screen1.add(togglePanel, screen1GBC);
    }
    
    private JPanel createHeadersPanel() {
        JPanel headersPanel = new JPanel(new GridBagLayout());
        headersPanel.setBackground(branding.maroon);

        GridBagConstraints headerGBC = new GridBagConstraints();
        headerGBC.fill = GridBagConstraints.HORIZONTAL;
        
        // Create labels for headers - consistent across all views
        JLabel[] headerLabels = new JLabel[3];
        headerLabels[0] = new JLabel("                     Date and Time");
        headerLabels[1] = new JLabel("Name");
        headerLabels[2] = new JLabel("Item");
        JLabel accountabilityLabel = new JLabel("Accountability");

        // Style headers
        for (JLabel label : headerLabels) {
            label.setForeground(branding.lightergray);
            label.setFont(new Font(label.getFont().getName(), Font.BOLD, 12));
            label.setHorizontalAlignment(SwingConstants.LEFT);
        }
        accountabilityLabel.setForeground(branding.lightergray);
        accountabilityLabel.setFont(new Font(accountabilityLabel.getFont().getName(), Font.BOLD, 12));
        accountabilityLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Add headers
        headerGBC.gridx = 1; // Skipping the first column (for icon)
        headerGBC.weightx = 0.3;
        headerGBC.ipadx = 100;
        headersPanel.add(headerLabels[0], headerGBC);
        
        headerGBC.gridx++;
        headerGBC.weightx = 0.2;
        headerGBC.ipadx = 70;
        headersPanel.add(headerLabels[1], headerGBC);
        
        headerGBC.gridx++;
        headerGBC.weightx = 0.2;
        headerGBC.ipadx = 70;
        headersPanel.add(headerLabels[2], headerGBC);
        
        headerGBC.gridx++;
        headerGBC.weightx = 0.2;
        headerGBC.ipadx = 70;
        headersPanel.add(accountabilityLabel, headerGBC);

        headersPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
        return headersPanel;
    }

    private void toggleTransactionType() {
        // Cycle through views: 0 (All) -> 1 (Borrows) -> 2 (Returns) -> 0 (All)
        currentView = (currentView + 1) % 3;
        
        // Update button text based on the next view
        switch (currentView) {
            case 0: // All transactions
                toggleTransactionTypeButton.setText("View Borrows");
                break;
            case 1: // Borrows
                toggleTransactionTypeButton.setText("View Returns");
                break;
            case 2: // Returns
                toggleTransactionTypeButton.setText("View All");
                break;
        }
        
        // Refresh entries to filter based on current view
        refreshEntries(allEntries);
        
        // Revalidate and repaint to ensure visual update
        screen1.revalidate();
        screen1.repaint();
    }

    public void refreshEntries(List<String[]> entries) {
        // Store all entries for toggle functionality
        this.allEntries = new ArrayList<>(entries);
        
        scrn1TransactionContentPanel.removeAll();

        for (String[] tuple : entries) {
            // Determine if the entry should be displayed based on current view
            boolean isBorrowTransaction = tuple[0].equals("Borrow");
            
            // Skip if not matching the current view filter
            if ((currentView == 1 && !isBorrowTransaction) || (currentView == 2 && isBorrowTransaction)) {
                continue;
            }

            int i = 0;
            JLabel stateLabel = new JLabel();
            switch (tuple[i++]) {
                case "Borrow" -> stateLabel.setIcon(new ImageIcon(branding.borrowIcon));
                case "Return" -> stateLabel.setIcon(new ImageIcon(branding.returnIcon));
                default -> System.err.println("Transaction History Label Error");
            }
            
            // Date label
            JLabel dateLabel = new JLabel(branding.reformatDateLabel(tuple[i++]));
            
            // Name label
            JLabel nameLabel = new JLabel(tuple[i++]);
            
            // Item label - always show this for both borrows and returns
            JLabel itemLabel = new JLabel(tuple[i++]);
            
            // Skip an entry if needed
            i++;
            
            // Accountability label
            JLabel accountabilityLabel = new JLabel();
            if(tuple[i] != null) {
                accountabilityLabel.setText("Php " + tuple[i++]);
            } else {
                accountabilityLabel.setText("---");
                i++;
            }
            
            // Set foreground color for labels
            dateLabel.setForeground(branding.white);
            nameLabel.setForeground(branding.white);
            itemLabel.setForeground(branding.white);
            accountabilityLabel.setForeground(branding.white);

            // Create panel containers for labels
            JPanel statePanel = new JPanel();
            JPanel dateLabelPanel = new JPanel();
            JPanel nameLabelPanel = new JPanel();
            JPanel itemLabelPanel = new JPanel();
            JPanel accountabilityLabelPanel = new JPanel();

            // Set layouts
            statePanel.setLayout(new BorderLayout());
            dateLabelPanel.setLayout(new BorderLayout());
            nameLabelPanel.setLayout(new BorderLayout());
            itemLabelPanel.setLayout(new BorderLayout());
            accountabilityLabelPanel.setLayout(new BorderLayout());

            // Set preferred sizes
            statePanel.setPreferredSize(new Dimension(10, 70));
            dateLabelPanel.setPreferredSize(new Dimension(50, 70));
            nameLabelPanel.setPreferredSize(new Dimension(70, 70));
            itemLabelPanel.setPreferredSize(new Dimension(50, 70));
            accountabilityLabelPanel.setPreferredSize(new Dimension(50, 70));

            // Make panels transparent
            statePanel.setOpaque(false);
            dateLabelPanel.setOpaque(false);
            nameLabelPanel.setOpaque(false);
            itemLabelPanel.setOpaque(false);
            accountabilityLabelPanel.setOpaque(false);

            // Add labels to panels
            statePanel.add(stateLabel, BorderLayout.WEST);
            dateLabelPanel.add(dateLabel, BorderLayout.WEST);
            nameLabelPanel.add(nameLabel, BorderLayout.WEST);
            itemLabelPanel.add(itemLabel, BorderLayout.WEST);
            accountabilityLabelPanel.add(accountabilityLabel, BorderLayout.WEST);

            // Create panel for entire row
            JPanel tupleInfoPanel = new JPanel();
            tupleInfoPanel.setMaximumSize(new Dimension(900, 70));
            tupleInfoPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            tupleInfoPanel.setLayout(new GridBagLayout());
            tupleInfoPanel.setBackground(branding.maroon);

            // Add components to row panel
            GridBagConstraints tupleInfoPanelGBC = new GridBagConstraints();
            tupleInfoPanelGBC.fill = GridBagConstraints.HORIZONTAL;
            
            tupleInfoPanelGBC.gridx = 0;
            tupleInfoPanelGBC.ipadx = 50;
            tupleInfoPanelGBC.weightx = 0.01;
            tupleInfoPanel.add(statePanel, tupleInfoPanelGBC);
            
            tupleInfoPanelGBC.gridx++;
            tupleInfoPanelGBC.ipadx = 100;
            tupleInfoPanelGBC.weightx = 0.3;
            tupleInfoPanel.add(dateLabelPanel, tupleInfoPanelGBC);
            
            tupleInfoPanelGBC.gridx++;
            tupleInfoPanelGBC.ipadx = 70;
            tupleInfoPanelGBC.weightx = 0.2;
            tupleInfoPanel.add(nameLabelPanel, tupleInfoPanelGBC);
            
            tupleInfoPanelGBC.gridx++;
            tupleInfoPanelGBC.ipadx = 70;
            tupleInfoPanelGBC.weightx = 0.2;
            tupleInfoPanel.add(itemLabelPanel, tupleInfoPanelGBC);
            
            tupleInfoPanelGBC.gridx++;
            tupleInfoPanelGBC.ipadx = 70;
            tupleInfoPanelGBC.weightx = 0.2;
            tupleInfoPanel.add(accountabilityLabelPanel, tupleInfoPanelGBC);

            // Add row to content panel
            scrn1TransactionContentPanel.add(tupleInfoPanel);
            scrn1TransactionContentPanel.add(Box.createVerticalStrut(10)); // Add gaps between rows
        }

        scrn1TransactionContentPanel.revalidate();
        scrn1TransactionContentPanel.repaint();
    }
}