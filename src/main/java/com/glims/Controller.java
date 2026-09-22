package com.glims;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Controller {
    private Queries queries;
        private LoadingScreen loadingScreen;

    // Data structures to use during runtime
    private List<String[]> borrowerListEntries;
    private List<String[]> allBorrowRecords;
    private List<String[]> allTransactionHistory;
    private List<String[]> categoryList;
    private List<String[]> borrowerInfo;
    private Map<Integer, List<String>> itemsByCategory; 
    private Map<Integer, Integer> itemQuantityMap;
    private Map<String, Integer> itemIDMap; // Maps item name with unit to item ID
    private Map<Integer, String[]> itemDetailsMap; // Maps itemID to [itemName, unit]
    private Map<String, String[]> courseSectionsMap; // Maps course ID to available sections
    private Map<String, Map<String, String[]>> courseInstructorsMap; // Maps course+section to available instructors
    private List<String[]> allCourses; // All courses data

    public Controller(){
        this.queries = new Queries();
        this.loadingScreen = new LoadingScreen();
        this.itemsByCategory = new HashMap<>();
        this.itemIDMap = new HashMap<>();
        this.itemDetailsMap = new HashMap<>();
        this.courseSectionsMap = new HashMap<>();
        this.courseInstructorsMap = new HashMap<>();
    }

    // ======= Loading Screen ========
    public void showLoadingScreen() {
        loadingScreen.setVisible(true);
    }

    public void hideLoadingScreen() {
        loadingScreen.setVisible(false);
        updateLoadingProgress(0);
        loadingScreen.dispose();
    }

    public void updateLoadingProgress(int progress) {
        loadingScreen.updateProgress(progress);
    }

    public void updateLoadingStatus(String status) {
        loadingScreen.updateStatus(status);
    }

    // GETS ALL INFORMATION FROM THE DATABASE AFTER LOG-IN
    public void controllerGetAllDatabaseInformationWithLoading() {
        updateLoadingStatus("Fetching Data From Database");
        int steps = 7; int currentStep = 1; int progress = 0;
        
        System.out.println("GETTING DATABASE DATA (1/8)"); // Updated count
        this.borrowerListEntries = queries.getBorrowList();
        progress = (currentStep * 33) / steps; 
        currentStep++;
        updateLoadingProgress(progress);
        
        System.out.println("GETTING DATABASE DATA (2/8)");
        this.allBorrowRecords = queries.getAllItemsBorrowed();
        progress = (currentStep * 33) / steps; 
        currentStep++;
        updateLoadingProgress(progress);
        
        System.out.println("GETTING DATABASE DATA (3/8)");
        this.allTransactionHistory = queries.getTransactionHistory();
        progress = (currentStep * 33) / steps; 
        currentStep++;
        updateLoadingProgress(progress);
        
        System.out.println("GETTING DATABASE DATA (4/8)");
        this.categoryList = queries.getAllCategories();
        progress = (currentStep * 33) / steps; 
        currentStep++;
        updateLoadingProgress(progress);
        
        System.out.println("GETTING DATABASE DATA (5/8)");
        loadData();
        progress = (currentStep * 33) / steps; 
        currentStep++;
        updateLoadingProgress(progress);
        
        System.out.println("GETTING DATABASE DATA (6/8)");
        loadItemQuantities(); // New step for loading quantities
        progress = (currentStep * 33) / steps; 
        currentStep++;
        updateLoadingProgress(progress);
        
        System.out.println("GETTING DATABASE DATA (7/8)");
        loadCourseData();
        progress = (currentStep * 33) / steps; 
        currentStep++;
        updateLoadingProgress(progress);
        
        System.out.println("GETTING DATABASE DATA (8/8)");
        System.out.println("DATABASE DATA RETRIEVED SUCCESSFULLY");
    }

    // GETS ALL INFORMATION FROM THE DATABASE AFTER LOG-IN
    public void controllerGetAllDatabaseInformation(){
            System.out.println("GETTING DATABASE DATA (1/7)");
            this.borrowerListEntries = queries.getBorrowList();
            System.out.println("GETTING DATABASE DATA (2/7)");
            this.allBorrowRecords = queries.getAllItemsBorrowed();
            System.out.println("GETTING DATABASE DATA (3/7)");
            this.allTransactionHistory = queries.getTransactionHistory();
            System.out.println("GETTING DATABASE DATA (4/7)");
            this.categoryList = queries.getAllCategories();
            System.out.println("GETTING DATABASE DATA (5/7)");
            loadData();
            System.out.println("GETTING DATABASE DATA (6/7)");
            loadCourseData();
            System.out.println("GETTING DATABASE DATA (7/7)");
            System.out.println("DATABASE DATA RETRIEVED SUCCESSFULLY");
    }

    public List<String[]> getBorrowerInfo(String borrowerID){
        this.borrowerInfo = queries.autoCompleteForm(borrowerID);
        return this.borrowerInfo;
    }
    // Cache category data for quick access
     private void loadData() {
        // Load all categories into memory
        this.categoryList = queries.getAllCategories();

        // Preload items for each category
        for (String[] category : categoryList) {
            int categoryId = Integer.parseInt(category[0]);
            String[] items = queries.getItems(categoryId);
            itemsByCategory.put(categoryId, Arrays.asList(items));

            // Populate itemIDMap for fast lookup
            for (String item : items) {
                int itemID = queries.getItemIDWithUnit(item);
                itemIDMap.put(item, itemID);

                // Populate itemDetailsMap
                String[] parts = item.split(" - ", 2); // Extract name and unit
                String itemName = parts[0].trim();
                String unit = (parts.length > 1) ? parts[1].trim() : null;
                itemDetailsMap.put(itemID, new String[]{itemName, unit});
            }
        }
    }

    private void loadItemQuantities() {
        // Initialize itemQuantityMap if not already initialized
        if (this.itemQuantityMap == null) {
            this.itemQuantityMap = new HashMap<>();
        }
        
        // For each category, get items and their quantities
        for (String[] category : categoryList) {
            int categoryId = Integer.parseInt(category[0]);
            String[][] categoryItems = queries.getItemsPerCategory(categoryId);
            
            // Store quantity for each item
            for (String[] itemData : categoryItems) {
                int itemId = Integer.parseInt(itemData[0]);
                int quantity = Integer.parseInt(itemData[3]);
                
                // Store quantity in itemQuantityMap
                itemQuantityMap.put(itemId, quantity);
            }
        }
    }

    private void loadCourseData() {
        // Load course data
        String[] courseIDs = queries.getCourseOption();
        allCourses = new ArrayList<>();
        
        // Store course data and pre-load sections for each course
        for (String courseID : courseIDs) {
            allCourses.add(new String[]{courseID});
            
            // Pre-load sections for this course
            String[] sections = queries.getSectionOption(courseID);
            courseSectionsMap.put(courseID, sections);
            
            // Initialize course-section-instructor map
            Map<String, String[]> sectionInstructorsMap = new HashMap<>();
            courseInstructorsMap.put(courseID, sectionInstructorsMap);
            
            // Pre-load instructors for each section
            for (String section : sections) {
                String[] instructors = queries.getInstructorOption(courseID, section);
                sectionInstructorsMap.put(section, instructors);
            }
        }
    }
    
    // =========================================
    // ========== Login Panel Methods ==========
    // =========================================

    public boolean controllerLogin(GUILoginPanel loginPanel, JFrame mainFrame, GUIMainPanel mainPanel, JLabel statusLabel){
        statusLabel.setText("Logging in...");
        statusLabel.setForeground(Color.BLACK);
        boolean loginSuccess = false;
        try {
            String userInput = loginPanel.lgnInputUsernameField.getText();
            char[] passInputChars = loginPanel.lgnInputPasswordField.getPassword();
            String passInput = new String(passInputChars);
            Arrays.fill(passInputChars, '\0');

            if (queries.login(userInput, passInput, mainFrame)){
                loginPanel.lgnStatusLabel.setText("Login Success");
                loginPanel.lgnStatusLabel.setForeground(Color.GREEN);
                JOptionPane.showMessageDialog(mainFrame, new JLabel("Login successful!", SwingConstants.CENTER), "Success", JOptionPane.PLAIN_MESSAGE );
                loginSuccess = true;
            } else{
                statusLabel.setText("Login Denied.\n Please Try Again.");
                statusLabel.setForeground(Color.RED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error occurred.");
            statusLabel.setForeground(Color.RED);
        }
        return loginSuccess;
    }
    
    // ========================================
    // ========== Main Panel Methods ==========
    // ========================================

    // ================================================
    // ========== Borrow Items Panel Methods ==========
    // ================================================

    public String getCategoryName(int categoryID) {
        for (String[] category : categoryList) {
            if (Integer.parseInt(category[0]) == categoryID) {
                return category[1];
            }
        }
        return null;
    }

    public String[] getItemsInCategory(int categoryID) {
        List<String> items = itemsByCategory.get(categoryID);
        return items != null ? items.toArray(new String[0]) : new String[0];
    }
    
    public int getCategoryID(String categoryName) {
        for (String[] category : categoryList) {
            if (category[1].equals(categoryName)) {
                return Integer.parseInt(category[0]);
            }
        }
        return -1;
    }

    public int getItemIDWithUnit(String itemNameWithUnit) {
        return itemIDMap.getOrDefault(itemNameWithUnit, -1);
    }

    public String getItemNameWithUnit(int itemID) {
        String[] details = itemDetailsMap.get(itemID);
        if (details == null) return null;

        String itemName = details[0];
        String unit = details[1];
        return (unit != null && !unit.isEmpty()) ? itemName + " - " + unit : itemName;
    }

    // Retrieve item name by item ID
    public String getItemName(int itemID) {
            String[] details = itemDetailsMap.get(itemID);
            return (details != null) ? details[0] : null;
    }
    
    public List<String[]> getCategoryList() {
        return categoryList;
    }

    public String[] getCourseOptions() {
        if (allCourses == null || allCourses.isEmpty()) {
            return new String[]{"Select a Course"};
        }
        
        String[] courseOptions = new String[allCourses.size() + 1];
        courseOptions[0] = "Select a Course";
        
        for (int i = 0; i < allCourses.size(); i++) {
            courseOptions[i+1] = allCourses.get(i)[0];
        }
        
        return courseOptions;
    }

    public String[] getSectionOptions(String courseID) {
        if (courseID == null || courseID.equals("Select a Course") || !courseSectionsMap.containsKey(courseID)) {
            return new String[]{"Select a Section"};
        }
        
        String[] sections = courseSectionsMap.get(courseID);
        String[] sectionOptions = new String[sections.length + 1];
        sectionOptions[0] = "Select a Section";
        
        System.arraycopy(sections, 0, sectionOptions, 1, sections.length);
        
        return sectionOptions;
    }

    public String[] getInstructorOptions(String courseID, String sectionName) {
        if (courseID == null || sectionName == null || 
            courseID.equals("Select a Course") || sectionName.equals("Select a Section") ||
            !courseSectionsMap.containsKey(courseID) || 
            !courseInstructorsMap.get(courseID).containsKey(sectionName)) {
            return new String[]{"Select an Instructor"};
        }
        
        String[] instructors = courseInstructorsMap.get(courseID).get(sectionName);
        String[] instructorOptions = new String[instructors.length + 1];
        instructorOptions[0] = "Select an Instructor";
        
        System.arraycopy(instructors, 0, instructorOptions, 1, instructors.length);
        
        return instructorOptions;
    }

    public boolean validateBorrowerInfoInputs(
            List<JTextField> studentNumberFields,
            List<JTextField> fullNameFields,
            List<JTextField> emailAddressFields,
            List<JTextField> contactNumberFields,
            List<JComboBox<String>> degreeProgramComboBoxes) {

        for (int i = 0; i < studentNumberFields.size(); i++) {
            String studentNumber = studentNumberFields.get(i).getText().trim();
            String fullName = fullNameFields.get(i).getText().trim();
            String email = emailAddressFields.get(i).getText().trim();
            String contact = contactNumberFields.get(i).getText().trim();
            String degreeProgram = (String) degreeProgramComboBoxes.get(i).getSelectedItem();

            // Validate student number: YYYY-XXXXX
            if (!studentNumber.matches("\\d{4}-\\d{5}")) {
                JOptionPane.showMessageDialog(null,
                        "Invalid student number at row " + (i + 1) + ". Format must be YYYY-XXXXX.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate email
            if (!email.matches("^[a-zA-Z0-9._%+-]+@up\\.edu\\.ph$")) {
                JOptionPane.showMessageDialog(null,
                        "Invalid email at row " + (i + 1) + ". Email must end with '@up.edu.ph'.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate contact number: 09xxxxxxxxx (11 digits)
            if (!contact.matches("^09\\d{9}$")) {
                JOptionPane.showMessageDialog(null,
                        "Invalid contact number at row " + (i + 1) + ". Must start with 09 and be 11 digits long.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Validate degree program selection
            if ("Select an Option".equals(degreeProgram)) {
                JOptionPane.showMessageDialog(null,
                        "Please select a degree program at row " + (i + 1) + ".",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Optional: check full name is not empty
            if (fullName.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Full name cannot be empty at row " + (i + 1) + ".",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true; // All inputs are valid
    }

    // Get section ID by section name
    public int getSectionID(String sectionName) {
        return queries.getSectionID(sectionName);
    }

    public static class BorrowCheckoutResult {
        public final boolean success;
        public final int borrowId;
        public final String message;

        BorrowCheckoutResult(boolean success, int borrowId, String message) {
            this.success = success;
            this.borrowId = borrowId;
            this.message = message;
        }
    }

    /**
     * Processes one checkout as a single atomic transaction: creates or
     * reuses each borrower row, then records every basket item as borrowed
     * by every borrower in the group (a lab group shares one basket), all
     * under one database-minted borrow_id. Replaces the old code path, which
     * only ever associated basket items with the first borrower row
     * (studentNumberFields.get(0)) and minted IDs via a racy
     * "SELECT MAX(borrow_id)+1".
     */
    public BorrowCheckoutResult processBorrowCheckout(
            List<String> studentNumbers,
            List<String> fullNames,
            List<String> emails,
            List<String> contactNumbers,
            List<String> degreePrograms,
            String courseID,
            int sectionID,
            List<Integer> itemIds,
            List<Integer> quantities) {

        int borrowID;
        try {
            borrowID = queries.beginBorrowTransaction();
        } catch (SQLException e) {
            return new BorrowCheckoutResult(false, -1, "Could not start transaction: " + e.getMessage());
        }

        try {
            for (int i = 0; i < studentNumbers.size(); i++) {
                Queries.BorrowerInsertResult result = queries.insertBorrowerInfo(
                        studentNumbers.get(i), fullNames.get(i), emails.get(i),
                        contactNumbers.get(i), degreePrograms.get(i));

                if (result == Queries.BorrowerInsertResult.INVALID || result == Queries.BorrowerInsertResult.FAILED) {
                    queries.rollbackBorrowTransaction();
                    return new BorrowCheckoutResult(false, -1,
                            "Could not save borrower " + studentNumbers.get(i) + " (" + result + ")");
                }
            }

            for (int i = 0; i < itemIds.size(); i++) {
                for (String studentNumber : studentNumbers) {
                    queries.borrowItem(borrowID, itemIds.get(i), studentNumber, courseID, sectionID, quantities.get(i));
                }
            }

            queries.commitBorrowTransaction();
            return new BorrowCheckoutResult(true, borrowID, "OK");
        } catch (SQLException e) {
            queries.rollbackBorrowTransaction();
            return new BorrowCheckoutResult(false, -1, e.getMessage());
        }
    }

    public List<String> getBorrowedItemsInfo(int borrowId) {
        return queries.getBorrowedItemsInfo(borrowId);
    }

    // Records a return (selectedItems: borrowId -> list of [itemId, qtyReturned] pairs).
    public void processReturn(HashMap<String, List<Integer[]>> selectedItems, Timestamp ts, String borrowerID) {
        queries.updateActualReturnDate(selectedItems, ts, borrowerID);
    }

    // Refresh cached data after transaction
    public void refreshCachedData() {
        // Refresh all the cached data structures
        this.borrowerListEntries = queries.getBorrowList();
        this.allBorrowRecords = queries.getAllItemsBorrowed();
        this.allTransactionHistory = queries.getTransactionHistory();
        loadItemQuantities();
        System.out.println("Cached data refreshed");
    }
    // =================================================
    // ========== Borrower List Panel Methods ==========
    // =================================================

    // get items borrowed by specific borrower
    public List<String[]> getItemsBorrowedByBorrower(String borrowerID) {
        List<String[]> filteredList = new ArrayList<>();

        for (String[] row : allBorrowRecords) {
            String rowBorrowerId = row[5]; // borrower_id

            // if (rowBorrowerId.equals(borrowerID) &&
            //     rowDateBorrowed.equals(dateBorrowed) &&
            //     rowExpectedReturnDate.equals(expectedReturnDate)) {
            if (rowBorrowerId.equals(borrowerID)) {
                // Convert to String[] directly
                filteredList.add(new String[] { row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7] });
            }
        }
        return filteredList;
    }


    // Getters
    public List<String[]> getBorrowerListEntries(){
        return borrowerListEntries;
    }

    // ====================================================
    // ========== Update Inventory Panel Methods ==========
    // ====================================================
    public void updateItemQuantity(int itemId, int newQuantity) {
        queries.updateItemQuantity(itemId, newQuantity);
        // Update the cached quantity
        itemQuantityMap.put(itemId, newQuantity);
    }
    
    // Get items for a specific category
    public String[][] getItemsPerCategory(int categoryId) {
        List<String> itemsWithUnit = itemsByCategory.get(categoryId);
        
        if (itemsWithUnit == null || itemsWithUnit.isEmpty()) {
            return new String[0][4]; // Return empty array if no items found
        }
        
        String[][] result = new String[itemsWithUnit.size()][4];
        
        for (int i = 0; i < itemsWithUnit.size(); i++) {
            String itemWithUnit = itemsWithUnit.get(i);
            int itemId = itemIDMap.get(itemWithUnit);
            String[] itemDetails = itemDetailsMap.get(itemId);
            Integer quantity = itemQuantityMap.get(itemId);
            
            result[i][0] = String.valueOf(itemId);                // item_id
            result[i][1] = itemDetails[0];                        // item_name
            result[i][2] = itemDetails[1];                        // unit
            result[i][3] = String.valueOf(quantity != null ? quantity : 0); // qty
        }
        
        return result;
    }

    // Get total number of items
    public int getNoOfItems() {
        return itemIDMap.size();
    }

    // Import items from CSV
    public void importItemsFromCSV(String filepath, String[][] data) {
        queries.importToItems(filepath, data);
        // Refresh cached data after import
        refreshCachedData();
        // Update category-specific data structures
        loadData();
    }

    // Remove item from database
    public void removeItemFromDatabase(int itemId) {
        
        queries.removeItemFromDatabase(itemId);
        // Refresh cached data after removal
        refreshCachedData();
        // Update category-specific data structures
        loadData();
    }

    // Add item to database
    public void addItemToDatabase(String name, String unit, int quantity, int categoryId) {
        queries.addItemToDatabase(name, unit, quantity, categoryId);
        // Refresh cached data after adding
        refreshCachedData();
        // Update category-specific data structures
        loadData();
    }

    // =======================================================
    // ========== Transaction History Panel Methods ==========
    // =======================================================

    public List<String[]>  getAllTransactionHistory(){
        return allTransactionHistory;
    }

    // Resolves a receipt file under <dirName>/ (created if missing), relative
    // to the working directory. Replaces the old hardcoded "Dir\\file.txt"
    // string, which only worked on Windows -- a literal backslash isn't a
    // path separator on macOS/Linux, so receipts there were written to a
    // single file with a backslash in its name instead of into a folder.
    private Path resolveReceiptPath(String dirName, String fileName) {
        Path dir = Path.of(dirName);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("Could not create receipts directory " + dir + ": " + e.getMessage());
        }
        return dir.resolve(fileName);
    }

    // =======================================================
    // =============== Generate borrow receipt ===============
    // =======================================================
    public void generateBorrowReceipt(int borrowID, Timestamp ts, List<JTextField> studentNumberFields, List<JTextField> fullNameFields, List<String> itemsBorrowedInfo) {
        Path receiptPath = resolveReceiptPath("Borrow Receipts", "BorrowReceipt_" + borrowID + ".txt");
        String fileName = receiptPath.toString();

        try (BufferedWriter writer = Files.newBufferedWriter(receiptPath)) {
            writer.write("==================  RECEIPT  ==================\n");
            writer.write("Borrow ID: " + borrowID + "\n");
            writer.write("Date Borrowed: " + ts + "\n");
            LocalDateTime ldt = ts.toLocalDateTime();
            LocalDateTime newLdt = ldt.plusDays(4);
            writer.write("Expected Return Date: " + Timestamp.valueOf(newLdt) + "\n");
            writer.write("Borrower:\n");

            if(studentNumberFields.size() == 1) {
                writer.write("  1. " + fullNameFields.get(0).getText() + "\n");
            } else {
                for(int n = 0; n < studentNumberFields.size(); n++) {
                    if(n == 0) {
                        writer.write("  " + (n+1) + ". " + fullNameFields.get(n).getText() + " (Leader)\n");
                    } else {
                        writer.write("  " + (n+1) + ". " + fullNameFields.get(n).getText() + "\n");
                    }
                }
            }

            writer.write("Items Borrowed:\n");
            for (String item : itemsBorrowedInfo) {
                writer.write("  " + item + "\n");
            }

            writer.write("===============================================");

            System.out.println("Receipt saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing receipt: " + e.getMessage());
        }
    }

    // =======================================================
    // =============== Generate return receipt ===============
    // =======================================================
    public void generateReturnReceipt(HashMap<String, List<Integer[]>> map, Timestamp ts) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String tS = sdf.format(ts);
        Path receiptPath = resolveReceiptPath("Return Receipts", "ReturnReceipt_" + tS + ".txt");
        String fileName = receiptPath.toString();
        double lateFee = 0.0;

        try (BufferedWriter writer = Files.newBufferedWriter(receiptPath)) {
            writer.write("==================  RECEIPT  ==================\n");
            writer.write("Date Returned: " + ts + "\n");
            for (Map.Entry<String, List<Integer[]>> entry : map.entrySet()) {
                String borrowID = entry.getKey();
                writer.write("  Borrow ID: " + borrowID + "\n");
                writer.write("    Items Returned:\n");
                List<String> itemList = queries.getReturnedItems(Integer.parseInt(borrowID), ts);
                for (int i = 0; i < itemList.size() - 1; i++) {
                    writer.write("      " + itemList.get(i) + "\n");
                }
                lateFee += Double.parseDouble(itemList.get(itemList.size() - 1));
            }
            writer.write("Total Late Fee: " + String.format("%.2f", lateFee) + "\n");

            writer.write("===============================================");

            System.out.println("Receipt saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing receipt: " + e.getMessage());
        }
    }


}
