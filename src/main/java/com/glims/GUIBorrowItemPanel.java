package com.glims;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class GUIBorrowItemPanel extends JPanel{
    private CardLayout cardLayout;
    private Branding branding;
    private JPanel screen1, screen2, screen3;
    private JButton selectedCategoryButton = null;
    private JPanel scrn2BorrowedItemsContentPanel, borrowerInfoInputPanel;
    private JPanel equipmentPanel, categoryPanelContents; // Declare equipmentPanel as an instance variable
    private JButton addToBasketButton, continueButton; // Track the Add To Basket button
    private JButton mainBackButton;
    private JButton formsAddBorrowerBtn, formsRemoveBorrowerBtn;
    private Queries queries = new Queries();
    private ImageStorage imageStorage;
    private Controller ctrl;
    private int currentCategoryID = -1;
    // Data structure to keep track of items in the basket
    private List<BasketItem> basketItems = new ArrayList<>();
    private Map<Integer, BasketItem> basketItemsMap = new HashMap<>();
    private Map<String, JPanel> itemPanelsMap = new HashMap<>();
    
    // Data structure to keep item panels for each category
    private Map<Integer, JPanel> categoryGridPanels = new HashMap<>();
    private Set<JPanel> selectedItemCards = new HashSet<>();

    // Lists to store all borrower components for later data retrieval
    private List<JPanel> borrowerPanels;
    List<JTextField> studentNumberFields;
    List<JTextField> fullNameFields, emailAddressFields, contactNumberFields;
    List<JComboBox<String>> degreeProgramComboBoxes;

    // Data to store Course Information Options
    private String[] courseData = {"Select a Course"};
    private String[] sectionData = {"Select a Section"};
    private String[] instructorData = {"Select an Instructor"};
    private JComboBox<String> courseOptions;
    private JComboBox<String> sectionOptions;
    private JComboBox<String> instructorOptions;

    // Class to represent an item in the basket
    private static class BasketItem {
        int category;
        int itemID;
        int itemQuantity;
        
        public BasketItem(int category, int itemID) {
            this.category = category;
            this.itemID = itemID;
            this.itemQuantity = 1;
        }
        
        @Override
        public String toString() {
            return "Category: " + category + ", ItemID: " + itemID + ", Quantity: " + itemQuantity;
        }

        public int getItemID(){
            return itemID;
        }

        public int getCategoryID(){
            return category;
        }
    }
    
    public GUIBorrowItemPanel(Controller ctrl, Branding branding, JButton backButton) {
        this.ctrl = ctrl;
        this.branding = branding;
        this.mainBackButton = backButton;
        this.cardLayout = new CardLayout();
        this.imageStorage = new ImageStorage();

        this.setLayout(cardLayout);
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setPreferredSize(new Dimension(900, 500));
        
        // Create main content panel
        initializeScreen1(backButton);
        initializeScreen2();
        initializeScreen3();
        add(screen1, "Panel 1");
        add(screen2, "Panel 2");
        add(screen3, "Panel 3");
        
    }

    // ========== SCREEN 1 ==========

    private void initializeScreen1(JButton backButton) {
        // Main panel with border layout
        screen1 = new JPanel(new BorderLayout());
        screen1.setBorder(BorderFactory.createLineBorder(branding.maroon, 2));
        
        // Create content panel with border layout
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createLineBorder(branding.maroon, 1));
        
        // Create and add category panel to the left side
        JPanel categoryPanel = createCategoryPanel();
        contentPanel.add(categoryPanel, BorderLayout.WEST);
        
        // Create equipment display panel
        equipmentPanel = createEquipmentPanel(backButton);
        contentPanel.add(equipmentPanel, BorderLayout.CENTER);
        
        screen1.add(contentPanel, BorderLayout.CENTER);
        
        // Show the initial "No Category is Selected" message
        showNoCategorySelectedMessage();
    }

    private JPanel createCategoryPanel() {
        // The panel that contains the category buttons (BoxLayout for vertical stack)
        categoryPanelContents = new JPanel();
        categoryPanelContents.setLayout(new BoxLayout(categoryPanelContents, BoxLayout.Y_AXIS));
        categoryPanelContents.setBackground(branding.maroon);
        categoryPanelContents.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        categoryPanelContents.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel categoryPanel = new JPanel(new GridBagLayout());
        categoryPanel.setBackground(branding.maroon);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        categoryPanel.add(categoryPanelContents, gbc);
        categoryPanel.setPreferredSize(new Dimension(220, 0)); // Force fixed width like before
        return categoryPanel;
    }

    public void loadCategoryPanel(List<String[]> categoryList) {
        categoryPanelContents.removeAll();
        ctrl.updateLoadingStatus("Initializing User Interface");
        int steps = categoryList.size(); int currentStep = 1; int progress = 0;
        int panelCount = 1;
        for (String[] category : categoryList) {
            System.out.printf("Loading Panel (%d/%d)\n",  panelCount++, categoryList.size());
            int categoryId = Integer.parseInt(category[0]);
            String categoryName = category[1];

            JButton categoryButton = new JButton(categoryName);
            categoryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            categoryButton.setPreferredSize(new Dimension(200, 40));
            categoryButton.setMaximumSize(new Dimension(200, 40));
            categoryButton.setMinimumSize(new Dimension(200, 40));
            categoryButton.setBackground(branding.lightergray);
            categoryButton.setForeground(branding.maroon);
            categoryButton.setFont(new Font("Arial", Font.PLAIN, 14));
            categoryButton.setFocusPainted(false);
            categoryButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            categoryButton.setMargin(new Insets(0, 0, 0, 0));

            categoryButton.putClientProperty("categoryId", categoryId);
            
            JPanel gridPanel = createGridPanelForCategory(categoryId);
            categoryGridPanels.put(categoryId, gridPanel);

            // Hover effects
            categoryButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    categoryButton.setBackground(branding.lightgray);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (getSelectedCategoryButton() != categoryButton) {
                        categoryButton.setBackground(branding.lightergray);
                    }
                }
            });

            // Action listener
            categoryButton.addActionListener(e -> {
                JButton currentSelectedButton = getSelectedCategoryButton();
                if (currentSelectedButton != null) {
                    currentSelectedButton.setBackground(branding.lightergray);
                }

                categoryButton.setBackground(branding.gray);
                setSelectedCategoryButton(categoryButton);

                int selectedCategoryId = (int) categoryButton.getClientProperty("categoryId");
                setCurrentCategoryID(selectedCategoryId);

                showCategoryGridPanel(selectedCategoryId);
            });

            categoryPanelContents.add(categoryButton);
            categoryPanelContents.add(Box.createVerticalStrut(10));
            
            progress = 33 + (currentStep * 33) / steps; currentStep++; // from 33% - 63%
            ctrl.updateLoadingProgress(progress);
        }

        categoryPanelContents.revalidate();
        categoryPanelContents.repaint();
    }
    
    private JPanel createGridPanelForCategory(int categoryId) {
        JPanel gridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));

        String[] categoryItems = ctrl.getItemsInCategory(categoryId);
        int cardWidth = 140, cardHeight = 140;
        for (String itemName : categoryItems) {
            JPanel itemCard = createItemCard(itemName, categoryId);
            gridPanel.add(itemCard);
        }

        return gridPanel;
    }
    
    private void showCategoryGridPanel(int categoryId) {
        if (equipmentPanel == null) return;

        JPanel containerPanel = (JPanel) equipmentPanel.getClientProperty("gridPanel");
        if (containerPanel == null) return;

        containerPanel.removeAll();

        JPanel selectedGridPanel = categoryGridPanels.get(categoryId);
        if (selectedGridPanel != null) {
            containerPanel.add(selectedGridPanel, BorderLayout.CENTER);
        }

        containerPanel.revalidate();
        containerPanel.repaint();
    }

    private JPanel createItemCard(String itemName, int categoryId) {
        JPanel itemCard = new JPanel();
        itemCard.setLayout(new BoxLayout(itemCard, BoxLayout.Y_AXIS));
        itemCard.setBackground(Color.WHITE);
        itemCard.setBorder(BorderFactory.createLineBorder(branding.maroon, 1));
        itemCard.setPreferredSize(new Dimension(140, 140));

        // Get the full item name with unit from the Controller
        int itemID = ctrl.getItemIDWithUnit(itemName);
        String fullItemName = ctrl.getItemNameWithUnit(itemID); // Includes unit if applicable
        String imagePath = imageStorage.getImagePath(fullItemName);

        // Create image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(80, 80));
        imagePanel.setMaximumSize(new Dimension(80, 80));
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (imagePath != null) {
            try {
                // Load and resize image
                BufferedImage originalImage = ImageIO.read(new File(imagePath));

                // Calculate dimensions to maintain aspect ratio
                int targetWidth = 80;
                int targetHeight = 80;

                double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
                int scaledWidth, scaledHeight;

                if (aspectRatio > 1) { // Wider than tall
                    scaledWidth = targetWidth;
                    scaledHeight = (int) (targetWidth / aspectRatio);
                } else { // Taller than wide or square
                    scaledHeight = targetHeight;
                    scaledWidth = (int) (targetHeight * aspectRatio);
                }

                Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

                JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                imageLabel.setHorizontalAlignment(JLabel.CENTER);

                imagePanel.add(imageLabel, BorderLayout.CENTER);

            } catch (IOException e) {
                JLabel placeholderLabel = new JLabel("No Image");
                placeholderLabel.setHorizontalAlignment(JLabel.CENTER);
                placeholderLabel.setForeground(Color.GRAY);
                imagePanel.add(placeholderLabel, BorderLayout.CENTER);
            }
        } else {
            JLabel placeholderLabel = new JLabel("No Image");
            placeholderLabel.setHorizontalAlignment(JLabel.CENTER);
            placeholderLabel.setForeground(Color.GRAY);
            imagePanel.add(placeholderLabel, BorderLayout.CENTER);
        }

        JLabel nameLabel = new JLabel(fullItemName, JLabel.CENTER);
        nameLabel.setForeground(branding.maroon);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        itemCard.add(Box.createVerticalStrut(10));
        itemCard.add(imagePanel);
        itemCard.add(Box.createVerticalStrut(5));
        itemCard.add(nameLabel);
        itemCard.add(javax.swing.Box.createVerticalGlue());

        // Store full item name (including unit) as a client property
        itemCard.putClientProperty("itemName", fullItemName);
        itemCard.putClientProperty("category", ctrl.getCategoryName(categoryId));

        // Add click and hover listeners
        itemCard.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                boolean isInBasket = basketItemsMap.containsKey(itemID);
                if (isInBasket) return; // Do not allow selection if it's already in basket
                if (selectedItemCards.contains(itemCard)) {
                    itemCard.setBackground(Color.WHITE);
                    selectedItemCards.remove(itemCard);
                } else {
                    itemCard.setBackground(Color.GRAY);
                    selectedItemCards.add(itemCard);
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return itemCard;
    }

    private void showNoCategorySelectedMessage() {
        if (equipmentPanel == null) return;
        
        // Get the grid panel from the equipment panel
        JPanel gridPanel = (JPanel) equipmentPanel.getClientProperty("gridPanel");
        if (gridPanel == null) return;
        
        // Clear the grid panel
        gridPanel.removeAll();
        
        // Create a message panel
        JPanel messagePanel = new JPanel(new GridBagLayout());
        messagePanel.setBackground(branding.lightgray);
        
        JLabel messageLabel = new JLabel("No Category is Selected");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setForeground(branding.maroon);
        
        messagePanel.add(messageLabel);
        
        // Add the message panel to the grid panel
        gridPanel.setLayout(new BorderLayout());
        gridPanel.add(messagePanel, BorderLayout.CENTER);
        
        // Refresh the panel
        gridPanel.revalidate();
        gridPanel.repaint();
    }
    
    private JPanel createEquipmentPanel(JButton backButton) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(branding.lightgray);
    
        // Layout with button panel at the bottom
        JPanel itemsContainerPanel = new JPanel(new BorderLayout());
        itemsContainerPanel.setBackground(branding.lightgray);
        
        // Create scrollable grid panel
        JPanel gridPanel = new JPanel(new GridLayout(0, 4, 20, 20)); // 0 rows = auto, 4 columns fixed
        gridPanel.setBackground(branding.lightgray);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Scroll pane with vertical-only scrolling
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        branding.reskinScrollBar(scrollPane, branding.gray);
        
        itemsContainerPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(itemsContainerPanel, BorderLayout.CENTER);
        
        // Add button panel at the bottom
        JPanel buttonPanel = createButtonPanel(backButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Store the gridPanel as a property of the main panel for later access
        mainPanel.putClientProperty("gridPanel", gridPanel);
    
        return mainPanel;
    }
    
    private JPanel createButtonPanel(JButton backButton) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 30));
        buttonPanel.setBackground(branding.lightgray);
        
        // Ensure the back button is visible and has appropriate size
        backButton.setText("Go Back");
        addToBasketButton = new JButton("Add To Basket");
        continueButton = new JButton("Continue");
        
        styleActionButton(backButton);
        styleActionButton(addToBasketButton);
        styleActionButton(continueButton);
        
        // Add action listener to the Add To Basket button
        addToBasketButton.addActionListener(e -> {
            if (!selectedItemCards.isEmpty()) {
                for (JPanel card : new HashSet<>(selectedItemCards)) {
                    String itemName = (String) card.getClientProperty("itemName");
                    System.out.println("Selected item: " + itemName);
                    
                    int itemID = ctrl.getItemIDWithUnit(itemName);
                    System.out.println("Item ID: " + itemID);
                    String category = (String) card.getClientProperty("category");
                    int categoryID = ctrl.getCategoryID(category);

                    if (!basketItemsMap.containsKey(itemID)) {
                        BasketItem newItem = new BasketItem(categoryID, itemID);
                        System.out.println("Selected Item: " + itemName);
                        basketItems.add(newItem);
                        basketItemsMap.put(itemID, newItem);
            
                        card.setBackground(branding.yellow);
                    }
                    selectedItemCards.remove(card); // Deselect after adding
                }
            
                // Print basket
                System.out.println("\n--- Current Basket Contents ---");
                for (int i = 0; i < basketItems.size(); i++) {
                    System.out.println((i + 1) + ". " + basketItems.get(i));
                }
                System.out.println("-----------------------------\n");
            }
        });

        continueButton.addActionListener(e -> {
            if (basketItems.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "You must add at least one item to the basket before continuing.",
                    "No Items in Basket",
                    JOptionPane.WARNING_MESSAGE
                );
            } else {
                updateBasketDisplayPanel();
                cardLayout.next(GUIBorrowItemPanel.this);
            }
        });
        
        // Ensure all buttons are added properly to the button panel
        buttonPanel.add(backButton);
        buttonPanel.add(addToBasketButton);
        buttonPanel.add(continueButton);
        
        return buttonPanel;
    }
    

    // ========== SCREEN 2 ==========

    private void initializeScreen2() {
        screen2 = new JPanel();
        screen2.setLayout(new GridBagLayout());
        screen2.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        screen2.setBackground(branding.maroon);
    
        scrn2BorrowedItemsContentPanel = new JPanel();
        scrn2BorrowedItemsContentPanel.setLayout(new GridBagLayout()); // Set to vertical stack
        scrn2BorrowedItemsContentPanel.setBackground(branding.maroon);
        scrn2BorrowedItemsContentPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
    
    
        JPanel scrn2BasketLabelPanel = new JPanel();
        scrn2BasketLabelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrn2BasketLabelPanel.setLayout(new GridBagLayout());
        scrn2BasketLabelPanel.setOpaque(false);
        
        JLabel scrn2BasketLabel = new JLabel("Borrow Basket");
        scrn2BasketLabel.setFont(new Font("Roboto", Font.PLAIN, 20));
        scrn2BasketLabel.setForeground(branding.white);
        
        scrn2BasketLabelPanel.add(scrn2BasketLabel);
        
    
        JScrollPane scrn2ScrollContentPanel = new JScrollPane(scrn2BorrowedItemsContentPanel);
        scrn2ScrollContentPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrn2ScrollContentPanel.getVerticalScrollBar().setUnitIncrement(16);
        scrn2ScrollContentPanel.setBackground(branding.maroon);
        scrn2ScrollContentPanel.setBorder(null);
        branding.reskinScrollBar(scrn2ScrollContentPanel, branding.maroon);
    
        JPanel scrn2MenuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        scrn2MenuPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 33));
        scrn2MenuPanel.setBackground(branding.lightgray);
        scrn2MenuPanel.setOpaque(true);
    
        JButton screen2BackBtn = new JButton("Go Back");
        JButton screen2ContinueBtn = new JButton("Continue");
    
        screen2BackBtn.setPreferredSize(new Dimension(150, 30));
        screen2BackBtn.setBackground(branding.maroon);
        screen2BackBtn.setForeground(branding.white);
        screen2BackBtn.addActionListener(e -> {
            cardLayout.previous(GUIBorrowItemPanel.this);
        });
    
        screen2ContinueBtn.setPreferredSize(new Dimension(150, 30));
        screen2ContinueBtn.setBackground(branding.maroon);
        screen2ContinueBtn.setForeground(branding.white);
        screen2ContinueBtn.addActionListener(e -> {
            cardLayout.next(GUIBorrowItemPanel.this);
        });
    
        scrn2MenuPanel.add(screen2BackBtn);
        scrn2MenuPanel.add(screen2ContinueBtn);
    
        GridBagConstraints screen2GBC = new GridBagConstraints();
        screen2GBC.fill = GridBagConstraints.BOTH;
        screen2GBC.weightx = 1;
        screen2GBC.weighty = 0.01;
        screen2GBC.gridy = 0;
        screen2.add(scrn2BasketLabelPanel, screen2GBC);
        screen2GBC.weighty = 0.9;
        screen2GBC.gridy++;
        screen2.add(scrn2ScrollContentPanel, screen2GBC);
        screen2GBC.weighty = 0.01;
        screen2GBC.gridy++;
        screen2.add(scrn2MenuPanel, screen2GBC);
    }

    private void updateBasketDisplayPanel() {
        // Clear the panel before rebuilding it
        scrn2BorrowedItemsContentPanel.removeAll();

        GridBagConstraints scrn2BorrowedItemsContentPanelGBC = new GridBagConstraints();
        scrn2BorrowedItemsContentPanelGBC.fill = GridBagConstraints.BOTH;
        scrn2BorrowedItemsContentPanelGBC.anchor = GridBagConstraints.NORTH;
        scrn2BorrowedItemsContentPanelGBC.insets = new Insets(10,0,0,0);
        scrn2BorrowedItemsContentPanelGBC.gridy = -1;
        scrn2BorrowedItemsContentPanelGBC.weightx = 1;
        
        // Empty the item panels map as we're rebuilding it
        itemPanelsMap.clear();
        
        // Add each item in the basket to the panel
        for (BasketItem item : basketItems) {

            int maxQty = Integer.MAX_VALUE;
            String[][] categoryItems = ctrl.getItemsPerCategory(item.category);
            for (String[] row : categoryItems) {
                int id = Integer.parseInt(row[0]); // item_id
                if (id == item.itemID) {
                    maxQty = Integer.parseInt(row[3]); // available quantity
                    break;
                }
            }
            final int finalMaxQty = maxQty; // workaround for lambda access
            JLabel itemLabel = new JLabel(ctrl.getItemNameWithUnit(item.itemID));
            System.out.println("Item Label: " + itemLabel.getText());
            JLabel quantityLabel = new JLabel(String.valueOf(item.itemQuantity));
            
            // Set styling
            itemLabel.setForeground(branding.maroon);
            quantityLabel.setForeground(branding.maroon);
            quantityLabel.setFont(new Font("Arial", Font.BOLD, 16));

            JPanel quantityLabelPanel = new JPanel(new GridBagLayout());
            quantityLabelPanel.setPreferredSize(new Dimension(50, 35));
            quantityLabelPanel.add(quantityLabel);

            
            // Create buttons
            JButton addBtn = new JButton("+");
            addBtn.setPreferredSize(new Dimension(50, 35));
            addBtn.setBackground(branding.maroon);
            addBtn.setForeground(branding.white);

            // Disable add button initially if already at or above max quantity
            if (item.itemQuantity >= maxQty) {
                addBtn.setEnabled(false);
            }
            
            JButton subtractBtn = new JButton("-");
            subtractBtn.setPreferredSize(new Dimension(50, 35));
            subtractBtn.setBackground(branding.maroon);
            subtractBtn.setForeground(branding.white);
            
            // Create panels
            JPanel itemPanel = new JPanel();
            JPanel quantityPanel = new JPanel();
            
            itemPanel.setLayout(new BorderLayout());
            quantityPanel.setLayout(new GridBagLayout());
            
            itemPanel.setPreferredSize(new Dimension(10, 70));
            quantityPanel.setPreferredSize(new Dimension(50, 70));
            
            itemPanel.setOpaque(false);
            quantityPanel.setOpaque(false);
            
            itemPanel.add(itemLabel, BorderLayout.WEST);
            
            // Add components to quantity panel
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            //gbc.weightx = 0.5;
            gbc.insets = new Insets(0, 5, 0, 5);
            gbc.gridx = 0;
            gbc.ipadx = 0;
            quantityPanel.add(subtractBtn, gbc);
            gbc.gridx = 1;
            gbc.ipadx = 10;
            quantityPanel.add(quantityLabelPanel, gbc);
            gbc.gridx = 2;
            gbc.ipadx = 0;
            quantityPanel.add(addBtn, gbc);
            
            // Add button listeners
            final int itemID = item.itemID; // Capture the item name for the lambda expressions
            
            // Add button listener
            addBtn.addActionListener(e -> {
                BasketItem basketItem = basketItemsMap.get(itemID);
                if (basketItem != null && basketItem.itemQuantity < finalMaxQty) {
                    basketItem.itemQuantity++;
                    quantityLabel.setText(String.valueOf(basketItem.itemQuantity));

                    if (basketItem.itemQuantity >= finalMaxQty) {
                        addBtn.setEnabled(false);
                    }
                    subtractBtn.setEnabled(true);
                }
            });
            
            // Subtract button listener
            subtractBtn.addActionListener(e -> {
                BasketItem basketItem = basketItemsMap.get(itemID);
                if (basketItem != null) {
                    if (basketItem.itemQuantity > 1) {
                        basketItem.itemQuantity--;
                        quantityLabel.setText(String.valueOf(basketItem.itemQuantity));

                        if (basketItem.itemQuantity < finalMaxQty) {
                            addBtn.setEnabled(true);
                        }
                    } else {
                        basketItems.remove(basketItem);
                        basketItemsMap.remove(itemID);
                        updateBasketDisplayPanel();
                        refreshEquipmentItemBackground(ctrl.getItemNameWithUnit(itemID));
                    }
                }
            });
            
            // Create and add the item row panel
            JPanel tupleInfoPanel = new JPanel();
            tupleInfoPanel.setMaximumSize(new Dimension(900, 70));
            tupleInfoPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 20));
            tupleInfoPanel.setLayout(new GridBagLayout());
            tupleInfoPanel.setBackground(branding.lightgray);
            
            GridBagConstraints tupleInfoPanelGBC = new GridBagConstraints();
            tupleInfoPanelGBC.fill = GridBagConstraints.HORIZONTAL;
            tupleInfoPanelGBC.gridx = 0;
            tupleInfoPanelGBC.ipadx = 20;
            tupleInfoPanelGBC.weightx = 0.5;
            tupleInfoPanel.add(itemPanel, tupleInfoPanelGBC);
            
            tupleInfoPanelGBC.fill = GridBagConstraints.NONE;
            tupleInfoPanelGBC.anchor = GridBagConstraints.EAST;
            tupleInfoPanelGBC.gridx++;
            tupleInfoPanelGBC.ipadx = 100;
            tupleInfoPanelGBC.weightx = 0.2;
            tupleInfoPanel.add(quantityPanel, tupleInfoPanelGBC);
            
            // Store the panel in the map
            itemPanelsMap.put(ctrl.getItemName(itemID), tupleInfoPanel);
            
            // Add the panel to the content panel
            scrn2BorrowedItemsContentPanelGBC.gridy++;
            scrn2BorrowedItemsContentPanel.add(tupleInfoPanel, scrn2BorrowedItemsContentPanelGBC);
            
            // Add a small gap between items (only if this isn't the last item)
            if (basketItems.indexOf(item) < basketItems.size() - 1) {
                scrn2BorrowedItemsContentPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        // Update the UI
        scrn2BorrowedItemsContentPanel.revalidate();
        scrn2BorrowedItemsContentPanel.repaint();

        Component[] components = screen2.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel panel) {
                for (Component innerComp : panel.getComponents()) {
                    if (innerComp instanceof JButton button && "Continue".equals(button.getText())) {
                        button.setEnabled(!basketItems.isEmpty());
                    }
                }
            }
        }
    }
    
    private void refreshEquipmentItemBackground(String itemNameWithUnit) {
        System.out.println("REFRESH EQUIPMENT ITEMS BACKGROUND");

        // Check if the category panels map is initialized
        if (categoryGridPanels == null || categoryGridPanels.isEmpty()) {
            System.out.println("Category grid panels map is empty.");
            return;
        }

        // Iterate through all category panels
        for (Map.Entry<Integer, JPanel> entry : categoryGridPanels.entrySet()) {
            int categoryId = entry.getKey();
            JPanel gridPanel = entry.getValue();

            if (gridPanel == null) {
                System.out.println("Grid panel for category " + categoryId + " is null.");
                continue;
            }

            // Iterate through all components in the grid panel
            for (Component component : gridPanel.getComponents()) {
                if (component instanceof JPanel itemCard) {
                    // Retrieve the full item name with unit stored in the item's client properties
                    String cardItemNameWithUnit = (String) itemCard.getClientProperty("itemName");
                    

                    if (cardItemNameWithUnit == null) {
                        System.out.println("Item card name with unit is null in category " + categoryId + ".");
                        continue;
                    }

                    // Reset the background color if the card matches the given item name with unit
                    if (itemNameWithUnit.equals(cardItemNameWithUnit)) {
                        itemCard.setBackground(Color.WHITE);
                        itemCard.revalidate();
                        itemCard.repaint();
                    }
                }
            }

            // Ensure the grid panel reflects the changes
            gridPanel.revalidate();
            gridPanel.repaint();
        }
    }

    // ========== SCREEN 3 ==========

    private void initializeScreen3(){
        screen3 = new JPanel();
        screen3.setLayout(new GridBagLayout());
        screen3.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        screen3.setBackground(branding.maroon);
    
        JPanel scrn3BorrowerInformationFormsPanel = new JPanel();
        scrn3BorrowerInformationFormsPanel.setLayout(new GridBagLayout());
        scrn3BorrowerInformationFormsPanel.setBackground(branding.maroon);
        scrn3BorrowerInformationFormsPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
    
        JPanel formsHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel formsHeaderLabel = new JLabel("Borrower(s) Information");
        formsHeaderLabel.setFont(new Font("Roboto", Font.PLAIN, 20));
        formsHeaderLabel.setForeground(branding.white);
        formsHeaderPanel.add(formsHeaderLabel);
        formsHeaderPanel.setOpaque(false);
    
        JPanel formsBorrowerInfoInputPanel = new JPanel();
        formsBorrowerInfoInputPanel.setLayout(new GridBagLayout());
        formsBorrowerInfoInputPanel.setPreferredSize(new Dimension(700, 150));
        formsBorrowerInfoInputPanel.setBorder(BorderFactory.createLineBorder(branding.white, 1));
        formsBorrowerInfoInputPanel.setOpaque(false);
    
        JLabel formStudentNumberLabel = new JLabel("Student Number");
        JLabel formFullNameLabel = new JLabel("Full Name");
        JLabel formEmailAddressLabel = new JLabel("Email Address");
        JLabel formContactNumberLabel = new JLabel("Contact Number");
        JLabel formDegreeProgramLabel = new JLabel("Degree Program");
    
        formStudentNumberLabel.setForeground(branding.white);
        formFullNameLabel.setForeground(branding.white);
        formEmailAddressLabel.setForeground(branding.white);
        formContactNumberLabel.setForeground(branding.white);
        formDegreeProgramLabel.setForeground(branding.white);
        
        JPanel borrowerInfoInputHeaderPanel = new JPanel(new GridBagLayout());
        borrowerInfoInputHeaderPanel.setPreferredSize(new Dimension(800, 20));
        borrowerInfoInputHeaderPanel.setOpaque(false);

        GridBagConstraints borrowerInfoInputHeaderPanelGBC = new GridBagConstraints();
        borrowerInfoInputHeaderPanelGBC.fill = GridBagConstraints.NONE;
        borrowerInfoInputHeaderPanelGBC.anchor = GridBagConstraints.WEST;
        borrowerInfoInputHeaderPanelGBC.gridx = 0;
        borrowerInfoInputHeaderPanelGBC.weightx = 0.010;
        borrowerInfoInputHeaderPanel.add(formStudentNumberLabel, borrowerInfoInputHeaderPanelGBC);
        borrowerInfoInputHeaderPanelGBC.gridx++;
        borrowerInfoInputHeaderPanelGBC.weightx = 0.042;
        borrowerInfoInputHeaderPanel.add(formFullNameLabel, borrowerInfoInputHeaderPanelGBC);
        borrowerInfoInputHeaderPanelGBC.gridx++;
        borrowerInfoInputHeaderPanelGBC.weightx = 0.0354;
        borrowerInfoInputHeaderPanel.add(formEmailAddressLabel, borrowerInfoInputHeaderPanelGBC);
        borrowerInfoInputHeaderPanelGBC.gridx++;
        borrowerInfoInputHeaderPanelGBC.weightx = 0.0145;
        borrowerInfoInputHeaderPanel.add(formContactNumberLabel, borrowerInfoInputHeaderPanelGBC);
        borrowerInfoInputHeaderPanelGBC.gridx++;
        borrowerInfoInputHeaderPanelGBC.weightx = 0.04;
        borrowerInfoInputHeaderPanel.add(formDegreeProgramLabel, borrowerInfoInputHeaderPanelGBC);
    
        // Create a panel to hold all borrower info panels
        borrowerInfoInputPanel = new JPanel();
        borrowerInfoInputPanel.setLayout(new BoxLayout(borrowerInfoInputPanel, BoxLayout.Y_AXIS));
        borrowerInfoInputPanel.setBackground(branding.maroon);
    
        // Lists to store all borrower components for later data retrieval
        borrowerPanels = new ArrayList<>();
        studentNumberFields = new ArrayList<>();
        fullNameFields = new ArrayList<>();
        emailAddressFields = new ArrayList<>();
        contactNumberFields = new ArrayList<>();
        degreeProgramComboBoxes = new ArrayList<>();
        
        // Create first borrower panel and add it
        JPanel firstBorrowerPanel = createBorrowerInfoPanel(
            studentNumberFields, fullNameFields, emailAddressFields, 
            contactNumberFields, degreeProgramComboBoxes
        );
        borrowerPanels.add(firstBorrowerPanel);
        borrowerInfoInputPanel.add(firstBorrowerPanel);
    
        JScrollPane borrowerInfoInputScrollPanel = new JScrollPane(borrowerInfoInputPanel);
        borrowerInfoInputScrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        borrowerInfoInputScrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        borrowerInfoInputScrollPanel.getVerticalScrollBar().setUnitIncrement(16);
        borrowerInfoInputScrollPanel.setBackground(branding.maroon);
        borrowerInfoInputScrollPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, branding.maroon));
        branding.reskinScrollBar(borrowerInfoInputScrollPanel, branding.maroon);
        
        GridBagConstraints formsBorrowerInfoInputPanelGBC = new GridBagConstraints();
        formsBorrowerInfoInputPanelGBC.fill = GridBagConstraints.BOTH;
        formsBorrowerInfoInputPanelGBC.weightx = 1;
        formsBorrowerInfoInputPanelGBC.weighty = 0.001;
        formsBorrowerInfoInputPanelGBC.gridy = 0;
        formsBorrowerInfoInputPanel.add(borrowerInfoInputHeaderPanel, formsBorrowerInfoInputPanelGBC);
        formsBorrowerInfoInputPanelGBC.weighty = 0.8;
        formsBorrowerInfoInputPanelGBC.gridy++;
        formsBorrowerInfoInputPanel.add(borrowerInfoInputScrollPanel, formsBorrowerInfoInputPanelGBC);

        JPanel formsAddRemoveBorrowerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        formsAddRemoveBorrowerPanel.setOpaque(false);

        formsAddBorrowerBtn = new JButton("Add More Borrower");
        formsAddBorrowerBtn.setPreferredSize(new Dimension(165, 25));
        formsAddBorrowerBtn.setBackground(branding.lightergray);
        formsAddBorrowerBtn.setForeground(branding.maroon);

        formsRemoveBorrowerBtn = new JButton("Remove Borrower");
        formsRemoveBorrowerBtn.setPreferredSize(new Dimension(165, 25));
        formsRemoveBorrowerBtn.setBackground(branding.lightergray);
        formsRemoveBorrowerBtn.setForeground(branding.maroon);

        formsRemoveBorrowerBtn.setEnabled(false);
        
        // Add action listener to the Add More Borrower button
        
        JPanel formsCourseInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JLabel formsCourseInfoLabel = new JLabel("Course Information");
        formsCourseInfoLabel.setFont(new Font("Roboto", Font.PLAIN, 20));
        formsCourseInfoLabel.setForeground(branding.white);
        formsCourseInfoPanel.add(formsCourseInfoLabel);
        formsCourseInfoPanel.setOpaque(false);
    
        JPanel formsCourseInfoInputPanel = new JPanel();
        formsCourseInfoInputPanel.setPreferredSize(new Dimension(700, 60));
        formsCourseInfoInputPanel.setBorder(BorderFactory.createLineBorder(branding.white, 1));
        formsCourseInfoInputPanel.setBackground(branding.maroon);
        formsCourseInfoInputPanel.setLayout(new GridBagLayout());
        formsCourseInfoInputPanel.setOpaque(false);

        JPanel coursInfCoursePanel = new JPanel();
        JPanel coursInfSectionPanel = new JPanel();
        JPanel coursInfInstructorPanel = new JPanel();

        coursInfCoursePanel.setLayout(new GridBagLayout());
        coursInfSectionPanel.setLayout(new GridBagLayout());
        coursInfInstructorPanel.setLayout(new GridBagLayout());

        coursInfCoursePanel.setOpaque(false);
        coursInfSectionPanel.setOpaque(false);
        coursInfInstructorPanel.setOpaque(false);

        JLabel coursInfCourseLabel  = new JLabel("Course");
        JLabel coursInfSectionLabel = new JLabel("Section");
        JLabel coursInfInstructorLabel = new JLabel("Instructor");
        coursInfCourseLabel.setForeground(branding.white);
        coursInfSectionLabel.setForeground(branding.white);
        coursInfInstructorLabel.setForeground(branding.white);

        courseOptions = new JComboBox<>(courseData);
        courseOptions.setMaximumRowCount(4);
    
        sectionOptions = new JComboBox<>(sectionData);
        sectionOptions.setMaximumRowCount(4);
    
        instructorOptions = new JComboBox<>(instructorData);
        instructorOptions.setMaximumRowCount(4);

        updateCourseOptions();

        courseOptions.addActionListener(e -> {
            String selectedCourse = (String) courseOptions.getSelectedItem();
            if (selectedCourse != null && !selectedCourse.equals("Select a Course")) {
                updateSectionOptions(selectedCourse);
            } else {
                // Reset to default if no course selected
                resetSectionOptions();
                resetInstructorOptions();
            }
        });

        sectionOptions.addActionListener(e -> {
            String selectedCourse = (String) courseOptions.getSelectedItem();
            String selectedSection = (String) sectionOptions.getSelectedItem();
            if (selectedCourse != null && selectedSection != null &&
                !selectedCourse.equals("Select a Course") && !selectedSection.equals("Select a Section")) {
                updateInstructorOptions(selectedCourse, selectedSection);
            } else {
                // Reset to default if no section selected
                resetInstructorOptions();
            }
        });

            // Add to panels
        coursInfCoursePanel.add(coursInfCourseLabel);
        coursInfCoursePanel.add(courseOptions);

        coursInfSectionPanel.add(coursInfSectionLabel);
        coursInfSectionPanel.add(sectionOptions);

        coursInfInstructorPanel.add(coursInfInstructorLabel);
        coursInfInstructorPanel.add(instructorOptions);
        formsAddBorrowerBtn.addActionListener(e -> {
            addBorrowerPanel();
            // Scroll to the bottom to show the new panel
            SwingUtilities.invokeLater(() -> {
                JScrollBar verticalScrollBar = borrowerInfoInputScrollPanel.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getMaximum());
            });
        });

        formsRemoveBorrowerBtn.addActionListener(e -> {
            if (borrowerPanels.size() > 1) {
                // Get the index of the last borrower
                int lastIndex = borrowerPanels.size() - 1;
                
                // Remove the last panel from the visual container
                borrowerInfoInputPanel.remove(borrowerPanels.get(lastIndex));
                
                // Remove from our tracking lists
                borrowerPanels.remove(lastIndex);
                studentNumberFields.remove(lastIndex);
                fullNameFields.remove(lastIndex);
                emailAddressFields.remove(lastIndex);
                contactNumberFields.remove(lastIndex);
                degreeProgramComboBoxes.remove(lastIndex);
                
                // If we now have only one borrower, disable the remove button
                if (borrowerPanels.size() <= 1) {
                    formsRemoveBorrowerBtn.setEnabled(false);
                }
                
                // Re-enable the add button since we're below the maximum
                formsAddBorrowerBtn.setEnabled(true);
                
                // Update the UI
                borrowerInfoInputPanel.revalidate();
                borrowerInfoInputPanel.repaint();
                formsAddBorrowerBtn.requestFocusInWindow();
                SwingUtilities.invokeLater(() -> {
                    courseOptions.updateUI();
                    sectionOptions.updateUI();
                    instructorOptions.updateUI();
                });
            }
        });
        
        formsAddRemoveBorrowerPanel.add(formsAddBorrowerBtn);
        formsAddRemoveBorrowerPanel.add(formsRemoveBorrowerBtn);

        GridBagConstraints coursPanelGBC = new GridBagConstraints();
        coursPanelGBC.anchor = GridBagConstraints.WEST;
        coursPanelGBC.fill = GridBagConstraints.HORIZONTAL;
        coursPanelGBC.ipadx = 100;
        coursPanelGBC.gridy = 0;
        coursInfCoursePanel.add(coursInfCourseLabel, coursPanelGBC);
        coursPanelGBC.gridy = 1;
        coursInfCoursePanel.add(courseOptions, coursPanelGBC);

        coursPanelGBC.gridy = 0;
        coursInfSectionPanel.add(coursInfSectionLabel, coursPanelGBC);
        coursPanelGBC.gridy = 1;
        coursInfSectionPanel.add(sectionOptions, coursPanelGBC);

        coursPanelGBC.gridy = 0;
        coursInfInstructorPanel.add(coursInfInstructorLabel, coursPanelGBC);
        coursPanelGBC.gridy = 1;
        coursInfInstructorPanel.add(instructorOptions, coursPanelGBC);

        GridBagConstraints formsCourseInfoInputPanelGBC = new GridBagConstraints();
        formsCourseInfoInputPanelGBC.anchor = GridBagConstraints.WEST;
        formsCourseInfoInputPanelGBC.insets = new Insets(10,10,10,0);
        formsCourseInfoInputPanelGBC.weightx = 0.05;
        formsCourseInfoInputPanelGBC.gridx = 0;
        formsCourseInfoInputPanel.add(coursInfCoursePanel, formsCourseInfoInputPanelGBC);
        formsCourseInfoInputPanelGBC.weightx = 0.05;
        formsCourseInfoInputPanelGBC.gridx++;
        formsCourseInfoInputPanel.add(coursInfSectionPanel, formsCourseInfoInputPanelGBC);
        formsCourseInfoInputPanelGBC.weightx = 0.2;
        formsCourseInfoInputPanelGBC.gridx++;
        formsCourseInfoInputPanel.add(coursInfInstructorPanel, formsCourseInfoInputPanelGBC);

        GridBagConstraints scrn3BorrowerInformationFormsPanelGBC = new GridBagConstraints();
        scrn3BorrowerInformationFormsPanelGBC.fill = GridBagConstraints.HORIZONTAL;
        scrn3BorrowerInformationFormsPanelGBC.anchor = GridBagConstraints.WEST;
        scrn3BorrowerInformationFormsPanelGBC.weightx = 1;
        scrn3BorrowerInformationFormsPanelGBC.weighty = 1;
        scrn3BorrowerInformationFormsPanelGBC.gridy = 0;
        scrn3BorrowerInformationFormsPanel.add(formsHeaderPanel, scrn3BorrowerInformationFormsPanelGBC);
        scrn3BorrowerInformationFormsPanelGBC.weighty = 1;
        scrn3BorrowerInformationFormsPanelGBC.gridy++;
        scrn3BorrowerInformationFormsPanel.add(formsBorrowerInfoInputPanel, scrn3BorrowerInformationFormsPanelGBC);
        scrn3BorrowerInformationFormsPanelGBC.weighty = 1;
        scrn3BorrowerInformationFormsPanelGBC.gridy++;
        scrn3BorrowerInformationFormsPanel.add(formsAddRemoveBorrowerPanel, scrn3BorrowerInformationFormsPanelGBC);
        scrn3BorrowerInformationFormsPanelGBC.weighty = 1;
        scrn3BorrowerInformationFormsPanelGBC.gridy++;
        scrn3BorrowerInformationFormsPanel.add(formsCourseInfoPanel, scrn3BorrowerInformationFormsPanelGBC);
        scrn3BorrowerInformationFormsPanelGBC.weighty = 1;
        scrn3BorrowerInformationFormsPanelGBC.gridy++;
        scrn3BorrowerInformationFormsPanel.add(formsCourseInfoInputPanel, scrn3BorrowerInformationFormsPanelGBC);
    
        JScrollPane scrn3ScrollContentPanel = new JScrollPane(scrn3BorrowerInformationFormsPanel);
        scrn3ScrollContentPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrn3ScrollContentPanel.getVerticalScrollBar().setUnitIncrement(16);
        scrn3ScrollContentPanel.setBackground(branding.maroon);
        scrn3ScrollContentPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, branding.maroon));
        branding.reskinScrollBar(scrn3ScrollContentPanel, branding.maroon);
    
        JPanel scrn3MenuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        scrn3MenuPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 33));
        scrn3MenuPanel.setBackground(branding.lightgray);
    
        JButton screen3BackBtn = new JButton("Go Back");
        JButton screen3BorrowBtn = new JButton("Borrow Now");
    
        screen3BackBtn.setPreferredSize(new Dimension(150, 30));
        screen3BackBtn.setBackground(branding.maroon);
        screen3BackBtn.setForeground(branding.white);
        screen3BackBtn.addActionListener(e -> {
            cardLayout.previous(GUIBorrowItemPanel.this);
        });
    
        screen3BorrowBtn.setPreferredSize(new Dimension(150, 30));
        screen3BorrowBtn.setBackground(branding.maroon);
        screen3BorrowBtn.setForeground(branding.white);
        

        screen3BorrowBtn.addActionListener(e -> {
            if (ctrl.validateBorrowerInfoInputs(studentNumberFields, fullNameFields, emailAddressFields, contactNumberFields, degreeProgramComboBoxes)) {
                List<String> studentNumbers = new ArrayList<>();
                List<String> fullNames = new ArrayList<>();
                List<String> emails = new ArrayList<>();
                List<String> contactNumbers = new ArrayList<>();
                List<String> degreePrograms = new ArrayList<>();
                for (int i = 0; i < studentNumberFields.size(); i++) {
                    studentNumbers.add(studentNumberFields.get(i).getText());
                    fullNames.add(fullNameFields.get(i).getText());
                    emails.add(emailAddressFields.get(i).getText());
                    contactNumbers.add(contactNumberFields.get(i).getText());
                    degreePrograms.add(degreeProgramComboBoxes.get(i).getSelectedItem().toString());
                }

                List<Integer> itemIds = new ArrayList<>();
                List<Integer> quantities = new ArrayList<>();
                for (BasketItem item : basketItems) {
                    itemIds.add(item.getItemID());
                    quantities.add(item.itemQuantity);
                }

                String course = (String) courseOptions.getSelectedItem();
                String sectionName = (String) sectionOptions.getSelectedItem();
                int sectionID = ctrl.getSectionID(sectionName);

                Controller.BorrowCheckoutResult result = ctrl.processBorrowCheckout(
                        studentNumbers, fullNames, emails, contactNumbers, degreePrograms,
                        course, sectionID, itemIds, quantities);

                if (result.success) {
                    Timestamp ts = new Timestamp(System.currentTimeMillis());
                    ts.setNanos(0);
                    ctrl.generateBorrowReceipt(result.borrowId, ts, studentNumberFields, fullNameFields,
                            ctrl.getBorrowedItemsInfo(result.borrowId));
                    JOptionPane.showMessageDialog(
                    GUIBorrowItemPanel.this,
                    "Transaction Successful!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                    );
                    // Reset borrower input fields
                    for (JTextField field : studentNumberFields) field.setText("");
                    for (JTextField field : fullNameFields) field.setText("");
                    for (JTextField field : emailAddressFields) field.setText("");
                    for (JTextField field : contactNumberFields) field.setText("");
                    for (JComboBox<String> box : degreeProgramComboBoxes) box.setSelectedIndex(0);
                    courseOptions.setSelectedIndex(0);
                    sectionOptions.setSelectedIndex(0);
                    instructorOptions.setSelectedIndex(0);
                    basketItems.clear();
                    resetPanel();
                    ctrl.refreshCachedData();
                    mainBackButton.doClick();
                }else{
                    JOptionPane.showMessageDialog(
                    GUIBorrowItemPanel.this,
                    "Transaction Unsuccessful! " + result.message,
                    "Failed",
                    JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });
        
        scrn3MenuPanel.add(screen3BackBtn);
        scrn3MenuPanel.add(screen3BorrowBtn);
    
        GridBagConstraints screen3GBC = new GridBagConstraints();
        screen3GBC.fill = GridBagConstraints.BOTH;
        screen3GBC.weightx = 1;
        screen3GBC.gridy = 0;
        screen3GBC.weighty = 0.8;
        screen3.add(scrn3ScrollContentPanel, screen3GBC);
        screen3GBC.weighty = 0.1;
        screen3GBC.gridy++;
        screen3.add(scrn3MenuPanel, screen3GBC);
    }

    public void updateCourseOptions() {
        // Clear current items, keeping only the default first item
        courseOptions.removeAllItems();
        courseOptions.addItem("Select a Course");
        
        // Get course data from controller instead of queries
        String[] courseOptionsData = ctrl.getCourseOptions();
        
        // Skip first item which is already "Select a Course"
        for (int i = 1; i < courseOptionsData.length; i++) {
            courseOptions.addItem(courseOptionsData[i]);
        }
        
        // Reset dependent dropdowns
        resetSectionOptions();
        resetInstructorOptions();
    }

    private void autocompleteBorrowerInfo(
        String studentNumber,
        int index,
        List<JTextField> fullNameFields,
        List<JTextField> emailAddressFields,
        List<JTextField> contactNumberFields,
        List<JComboBox<String>> degreeProgramComboBoxes) {
    
    // Call the controller to get borrower info
    List<String[]> borrowerInfo = ctrl.getBorrowerInfo(studentNumber);
    
    if (!borrowerInfo.isEmpty()) {
        // Get the first result (assuming student IDs are unique)
        String[] borrowerData = borrowerInfo.get(0);
        
        // Update the fields with the retrieved data
        fullNameFields.get(index).setText(borrowerData[0]);
        emailAddressFields.get(index).setText(borrowerData[1]);
        contactNumberFields.get(index).setText(borrowerData[2]);
        
        // For the degree program combo box, select the matching option
        String degreeProgram = borrowerData[3];
        JComboBox<String> programComboBox = degreeProgramComboBoxes.get(index);
        
        for (int i = 0; i < programComboBox.getItemCount(); i++) {
            if (programComboBox.getItemAt(i).equals(degreeProgram)) {
                programComboBox.setSelectedIndex(i);
                break;
            }
        }
    } else {
        fullNameFields.get(index).setText("");
        emailAddressFields.get(index).setText("");
        contactNumberFields.get(index).setText("");
        degreeProgramComboBoxes.get(index).setSelectedIndex(0);
    }
}
    public void updateInstructorOptions(String selectedCourse, String selectedSection) {
        // Clear current items, keeping only the default first item
        instructorOptions.removeAllItems();
        //instructorOptions.addItem("Select an Instructor");
        // Get instructor data from controller instead of queries
        String[] instructorOptionsData = ctrl.getInstructorOptions(selectedCourse, selectedSection);
        instructorOptions.addItem(instructorOptionsData[1]);
    }

    public void updateSectionOptions(String selectedCourse) {
        // Clear current items, keeping only the default first item
        sectionOptions.removeAllItems();
        sectionOptions.addItem("Select a Section");
        
        // Get fresh section data from queries
        String[] sectionOptionsData = ctrl.getSectionOptions(selectedCourse);
        
        // Add new items
        for (int i = 1; i < sectionOptionsData.length; i++) {
        sectionOptions.addItem(sectionOptionsData[i]);
        }
        
        // Reset instructor dropdown
        resetInstructorOptions();
    }

    private void resetSectionOptions() {
        sectionOptions.removeAllItems();
        sectionOptions.addItem("Select a Section");
    }

    private void resetInstructorOptions() {
        instructorOptions.removeAllItems();
        instructorOptions.addItem("Select an Instructor");
    }

    public void refreshFormDropdowns() {
        // Refresh course data
        updateCourseOptions();
        
        // This will cascade and reset section and instructor dropdowns as well
        SwingUtilities.invokeLater(() -> {
            courseOptions.updateUI();
            sectionOptions.updateUI();
            instructorOptions.updateUI();
        });
    }

    private JPanel createBorrowerInfoPanel(
        List<JTextField> studentNumberFields,
        List<JTextField> fullNameFields,
        List<JTextField> emailAddressFields,
        List<JTextField> contactNumberFields,
        List<JComboBox<String>> degreeProgramComboBoxes) {

        JPanel panel = new JPanel();
        panel.setMaximumSize(new Dimension(800, 25));
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    
        JTextField studentNumberTextField = new JTextField(11);
        JTextField fullNameTextField = new JTextField(16);
        JTextField emailAddressTextField = new JTextField(14);
        JTextField contactNumberTextField = new JTextField(12);

        String[] degreeProgram = {"Select an Option", "BS in Biology", "BS in Computer Science", "BS in Applied Mathematics"};
        JComboBox<String> degreeProgramOptions = new JComboBox<>(degreeProgram);
        
        studentNumberTextField.addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
            String studentNumber = studentNumberTextField.getText().trim();
            if (!studentNumber.isEmpty()) {
                int index = studentNumberFields.indexOf(studentNumberTextField);
                autocompleteBorrowerInfo(
                    studentNumber,
                    index,
                    fullNameFields,
                    emailAddressFields,
                    contactNumberFields,
                    degreeProgramComboBoxes
                );
            }
        }
    });

        // Add all components to respective lists for later data retrieval
        studentNumberFields.add(studentNumberTextField);
        fullNameFields.add(fullNameTextField);
        emailAddressFields.add(emailAddressTextField);
        contactNumberFields.add(contactNumberTextField);
        degreeProgramComboBoxes.add(degreeProgramOptions);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 4);
        gbc.gridx = 0;
        gbc.weighty = 1;
        gbc.weightx = 0.011;
        panel.add(studentNumberTextField, gbc);
        gbc.weightx = 0.014;
        gbc.gridx++;
        panel.add(fullNameTextField, gbc);
        gbc.weightx = 0.015;
        gbc.gridx++;
        panel.add(emailAddressTextField, gbc);
        gbc.weightx = 0.0125;
        gbc.gridx++;
        panel.add(contactNumberTextField, gbc);
        gbc.weightx = 0.0001;
        gbc.gridx++;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel.add(degreeProgramOptions, gbc);

        return panel;
    }

    // =========== MISCELLANEOUS ==========
    
    public void resetPanel() {
        resetAllEquipmentItemBackgrounds();
        if (selectedCategoryButton != null) {
            selectedCategoryButton.setBackground(branding.lightergray);
        }
        selectedCategoryButton = null;
        selectedItemCards.clear();
        basketItems.clear();
        basketItemsMap.clear();
        itemPanelsMap.clear();
        borrowerPanels.clear();
        studentNumberFields.clear();
        fullNameFields.clear();
        emailAddressFields.clear();
        contactNumberFields.clear();
        degreeProgramComboBoxes.clear();
        borrowerInfoInputPanel.removeAll();
        addBorrowerPanel();
        formsRemoveBorrowerBtn.setEnabled(false); // Disable remove since only 1 remains
        formsAddBorrowerBtn.setEnabled(true);    // Enable add
        
        borrowerInfoInputPanel.revalidate();
        borrowerInfoInputPanel.repaint();
    
        showNoCategorySelectedMessage();
        this.revalidate();
        this.repaint();
        cardLayout.show(this, "Panel 1");
    }

    private void resetAllEquipmentItemBackgrounds() {
        System.out.println("RESET ALL EQUIPMENT ITEMS BACKGROUND");

        if (categoryGridPanels == null || categoryGridPanels.isEmpty()) {
            System.out.println("Category grid panels map is empty.");
            return;
        }

        for (Map.Entry<Integer, JPanel> entry : categoryGridPanels.entrySet()) {
            int categoryId = entry.getKey();
            JPanel gridPanel = entry.getValue();

            if (gridPanel == null) {
                System.out.println("Grid panel for category " + categoryId + " is null.");
                continue;
            }

            for (Component component : gridPanel.getComponents()) {
                if (component instanceof JPanel itemCard) {
                    itemCard.setBackground(Color.WHITE);
                    itemCard.revalidate();
                    itemCard.repaint();
                }
            }

            gridPanel.revalidate();
            gridPanel.repaint();
        }
    }
    
    //helper method for formsAddBorrowerBtn and resetPanel;
    private void addBorrowerPanel() {
        JPanel newBorrowerPanel = createBorrowerInfoPanel(
            studentNumberFields, fullNameFields, emailAddressFields, 
            contactNumberFields, degreeProgramComboBoxes
        );
        borrowerPanels.add(newBorrowerPanel);
        borrowerInfoInputPanel.add(newBorrowerPanel);
        borrowerInfoInputPanel.revalidate();
        borrowerInfoInputPanel.repaint();
        
        formsRemoveBorrowerBtn.setEnabled(borrowerPanels.size() > 1);
        formsAddBorrowerBtn.setEnabled(borrowerPanels.size() < 7);
    }
    
    private void styleActionButton(JButton button) {
        button.setPreferredSize(new Dimension(150, 30));
        button.setBackground(branding.maroon);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
    
    // Custom Box class to replace javax.swing.Box
    private static class Box {
        public static Component createVerticalStrut(int height) {
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setPreferredSize(new Dimension(0, height));
            return panel;
        }
    }

    public JButton getSelectedCategoryButton() {
        return selectedCategoryButton;
    }

    public void setSelectedCategoryButton(JButton selectedCategoryButton) {
        this.selectedCategoryButton = selectedCategoryButton;
    }

    public int getCurrentCategoryID() {
        return currentCategoryID;
    }

    public void setCurrentCategoryID(int currentCategoryID) {
        this.currentCategoryID = currentCategoryID;
    }

}