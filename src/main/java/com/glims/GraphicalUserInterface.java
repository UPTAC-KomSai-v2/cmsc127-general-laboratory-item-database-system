package com.glims;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
public class GraphicalUserInterface implements ActionListener {

    private JButton lgnLoginBtn;
    private JButton ctntBorrowItemBtn, ctntBorrowerListBtn, ctntUpdateInventoryBtn, ctntTransactionHistoryBtn;
    private JPanel ctntBorrowItemPanel, ctntBorrowerListPanel, ctntUpdateInventoryPanel, ctntTransactionHistoryPanel;
    private JButton rbbnLogoutBtn, rbbnUserBtn, rbbnAboutBtn;
    private JButton bitmBackBtn, blstBackBtn, upinBackBtn, tranBackBtn;
    private GUILoginPanel loginPanel;
    private GUIMainPanel mainPanel;
    private JFrame mainFrame;
    private Controller ctrl;
    private Branding branding;
    private JDialog progressDialog;
    private JProgressBar progressBar;
    private LoadingScreen loadingScreen;

    public GraphicalUserInterface() {
        ctrl = new Controller();
        branding = new Branding();
        loadingScreen = new LoadingScreen();

        intializeMainFrame();
        initializeLoginPanel();
        initializeMainPanel();
    }

