package com.glims;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class GUIUpdateInventoryPanel extends JPanel {
    private Branding branding;
    private JPanel mainContentPanel, categoryPanel;
    private JButton selectedCategoryButton = null, removeItemButton, addItemButton;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JPanel inventoryPanel; // Panel to hold the inventory table
    private Controller ctrl;
    private int selectedCategoryIndexDatabase, selectedCategoryIndexArray;
    private JButton backButton;
    private GUIBorrowItemPanel borrowItemPanel;

    // Categories of laboratory equipment
    private String[] categories;

    public GUIUpdateInventoryPanel(Controller ctrl, Branding branding, JButton backButton, GUIBorrowItemPanel borrowItemPanel) {
        this.ctrl = ctrl;
        this.branding = branding;
        this.backButton = backButton;
        this.borrowItemPanel = borrowItemPanel;
        
        // Set up panel properties
        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setPreferredSize(new Dimension(900, 500));
        
        // Create the panel structure without loading data
        createPanelStructure();
    }
    
    private void createPanelStructure() {
        // Create main content panel with border layout
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBorder(BorderFactory.createLineBorder(branding.maroon, 2));
        
        // Create content panel with border layout
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createLineBorder(branding.maroon, 1));
        
        // Create category panel structure
        categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBackground(branding.maroon);
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        categoryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Wrapper panel to center the category panel vertically
        JPanel centeredPanel = new JPanel(new GridBagLayout());
        centeredPanel.setBackground(branding.maroon);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        centeredPanel.add(categoryPanel, gbc);
        centeredPanel.setPreferredSize(new Dimension(220, 0)); // Force fixed width
        
        // Create the inventory display panel structure
        inventoryPanel = createItemInventoryPanel();
        
        // Add panels to the content panel
        contentPanel.add(centeredPanel, BorderLayout.WEST);
        contentPanel.add(inventoryPanel, BorderLayout.CENTER);
        
        // Add content panel to main panel
        mainContentPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Add main content panel to this panel
        add(mainContentPanel, BorderLayout.CENTER);
    }
    

    public void loadContents() {
        // Get categories from the controller
        loadCategories();
        
        // Initialize category buttons
        loadCategoryButtons();
        
        // Create button panel with action listeners
        JPanel buttonPanel = createButtonPanel();
        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Configure initial state - show "No Category Selected" message
        showNoCategorySelectedMessage();
        
        // Set up add/remove item buttons based on database state
        refreshAddItemButton();
        
        // Refresh the panel
        revalidate();
        repaint();
    }
    

    private void loadCategories() {
        List<String[]> categoryList = ctrl.getCategoryList();
        categories = new String[categoryList.size()];
        
        for (int i = 0; i < categoryList.size(); i++) {
            categories[i] = categoryList.get(i)[1]; // Category name is at index 1
        }
    }
    

    private void loadCategoryButtons() {
        categoryPanel.removeAll();
        
        for (int i = 0; i < categories.length; i++) {
            final int categoryIndex = i;
            JButton categoryButton = new JButton(categories[i]);
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
    
            // Hover and selection effect
            categoryButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    categoryButton.setBackground(branding.lightgray);
                }
    
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (selectedCategoryButton != categoryButton) {
                        categoryButton.setBackground(branding.lightergray);
                    }
                }
            });
            
            // Action listener to update inventory panel when a category is selected
            categoryButton.addActionListener(e -> {
                if (selectedCategoryButton != null) {
                    selectedCategoryButton.setBackground(branding.lightergray);
                }
                categoryButton.setBackground(branding.gray);
                selectedCategoryButton = categoryButton;
                
                // Update inventory display with items from the selected category
                selectedCategoryIndexDatabase = ctrl.getCategoryID(categories[categoryIndex]);
                selectedCategoryIndexArray = categoryIndex;
                System.out.println("Selected Category ID from Database: " + selectedCategoryIndexDatabase);
                System.out.println("Selected Category ID from Array: " + selectedCategoryIndexArray);
                updateInventoryPanel(selectedCategoryIndexDatabase, categoryIndex);
            });
    
            categoryPanel.add(categoryButton);
            categoryPanel.add(Box.createVerticalStrut(10));
        }
        
        categoryPanel.revalidate();
        categoryPanel.repaint();
    }
    
    private JPanel createItemInventoryPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(branding.lightgray);
    
        JPanel tableContainerPanel = new JPanel(new BorderLayout());
        tableContainerPanel.setBackground(branding.lightgray);
        
        // Scroll pane with vertical-only scrolling
        JScrollPane scrollPane = new JScrollPane(tableContainerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        branding.reskinScrollBar(scrollPane, branding.gray);
    
        // Add components
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.putClientProperty("tableContainerPanel", tableContainerPanel);
    
        return mainPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBackground(branding.lightgray);
        
        // Configure the back button
        backButton.setText("Go Back");
        styleActionButton(backButton);
        
        // Create and style action buttons
        removeItemButton = new JButton("Remove Item");
        addItemButton = new JButton("Add Item");
        
        styleActionButton(removeItemButton);
        styleActionButton(addItemButton);
        
        // Add action listener for Remove Item button
        removeItemButton.addActionListener(e -> removeSelectedItem());
        
        buttonPanel.add(backButton);
        buttonPanel.add(removeItemButton);
        buttonPanel.add(addItemButton);
        
        return buttonPanel;
    }
    
    // Method to show "No Category is Selected" message
    private void showNoCategorySelectedMessage() {
        if (inventoryPanel == null) return;
        
        // Get the table container panel
        JPanel tableContainerPanel = (JPanel) inventoryPanel.getClientProperty("tableContainerPanel");
        if (tableContainerPanel == null) return;
        
        // Clear the panel
        tableContainerPanel.removeAll();
        
        // Create a message panel
        JPanel messagePanel = new JPanel(new GridBagLayout());
        messagePanel.setBackground(branding.lightgray);
        
        JLabel messageLabel = new JLabel("No Category is Selected");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 18));
        messageLabel.setForeground(branding.maroon);
        
        messagePanel.add(messageLabel);
        
        // Add the message panel
        tableContainerPanel.add(messagePanel, BorderLayout.CENTER);
        
        // Refresh the panel
        tableContainerPanel.revalidate();
        tableContainerPanel.repaint();
    }

    private void printItemDetails(int categoryId) {
        String[][] items = ctrl.getItemsPerCategory(categoryId);
        System.out.println("Items in selected category:");
        for (String[] item : items) {
            // item[1] is Item Name, item[3] is Quantity
            System.out.println("- " + item[1] + ": " + item[3]);
        }
        System.out.println(); // Empty line for better readability
    }
    
    // Method to update inventory panel based on selected category
    private void updateInventoryPanel(int categoryIndexDatabase, int categoryIndex) {
        if (inventoryPanel == null) return;
        
        // Get the table container panel
        JPanel tableContainerPanel = (JPanel) inventoryPanel.getClientProperty("tableContainerPanel");
        if (tableContainerPanel == null) return;
        
        // Clear the panel
        tableContainerPanel.removeAll();
        
        // Create table panel with a border
        JPanel tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBackground(branding.lightgray);
        Border lineBorder = BorderFactory.createLineBorder(branding.maroon, 5);
        Border emptyBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20);
        CompoundBorder compoundBorder = new CompoundBorder(emptyBorder, lineBorder);
        tablePanel.setBorder(compoundBorder);

        // Get data for selected category using controller
        String[][] data = ctrl.getItemsPerCategory(categoryIndexDatabase);

        // Column names
        String[] columnNames = {"Item Name", "Unit", "Quantity"};

        // Transform data to exclude item_id (index 0)
        String[][] trimmedData = new String[data.length][3];
        for (int i = 0; i < data.length; i++) {
            trimmedData[i][0] = data[i][1]; // Item Name
            trimmedData[i][1] = data[i][2]; // Unit
            trimmedData[i][2] = data[i][3]; // Quantity
        }

        // Create table model with trimmed data
        tableModel = new DefaultTableModel(trimmedData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column ==2; // or false depending on your requirement
            }
        };

        // Create JTable with the table model
        inventoryTable = new JTable(tableModel);
        inventoryTable.getModel().addTableModelListener(e -> {
            System.out.println("SUMULOD HEREEEEE");
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 2) {
                int row = e.getFirstRow();
                
                // Add null check before calling toString()
                Object quantityValue = tableModel.getValueAt(row, 2);
                if (quantityValue == null) {
                    JOptionPane.showMessageDialog(
                        GUIUpdateInventoryPanel.this,
                        "Quantity cannot be empty.",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE
                    );
                    // Reset to previous value
                    tableModel.setValueAt(data[row][3], row, 2);
                    return;
                }
                
                String quantityStr = quantityValue.toString();
                
                // Get item name and unit with null checks
                Object itemNameObj = tableModel.getValueAt(row, 0);
                Object unitObj = tableModel.getValueAt(row, 1);
                
                if (itemNameObj == null) {
                    JOptionPane.showMessageDialog(
                        GUIUpdateInventoryPanel.this,
                        "Item name is missing.",
                        "Invalid Data",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                
                String itemName = itemNameObj.toString();
                String unit = (unitObj != null) ? unitObj.toString() : "";
                
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity <= 0) {
                        JOptionPane.showMessageDialog(
                            GUIUpdateInventoryPanel.this,
                            "Quantity must be greater than zero.",
                            "Invalid Input",
                            JOptionPane.WARNING_MESSAGE
                        );
                        // Reset to previous value
                        tableModel.setValueAt(data[row][3], row, 2);
                    } else {
                        // Get the item ID
                        String itemNameWithUnit = (unit == null || unit.trim().isEmpty()) 
                            ? itemName 
                            : itemName + " - " + unit;
                        int itemId = ctrl.getItemIDWithUnit(itemNameWithUnit);
                        // Update the quantity in database
                        ctrl.updateItemQuantity(itemId, quantity);

                        // Notify user
                        JOptionPane.showMessageDialog(
                            GUIUpdateInventoryPanel.this,
                            "Quantity updated successfully.",
                            "Update Successful",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                        GUIUpdateInventoryPanel.this,
                        "Please enter a valid number for quantity.",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE
                    );
                    // Reset to previous value
                    tableModel.setValueAt(data[row][3], row, 2);
                }
            }
        });

        inventoryTable.setRowHeight(50);
        inventoryTable.setForeground(branding.maroon);
        inventoryTable.setBackground(branding.lightgray);

        GridBagConstraints tablePanelGBC = new GridBagConstraints();
        tablePanelGBC.fill = GridBagConstraints.BOTH;
        tablePanelGBC.weightx = 1;
        tablePanelGBC.ipady = 20;
        tablePanelGBC.gridy = 0;
        tablePanel.add(inventoryTable.getTableHeader(), tablePanelGBC); 
        tablePanelGBC.weighty = 1;
        tablePanelGBC.ipady = 0;
        tablePanelGBC.gridy = 1;
        tablePanel.add(inventoryTable, tablePanelGBC);  
        
        // Add the table panel to the container
        tableContainerPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Add a header label showing which category is selected
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(branding.lightgray);
        JLabel headerLabel = new JLabel(categories[categoryIndex] + " Inventory");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel.setForeground(branding.maroon);
        headerPanel.add(headerLabel);
        tableContainerPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Refresh the panel
        tableContainerPanel.revalidate();
        tableContainerPanel.repaint();
        printItemDetails(selectedCategoryIndexDatabase);
    }

    public void refreshAddItemButton() {
        if (ctrl.getNoOfItems() != 0) {
            addItemButton.setText("Add Item");
            
            // Clear previous action listeners
            for (ActionListener listener : addItemButton.getActionListeners()) {
                addItemButton.removeActionListener(listener);
            }
            
            addItemButton.addActionListener(e -> addNewItem());
            removeItemButton.setEnabled(true);
        } else {
            removeItemButton.setEnabled(false);
            addItemButton.setText("Import From File");
            
            // Clear previous action listeners
            for (ActionListener listener : addItemButton.getActionListeners()) {
                addItemButton.removeActionListener(listener);
            }
            
            addItemButton.addActionListener(e -> {
                boolean success = importItemsFromCSV();
                if (success) {
                    removeItemButton.setEnabled(true);
                    resetAddItemButtonToAddMode();
                    JOptionPane.showMessageDialog(
                    this,
                    "Import successful.",
                    "Successful Import",
                    JOptionPane.INFORMATION_MESSAGE
                    );
                    borrowItemPanel.loadCategoryPanel(ctrl.getCategoryList());
                } else {
                    JOptionPane.showMessageDialog(
                    this,
                    "Import unsuccessful.",
                    "Unsuccessful Import",
                    JOptionPane.WARNING_MESSAGE
                    );
                }
            });
        }
    }

    private void resetAddItemButtonToAddMode() {
        for (ActionListener listener : addItemButton.getActionListeners()) {
            addItemButton.removeActionListener(listener);
        }
        addItemButton.setText("Add Item");
        addItemButton.addActionListener(e -> addNewItem());
    }

    private boolean importItemsFromCSV() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);

            int result = chooser.showOpenDialog(this);

            if(result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                String filepath = selectedFile.getAbsolutePath();
                System.out.println("Selected file: " + filepath);
                
                String[][] data = parseCSVFile(selectedFile);
                
                if (data != null && data.length > 0) {
                    ctrl.importItemsFromCSV(filepath, data);
                    
                    if (selectedCategoryButton != null) {
                        updateInventoryPanel(selectedCategoryIndexDatabase, selectedCategoryIndexArray);
                    }
                    
                    return true;
                }
            } else {
                JOptionPane.showMessageDialog(null, "Opening file terminated.", "Terminated", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Error importing file: " + e.getMessage(),
                "Import Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        return false;
    }
    
    private String[][] parseCSVFile(File file) {
        try {
            java.io.BufferedReader br1 = new java.io.BufferedReader(new java.io.FileReader(file));
            String line = "";
            int numItems = 0;
            while ((line = br1.readLine()) != null) {
                numItems++;
            }
            br1.close();

            String[][] data = new String[numItems][5];
            int rowsIndex = 0;
            java.io.BufferedReader br2 = new java.io.BufferedReader(new java.io.FileReader(file));
            while ((line = br2.readLine()) != null) {
                String[] values = line.split(",");
                data[rowsIndex][0] = values[1]; // category_id
                data[rowsIndex][1] = values[2]; // item_name
                data[rowsIndex][2] = values[3]; // unit
                data[rowsIndex][3] = values[4]; // quantity
                data[rowsIndex][4] = values[5]; // ???
                rowsIndex++;
            }
            br2.close();
            
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void removeSelectedItem() {
        if (selectedCategoryButton == null) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a category first.",
                "No Category Selected",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow != -1) {
            String itemName = (String) tableModel.getValueAt(selectedRow, 0); // Item Name
            String unit = (String) tableModel.getValueAt(selectedRow, 1);     // Unit

            String itemNameWithUnit = (unit == null || unit.trim().isEmpty())
                ? itemName
                : itemName + " - " + unit;

            int itemId = ctrl.getItemIDWithUnit(itemNameWithUnit);
            if (itemId == -1) {
                JOptionPane.showMessageDialog(
                    this,
                    "Item could not be found in the database.",
                    "Invalid Item",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove '" + itemName + "' from the inventory database?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                ctrl.removeItemFromDatabase(itemId);
                updateInventoryPanel(selectedCategoryIndexDatabase, selectedCategoryIndexArray);
                JOptionPane.showMessageDialog(
                    this,
                    "Item '" + itemName + "' has been removed from inventory.",
                    "Item Removed",
                    JOptionPane.INFORMATION_MESSAGE
                );
                borrowItemPanel.loadCategoryPanel(ctrl.getCategoryList());
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Please select an item to remove.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    
    private void addNewItem() {
        // Check if a category is selected
        if (selectedCategoryButton == null) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a category first.",
                "No Category Selected",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // Request user input for item details
        String[] input = new String[3];
        String[] labels = {"Item Name", "Unit", "Quantity"};
        
        // Collect input for each field
        for (int i = 0; i < 3; i++) {
            boolean validInput = false;
            while (!validInput) {
                input[i] = JOptionPane.showInputDialog(
                    this,
                    "Enter " + labels[i] + ":",
                    "Add New Item",
                    JOptionPane.QUESTION_MESSAGE
                );
                
                // User cancelled
                if (input[i] == null) {
                    return;
                }
                
                // Validate input
                if (input[i].trim().isEmpty()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "You must enter a value for " + labels[i] + ".",
                        "Input Required",
                        JOptionPane.WARNING_MESSAGE
                    );
                } else {
                    validInput = true;
                }
            }
        }
        
        // Validate quantity
        try {
            int qty = Integer.parseInt(input[2]);
            
            if (qty > 0) {
                // Use controller to add the item
                ctrl.addItemToDatabase(input[0], input[1], qty, selectedCategoryIndexDatabase);
                
                // Refresh the display
                updateInventoryPanel(selectedCategoryIndexDatabase, selectedCategoryIndexArray);
                
                JOptionPane.showMessageDialog(
                    this,
                    "New item added successfully.",
                    "Item Added",
                    JOptionPane.INFORMATION_MESSAGE
                );
                borrowItemPanel.loadCategoryPanel(ctrl.getCategoryList());
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Quantity must be greater than zero.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Quantity must be a valid number.",
                "Invalid Input",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }
    
    private void styleActionButton(JButton button) {
        button.setPreferredSize(new Dimension(150, 30));
        button.setBackground(branding.maroon);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
    
    private static class Box {
        public static Component createVerticalStrut(int height) {
            JPanel panel = new JPanel();
            panel.setOpaque(false);
            panel.setPreferredSize(new Dimension(0, height));
            return panel;
        }
    }
}