    public void intializeMainFrame() {
        mainFrame = new JFrame("UPTC General Laboratory Database");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1280, 720);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setMinimumSize(new Dimension(1000, 700));
        branding.setAppIcon(mainFrame);
    }

    public void initializeLoginPanel() {
        lgnLoginBtn = new JButton("Login");
        lgnLoginBtn.setBackground(branding.maroon);
        lgnLoginBtn.setForeground(branding.white);
        lgnLoginBtn.setFocusable(false);
        lgnLoginBtn.setPreferredSize(new Dimension(250, 35));
        lgnLoginBtn.addActionListener(this);

        loginPanel = new GUILoginPanel(branding, lgnLoginBtn);
        mainFrame.add(loginPanel);
        mainFrame.setVisible(true);
    }

    public void initializeMainPanel() {
        // --- Ribbon buttons ---
        rbbnLogoutBtn = new JButton("Logout");
        rbbnUserBtn = new JButton("");
        rbbnAboutBtn = new JButton("About");

        JButton[] ribbonButtons = { rbbnLogoutBtn, rbbnUserBtn, rbbnAboutBtn };
        for (JButton btn : ribbonButtons) {
            btn.setContentAreaFilled(true);
            btn.setBackground(branding.maroon);
            btn.setBorderPainted(false);
            btn.setFocusable(false);
            btn.setForeground(branding.white);
            btn.addActionListener(this);
        }

        // --- Content buttons ---
        ctntBorrowItemBtn = new JButton("Borrow Item");
        ctntBorrowerListBtn = new JButton("Borrower List");
        ctntUpdateInventoryBtn = new JButton("Update Inventory");
        ctntTransactionHistoryBtn = new JButton("Transaction History");

        JButton[] contentButtons = { ctntBorrowItemBtn, ctntBorrowerListBtn, ctntUpdateInventoryBtn, ctntTransactionHistoryBtn };
        for (JButton btn : contentButtons) {
            btn.setForeground(branding.white);
            btn.setBackground(branding.maroon);
            btn.setFocusable(false);
            btn.setPreferredSize(new Dimension(270, 35));
            btn.addActionListener(this);
        }

        // --- Back buttons ---
        bitmBackBtn = new JButton("Go Back");
        blstBackBtn = new JButton("Go Back");
        upinBackBtn = new JButton("Go Back");
        tranBackBtn = new JButton("Go Back");

        JButton[] backButtons = { bitmBackBtn, blstBackBtn, upinBackBtn, tranBackBtn };
        for (JButton btn : backButtons) {
            btn.addActionListener(this);
        }

        // Main panel layout with buttons
        mainPanel = new GUIMainPanel(
            branding,
            rbbnLogoutBtn, rbbnUserBtn, rbbnAboutBtn,
            ctntBorrowItemBtn, ctntBorrowerListBtn, ctntUpdateInventoryBtn, ctntTransactionHistoryBtn
        );

        // Sub-panel views with back buttons
        ctntBorrowItemPanel = new GUIBorrowItemPanel(ctrl, branding, bitmBackBtn);
        ctntBorrowerListPanel = new GUIBorrowerListPanel(ctrl, branding, blstBackBtn);
        ctntUpdateInventoryPanel = new GUIUpdateInventoryPanel(ctrl, branding, upinBackBtn, (GUIBorrowItemPanel)ctntBorrowItemPanel);
        ctntTransactionHistoryPanel = new GUITransactionHistoryPanel(ctrl, branding, tranBackBtn);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == lgnLoginBtn) {
            if(ctrl.controllerLogin(loginPanel, mainFrame, mainPanel, loginPanel.lgnStatusLabel)){
                ctrl.showLoadingScreen();
                // progress = startPercent + (currentStep * rangePercent) / totalSteps; -> Formula for progress
                SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        publish(2);
                        ctrl.controllerGetAllDatabaseInformationWithLoading();
                        ((GUIBorrowItemPanel) ctntBorrowItemPanel).loadCategoryPanel(ctrl.getCategoryList());  // Class2 progress update
                        ((GUIUpdateInventoryPanel) ctntUpdateInventoryPanel).loadContents();
                        return null;
                    }

                    @Override
                    protected void process(java.util.List<Integer> chunks) {
                        int latestProgress = chunks.get(chunks.size() - 1);
                        loadingScreen.updateProgress(latestProgress);
                    }

                    @Override
                    protected void done() {
                        ctrl.hideLoadingScreen();
                        System.out.println("Loading complete!");
                        loginPanel.lgnInputUsernameField.setText("");
                        loginPanel.lgnInputPasswordField.setText("");
                        mainFrame.remove(loginPanel);
                        mainFrame.add(mainPanel);
                        mainFrame.revalidate();
                        mainFrame.repaint();
                    }
                };
                worker.execute();
            } 
        } else if (src == ctntBorrowItemBtn) {
            showPanel(ctntBorrowItemPanel);
            ((GUIBorrowItemPanel) ctntBorrowItemPanel).refreshFormDropdowns();
        } else if (src == ctntBorrowerListBtn) {
            ((GUIBorrowerListPanel) ctntBorrowerListPanel).refreshEntries1(ctrl.getBorrowerListEntries());
            showPanel(ctntBorrowerListPanel);
        } else if (src == ctntUpdateInventoryBtn) { 
            showPanel(ctntUpdateInventoryPanel);
        } else if (src == ctntTransactionHistoryBtn) {
            ((GUITransactionHistoryPanel) ctntTransactionHistoryPanel).refreshEntries(ctrl.getAllTransactionHistory());
            showPanel(ctntTransactionHistoryPanel);
        } else if (src == rbbnLogoutBtn) {
            int result = JOptionPane.showConfirmDialog(
                mainFrame,
                new JLabel("Are you sure you want to logout?", SwingConstants.CENTER),
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );
            if (result == JOptionPane.YES_OPTION) {
                loginPanel.lgnStatusLabel.setText("Please enter login credentials.");
                loginPanel.lgnStatusLabel.setForeground(branding.maroon);

                mainFrame.add(loginPanel);
                mainFrame.remove(mainPanel);
                mainPanel.contentPanel.removeAll();
                mainPanel.contentPanel.add(mainPanel.menuButtonsPanel);
                ((GUIBorrowItemPanel) ctntBorrowItemPanel).resetPanel();
                showMainMenu();
                mainPanel.revalidate();
                mainPanel.repaint();
                mainFrame.revalidate();
                mainFrame.repaint();
            }
        } else if (src == rbbnAboutBtn) {
                showAboutDialog();
        } else if (src == bitmBackBtn) {
            ((GUIBorrowItemPanel) ctntBorrowItemPanel).resetPanel();
            showMainMenu();
        } else if (src == blstBackBtn) {
            showMainMenu();
        } else if (src == upinBackBtn) {
            //((GUIUpdateInventoryPanel) ctntUpdateInventoryPanel).resetPanel();
            showMainMenu();
        } else if (src == tranBackBtn) {
            showMainMenu();
        }
    }

    private void showAboutDialog(){
        JDialog aboutDialog = new JDialog(mainFrame, "About", true);
        aboutDialog.setUndecorated(true); // Remove default window decoration
    
        // Create main panel with maroon background
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(branding.maroon);
        mainPanel.setBorder(BorderFactory.createLineBorder(branding.darkermaroon, 2));
    
        // Header panel with logo
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(branding.maroon);
        JLabel logoLabel = new JLabel(new ImageIcon(branding.rbbnUPLogoResized));
        headerPanel.add(logoLabel);
    
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(branding.lightgray);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // Title with custom font
        JLabel titleLabel = new JLabel("UPTC General Laboratory Database");
        titleLabel.setFont(branding.sizedFontPalatinoBig);
        titleLabel.setForeground(branding.maroon);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Version info
        JLabel versionLabel = new JLabel("Version 1.0");
        versionLabel.setFont(branding.sizedFontPalatinoSmall);
        versionLabel.setForeground(branding.darkermaroon);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Developer panel
        JPanel devPanel = new JPanel();
        devPanel.setLayout(new BoxLayout(devPanel, BoxLayout.Y_AXIS));
        devPanel.setBackground(branding.lightgray);
        devPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        
        JLabel devHeaderLabel = new JLabel("Developed by:");
        devHeaderLabel.setFont(new Font("Roboto", Font.BOLD, 14));
        devHeaderLabel.setForeground(branding.darkermaroon);
        devHeaderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add developers with their roles
        String[] developers = {
            "Sean Harvey Bantanos - Database Architect",
            "Mac Darren Louis Calimba - Frontend Developer",
            "Norman Enrico Eulin - Frontend Developer",
            "Rolf Genree Garces - Backend Developer",
            "Jhun Kenneth Iniego - Backend Developer",
            "Jade Eric Petilla - Database Architect",
            "Gian Angelo Tongzon - Frontend Developer"
        };
    
        devPanel.add(devHeaderLabel);
        devPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    
        for (String dev : developers) {
            JLabel devLabel = new JLabel(dev);
            devLabel.setFont(new Font("Roboto", Font.PLAIN, 10));
            devLabel.setForeground(branding.darkermaroon);
            devLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            devPanel.add(devLabel);
            devPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
    
        // Add everything to content panel
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(versionLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(devPanel);
    
        // Close button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(branding.lightgray);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
    
        JButton backButton = new JButton("Back");
        backButton.setBackground(branding.maroon);
        backButton.setForeground(branding.white);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> aboutDialog.dispose());
    
        buttonPanel.add(backButton);
    
        // Add panels to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    
        // Add main panel to dialog
        aboutDialog.add(mainPanel);
        aboutDialog.pack();
        aboutDialog.setSize(800, 525); // Set appropriate size
        aboutDialog.setLocationRelativeTo(mainFrame); // Center on main frame
        aboutDialog.setVisible(true);
    }

    private void showPanel(JPanel panel) {
        mainPanel.contentPanel.removeAll();
        mainPanel.contentPanel.add(panel);
        mainPanel.contentPanel.revalidate();
        mainPanel.contentPanel.repaint();
    }

    private void showMainMenu() {
        mainPanel.contentPanel.removeAll();
        mainPanel.contentPanel.add(mainPanel.menuButtonsPanel);
        mainPanel.contentPanel.revalidate();
        mainPanel.contentPanel.repaint();
    }
}
