package com.glims;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Queries {
    // Overridable via the GLIMS_DB_URL environment variable so the DB host
    // never needs to be hardcoded or committed (see README "Configuration").
    static final String DB_URL = System.getenv().getOrDefault("GLIMS_DB_URL", "jdbc:mysql://localhost:3306/genlab_db");
    private String user;
    private String pass;
    private Boolean connected;
    private Connection conn;
    private Statement stmt;
    private PreparedStatement ptmt;
    private int currentCategoryID = -1;
    
    public Queries(){}

    public void setUser(String user){
        this.user = user;
    }

    public void setPass(String pass){
        this.pass = pass;
    }

    public Boolean login(String username, String password, JFrame mainFrame){
        setUser(username);
        setPass(password);
        try{
            conn = DriverManager.getConnection(DB_URL, user, pass);
            stmt = conn.createStatement();
            
            /*String logIn = "INSERT INTO staff_user (username, last_login) VALUES (?, CURRENT_TIMESTAMP)";
            PreparedStatement logInStmt = conn.prepareStatement(logIn);
            logInStmt.setString(1, username);
            logInStmt.executeUpdate();

            logStaffActivity("login", null, null, null);*/
            connected = true;
        }catch(SQLException e){
            connected = false;
            JOptionPane.showMessageDialog(mainFrame, "Login failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return connected;
    }

    //This only prints out the items to the terminal (removed from Graphical User Interface Class)
    public void borrowItems(){
        String query = "SELECT * FROM item WHERE status = 'Available' ORDER BY category_id, item_name";
        try{
            ptmt = conn.prepareStatement(query);
            ResultSet rs = ptmt.executeQuery();
            while(rs.next()){
                System.out.println("Item ID: " + rs.getInt("item_id") + ", Item Name: " + rs.getString("item_name"));
            }
        }catch(SQLException e){
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    public void showBorrowerList(){
        String query = "SELECT * FROM borrower ORDER BY borrower_id";
        try{
            ptmt = conn.prepareStatement(query);
            ResultSet rs = ptmt.executeQuery();
            while(rs.next()){
                System.out.println("Borrower ID: " + rs.getInt("borrower_id") + ", Borrower Name: " + rs.getString("borrower_name"));
            }
        }catch(SQLException e){
            System.err.println("SQL Error: " + e.getMessage());
        }
    }
    
    public void setCurrentCategoryID(int categoryID){
        this.currentCategoryID = categoryID;
    }

    public int getCurrentCategoryID(){
        return currentCategoryID;
    }

    public List<String[]> autoCompleteForm(String studentID){
        String query = "SELECT full_name, email, contact_number, degree_prog FROM borrower WHERE borrower_id = ?";
        List<String[]> data = new ArrayList<>();
        try{
            ptmt = conn.prepareStatement(query);
            ptmt.setString(1, studentID);
            ResultSet rs = ptmt.executeQuery();

            while (rs.next()) {
                String[] row = new String[] {
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("contact_number"),
                    rs.getString("degree_prog")
                };
                data.add(row);
            }
        }catch(SQLException e){
            System.err.println("SQL Error: " + e.getMessage());
        }
        return data;
    }

    public List<String[]> getAllCategories() {
        List<String[]> categories = new ArrayList<>();
        try {
            String query = "SELECT category_id, category_name FROM category ORDER BY category_name";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String categoryId = String.valueOf(rs.getInt("category_id"));
                String categoryName = rs.getString("category_name");
                categories.add(new String[]{categoryId, categoryName});
            }

            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading categories: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return categories;
    }

    public String getCategoryName(int categoryID) {
        String query = "SELECT category_name FROM category WHERE category_id = ?";
        String categoryName = null;
        
        try {
            ptmt = conn.prepareStatement(query);
            ptmt.setInt(1, categoryID);
            ResultSet rs = ptmt.executeQuery();
            
            if (rs.next()) {
                categoryName = rs.getString("category_name");
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        
        return categoryName;
    }
    
    public int getItemQty(int categoryID){
        
        String query = "SELECT COUNT(*) FROM item WHERE category_id = ?";
        try{
            ptmt = conn.prepareStatement(query);
            ptmt.setInt(1, categoryID);
            ResultSet rs = ptmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }

            rs.close();
        }catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions
        }

        return 0;
    }

    public String[] getItems(int categoryID) {
        String[] items = new String[getItemQty(categoryID)];
        String query = "SELECT item_name, unit FROM item WHERE category_id = ? ORDER BY item_name";
    
        try {
            PreparedStatement ptmt = conn.prepareStatement(query);
    
            ptmt.setInt(1, categoryID);
            ResultSet rs = ptmt.executeQuery();
    
            int index = 0;
            while (rs.next()) {
                String name = rs.getString("item_name");
                String unit = rs.getString("unit");  // may be null
                if (unit == null || unit.trim().isEmpty()) {
                    items[index++] = name;
                }else{
                    items[index++] = name + " - " + unit;
                }
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    public int getItemIDWithoutUnit(String itemName){
        String query = "SELECT item_id FROM item WHERE item_name = ? AND unit IS NULL";
        int itemID = -1;
        
        try {
            ptmt = conn.prepareStatement(query);
            ptmt.setString(1, itemName);
            ResultSet rs = ptmt.executeQuery();
            
            if (rs.next()) {
                itemID = rs.getInt("item_id");
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        
        return itemID;
    }

    public int getCourseQty(){
        String query = "SELECT COUNT(*) FROM course";
        try{
            ptmt = conn.prepareStatement(query);
            ResultSet rs = ptmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }

            rs.close();
        }catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions
        }

        return 0;
    }

    public String getCourseID(String courseName){
        String query = "SELECT course_id FROM course WHERE course_name = ?";
        String courseID = null;
        
        try {
            ptmt = conn.prepareStatement(query);
            ptmt.setString(1, courseName);
            ResultSet rs = ptmt.executeQuery();
            
            if (rs.next()) {
                courseID = rs.getString("course_id");
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        
        return courseID;
    }

    public int getSectionQtyWithoutNullValues(String courseID){
        String query = "SELECT COUNT(*) " +
                        "FROM section s " +
                        "JOIN course_section cs ON s.section_id = cs.section_id " +
                        "WHERE cs.course_id = ?";
        try{
            ptmt = conn.prepareStatement(query);
            ptmt.setString(1, courseID);
            ResultSet rs = ptmt.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }

            rs.close();
        }catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions
        }

        return 0;
    }

    public int getSectionID(String sectionName){
        String query = "SELECT section_id FROM section WHERE section_name = ?";
        int sectionID = -1;
        
        try {
            ptmt = conn.prepareStatement(query);
            ptmt.setString(1, sectionName);
            ResultSet rs = ptmt.executeQuery();
            
            if (rs.next()) {
                sectionID = rs.getInt("section_id");
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        
        return sectionID;
    }
    
    public String[] getInstructorOption(String courseID, String sectionName){
        String query = "SELECT i.first_name AS first_name, i.surname AS surname " +
                        "FROM instructor i " +
                        "JOIN course_section cs ON i.instructor_id = cs.instructor_id " + 
                        "WHERE cs.course_id = ? AND cs.section_id = ?";
        String[] instructorOption = new String[getSectionQtyWithoutNullValues(courseID)];
        try { 
            PreparedStatement ptmt = conn.prepareStatement(query);
            ptmt.setString(1, courseID);
            //System.out.println("Course ID: " + courseID);
            ptmt.setInt(2, getSectionID(sectionName));
            //System.out.println("Section ID: " + getSectionID(sectionName));
            ResultSet rs = ptmt.executeQuery();
            //System.out.println("Query executed successfully.");
            int index = 0;
            while (rs.next()) {
                instructorOption[index++] = rs.getString("first_name") + " " + rs.getString("surname");
                //System.out.println("Instructor: " + rs.getString("first_name") + " " + rs.getString("surname"));
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        return instructorOption;
    }
    public String[] getSectionOption(String courseID){
        String query = "SELECT s.section_name " +
                        "FROM section s " +
                        "JOIN course_section cs ON s.section_id = cs.section_id " +
                        "WHERE cs.course_id = ?";
        String[] sectionOption = new String[getSectionQtyWithoutNullValues(courseID)];
        try {
            ptmt = conn.prepareStatement(query);
            ptmt.setString(1, courseID);
            ResultSet rs = ptmt.executeQuery();
            
            int index = 0;
            while (rs.next()) {
                sectionOption[index++] = rs.getString("section_name");
                //System.out.println("Section: " + rs.getString("section_name"));
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        return sectionOption;
    }

    public String[] getCourseOption(){
        String query = "SELECT course_id FROM course ORDER BY course_name";
        String[] courseOption = new String[getCourseQty()];
        try {
            ptmt = conn.prepareStatement(query);
            ResultSet rs = ptmt.executeQuery();
            
            int index = 0;
            while (rs.next()) {
                courseOption[index++] = rs.getString("course_id");
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        return courseOption;
    }

    public int getItemIDWithUnit(String itemNameWithUnit) {
        String query = "SELECT item_id FROM item WHERE item_name = ? AND unit = ?";
        int itemID = -1;

        String[] parts = itemNameWithUnit.split(" - ", 2);
        if (parts.length < 2) {
            return getItemIDWithoutUnit(itemNameWithUnit);
        }

        String itemName = parts[0].trim();
        String unit = parts[1].trim();

        try {
            PreparedStatement ptmt = conn.prepareStatement(query);

            ptmt.setString(1, itemName);
            ptmt.setString(2, unit);
            ResultSet rs = ptmt.executeQuery();

            if (rs.next()) {
                itemID = rs.getInt("item_id");
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }

        return itemID;
    }

    public int getCategoryID(String categoryName){
        String query = "SELECT category_id FROM category WHERE category_name = ?";
        int categoryID = -1;
        
        try {
            ptmt = conn.prepareStatement(query);
            ptmt.setString(1, categoryName);
            ResultSet rs = ptmt.executeQuery();
            
            if (rs.next()) {
                categoryID = rs.getInt("category_id");
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        
        return categoryID;
    }
    
    public String getItemName(int itemID) {
        String query = "SELECT item_name FROM item WHERE item_id = ?";
        String itemName = null;
        
        try {
            ptmt = conn.prepareStatement(query);
            ptmt.setInt(1, itemID);
            ResultSet rs = ptmt.executeQuery();
            
            if (rs.next()) {
                itemName = rs.getString("item_name");

            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        
        return itemName;
    }

    public String getItemNameWithUnit(int itemID) {
        String query = "SELECT item_name, unit FROM item WHERE item_id = ?";
        String itemName = null;
        
        try {
            ptmt = conn.prepareStatement(query);
            ptmt.setInt(1, itemID);
            ResultSet rs = ptmt.executeQuery();
            
            if (rs.next()) {
                if (rs.getString("unit") == null || rs.getString("unit").trim().isEmpty()) {
                    itemName = rs.getString("item_name");
                } else {
                    itemName = rs.getString("item_name") + " - " + rs.getString("unit");
                }
            }
            
            rs.close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
        
        return itemName;
    }

    /**
     * Starts one atomic checkout transaction and mints a fresh, collision-free
     * borrow_id via the borrow_seq minting table (replaces the old
     * "SELECT MAX(borrow_id)+1" read-then-write race condition). Every
     * borrowItem/insertBorrowerInfo call made before the matching
     * commit/rollbackBorrowTransaction() participates in this same transaction.
     */
    public int beginBorrowTransaction() throws SQLException {
        conn.setAutoCommit(false);
        try (PreparedStatement seqStmt = conn.prepareStatement(
                "INSERT INTO borrow_seq VALUES ()", Statement.RETURN_GENERATED_KEYS)) {
            seqStmt.executeUpdate();
            try (ResultSet keys = seqStmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to reserve a new borrow ID");
    }

    public void commitBorrowTransaction() throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    public void rollbackBorrowTransaction() {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            System.err.println("Rollback error: " + ex.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.err.println("Failed to restore autocommit: " + ex.getMessage());
            }
        }
    }

    /**
     * Inserts one borrow row. Must be called between beginBorrowTransaction()
     * and commit/rollbackBorrowTransaction() so a failure here (including the
     * `after_borrow` trigger's "not enough stock" SIGNAL) can be rolled back
     * together with every other row in the same checkout.
     */
    public void borrowItem(int borrowID, int itemID, String borrowerID, String courseID, int sectionID, int qtyBorrowed) throws SQLException {
        String query = "INSERT INTO borrow(borrow_id, item_id, borrower_id, course_id, section_id, qty_borrowed) VALUES(?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ptmt = conn.prepareStatement(query)) {
            ptmt.setInt(1, borrowID);
            ptmt.setInt(2, itemID);
            ptmt.setString(3, borrowerID);
            ptmt.setString(4, courseID);
            ptmt.setInt(5, sectionID);
            ptmt.setInt(6, qtyBorrowed);
            ptmt.executeUpdate();
        }
    }

    public enum BorrowerInsertResult { CREATED, REUSED, INVALID, FAILED }

    /**
     * Distinguishes "new borrower created" from "existing borrower reused"
     * from "invalid data" from "database failure" instead of the old
     * INSERT IGNORE, which silently swallowed duplicate/invalid rows and
     * reported success regardless. Uses the shared checkout connection so it
     * can participate in a caller's beginBorrowTransaction() when called
     * during checkout.
     */
    public BorrowerInsertResult insertBorrowerInfo(String borrowerID, String fullName, String email, String contactNumber, String degreeProgram) {
        if (isBlank(borrowerID) || isBlank(fullName) || isBlank(email) || isBlank(contactNumber)) {
            return BorrowerInsertResult.INVALID;
        }

        try {
            try (PreparedStatement existsStmt = conn.prepareStatement("SELECT 1 FROM borrower WHERE borrower_id = ?")) {
                existsStmt.setString(1, borrowerID);
                try (ResultSet rs = existsStmt.executeQuery()) {
                    if (rs.next()) {
                        return BorrowerInsertResult.REUSED;
                    }
                }
            }

            String insertQuery = "INSERT INTO borrower(borrower_id, full_name, email, degree_prog, contact_number) VALUES(?, ?, ?, ?, ?)";
            try (PreparedStatement ptmt = conn.prepareStatement(insertQuery)) {
                ptmt.setString(1, borrowerID);
                ptmt.setString(2, fullName);
                ptmt.setString(3, email);
                ptmt.setString(4, degreeProgram);
                ptmt.setString(5, contactNumber);
                ptmt.executeUpdate();
            }
            return BorrowerInsertResult.CREATED;
        } catch (SQLException e) {
            System.err.println("Failed to insert borrower " + borrowerID + ": " + e.getMessage());
            return BorrowerInsertResult.FAILED;
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }


    // ----------------------------------------------------------
    //                 JHUN KENNETH INIEGO QUERIES
    // ----------------------------------------------------------
    public List<String> getBorrowedItemsInfo(int borrowId) {
        String query = "SELECT qty_borrowed, item_name, unit FROM borrow JOIN item USING(item_id) WHERE borrow_id = ?";
        List<String> itemInfo = new ArrayList<>();

        try{
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ptmt = conn.prepareStatement(query);
            ptmt.setInt(1, borrowId);
            ResultSet rs = ptmt.executeQuery();

            while(rs.next()) {
                String itemConcat;
                if(rs.getString("unit") == null) {
                    itemConcat = "[" + rs.getString("qty_borrowed") + "x] " + rs.getString("item_name");
                } else {
                    itemConcat = "[" + rs.getString("qty_borrowed") + "x] " + rs.getString("item_name") + " (" + rs.getString("unit") + ")";
                }
                itemInfo.add(itemConcat);
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }catch(SQLException e){
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error: " + e1.getMessage());
            }
            System.err.println("SQL Error: " + e.getMessage());
        }

        return itemInfo;
    }

    public int getNoOfItems() {
        int n = 0;
        String query = "SELECT COUNT(item_id) AS no_of_items FROM item";

        try{
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ptmt = conn.prepareStatement(query);
            ResultSet rs = ptmt.executeQuery();

            while(rs.next()) n = rs.getInt("no_of_items");

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }catch(SQLException e){
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error: " + e1.getMessage());
            }
            System.err.println("SQL Error: " + e.getMessage());
        }

        return n;
    }

    public void importToItems(String filePath, String[][] items) {
        try {
            for(String[] tuple : items) {
                String query = "INSERT INTO item(item_name, unit, qty, category_id, status) VALUES(?, ?, ?, ?, ?)";

                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                ptmt = conn.prepareStatement(query);
                ptmt.setString(1, tuple[0]);
                if(!tuple[1].equals("null")) ptmt.setString(2, tuple[1]);
                else ptmt.setNull(2, java.sql.Types.VARCHAR);
                ptmt.setInt(3, Integer.parseInt(tuple[2]));
                ptmt.setInt(4, Integer.parseInt(tuple[3]));
                ptmt.setString(5, tuple[4]);
                int rowsInserted = ptmt.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println("A new item was inserted successfully! ==> " + tuple[0]);
                }

                conn.commit();
                conn.setAutoCommit(true);
                conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error: " + e1.getMessage());
            }
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    public String[][] getItemsPerCategory(int categoryID) {
        String query = "SELECT item_id, item_name, unit, qty FROM item WHERE category_id = ?";
        String[][] data = null;

        try{
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ptmt = conn.prepareStatement(query);
            ptmt.setInt(1, categoryID);
            ResultSet rs1 = ptmt.executeQuery();

            int rows = 0;
            while (rs1.next()) {
                rows++;
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            PreparedStatement ptmt1 = conn.prepareStatement(query);
            ptmt1.setInt(1, categoryID);
            ResultSet rs = ptmt1.executeQuery();

            data = new String[rows][4];
            int rowIndex = 0;
            while (rs.next()) {
                data[rowIndex][0] = rs.getString("item_id");
                data[rowIndex][1] = rs.getString("item_name");
                data[rowIndex][2] = rs.getString("unit");
                data[rowIndex][3] = rs.getString("qty");
                rowIndex++;
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }catch(SQLException e){
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error: " + e1.getMessage());
            }
            System.err.println("SQL Error: " + e.getMessage());
        }

        return data;
    }

    public void addItemToDatabase(String itemName, String unit, int qty, int categoryID) {
        String insertQuery = "INSERT INTO item(item_name, unit, qty, category_id, status) VALUES(?, ?, ?, ?, ?)";

        try{
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ptmt = conn.prepareStatement(insertQuery);
            ptmt.setString(1, itemName);
            if(unit.equals("null")) ptmt.setNull(2, java.sql.Types.VARCHAR);
            else ptmt.setString(2, unit);
            ptmt.setInt(3, qty);
            ptmt.setInt(4, categoryID);
            ptmt.setString(5, "Available");

            ptmt.executeUpdate();

            conn.commit();
            logStaffActivity("item_updated", null, null, null);
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }catch(SQLException e){
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error: " + e1.getMessage());
            }
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    public void removeItemFromDatabase(int itemID) {
        String deleteQuery = "DELETE FROM item WHERE item_id = ?";

        try{
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ptmt = conn.prepareStatement(deleteQuery);
            ptmt.setInt(1, itemID);
            ptmt.executeUpdate();

            conn.commit();
            logStaffActivity("item_updated", null, itemID, null);
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }catch(SQLException e){
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error: " + e1.getMessage());
            }
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    public void updateActualReturnDate(HashMap<String, List<Integer[]>> map, Timestamp ts, String borrowerID) {
        // String updateQuery = "UPDATE borrow SET actual_return_date = ? WHERE borrow_id = ? AND item_id = ?";
        for (Map.Entry<String, List<Integer[]>> entry : map.entrySet()) {
            String borrowID = entry.getKey();
            List<Integer[]> itemList = entry.getValue();
            System.out.println("Borrow ID: " + borrowID);
            for (Integer[] itemEntry : itemList) {
                int itemID = itemEntry[0];
                int qtyReturned = itemEntry[1];
                System.out.println("Updated actual return date");
                System.out.println("  Item ID: " + itemID + ", Quantity: " + qtyReturned);
                String addQuery = "INSERT INTO return_log(borrow_id, borrower_id, item_id, return_date, item_condition, qty_returned) VALUES(?, ?, ?, ?, ?, ?)";
                try{
                    conn.setAutoCommit(false);
                    conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                    PreparedStatement ptmt2 = conn.prepareStatement(addQuery);
                    ptmt2.setInt(1, Integer.parseInt(borrowID));
                    ptmt2.setString(2, borrowerID);
                    ptmt2.setInt(3, itemID);
                    ptmt2.setTimestamp(4, ts);
                    ptmt2.setString(5, "Good Condition");
                    ptmt2.setInt(6, qtyReturned);
                    System.out.println("X");
                    ptmt2.executeUpdate();

                    conn.commit();
                    conn.setAutoCommit(true);
                    conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                }catch(SQLException e){
                    try {
                        conn.rollback();
                    } catch (SQLException e1) {
                        System.err.println("SQL Error2.1B: " + e1.getMessage());
                    }
                    System.err.println("SQL Error2.2B: " + e.getMessage());
                }
            }
        }
    }
    
    public List<String[]> getBorrowList() {
        // String query = "SELECT DISTINCT full_name, borrower_id, date_borrowed, expected_return_date, " +
        //                 "degree_prog, course_id, section_name FROM borrow JOIN borrower USING(borrower_id) JOIN course USING(course_id) JOIN section USING(section_id) WHERE actual_return_date IS NULL ORDER BY date_borrowed DESC, expected_return_date DESC";
        String query1 = "SELECT DISTINCT full_name, borrower_id, degree_prog FROM borrow JOIN borrower USING(borrower_id) WHERE actual_return_date IS NULL ORDER BY full_name ASC";
        // Updated Query as of May 7 2025: "SELECT DISTINCT full_name, borrower_id," + "degree_prog, course_id, section_name FROM borrow JOIN borrower USING(borrower_id) JOIN course USING(course_id) JOIN section USING(section_id)"
        List<String[]> data = new ArrayList<>();

        try{
            ptmt = conn.prepareStatement(query1);
            ResultSet rs = ptmt.executeQuery();

            while (rs.next()) {
                String[] row = new String[] {
                    rs.getString("full_name"),
                    rs.getString("borrower_id"),
                    // rs.getString("date_borrowed"),
                    // rs.getString("expected_return_date"),
                    rs.getString("degree_prog")
                    // rs.getString("course_id"),
                    // rs.getString("section_name")
                };
                data.add(row);
            }
        }catch(SQLException e){
            System.err.println("SQL Error 2C: " + e.getMessage());
        }

        return data;
    }


    // used in controller to load all items borrowed by each user
    public List<String[]> getAllItemsBorrowed() {
        List<String[]> dataList = new ArrayList<>();
        String query = "SELECT qty_borrowed, item_name, unit, item_id, borrow_id, borrower_id, date_borrowed, expected_return_date FROM borrow JOIN borrower USING(borrower_id) JOIN item USING(item_id) WHERE actual_return_date IS NULL ORDER BY date_borrowed DESC";

        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ptmt = conn.prepareStatement(query);
            ResultSet rs = ptmt.executeQuery();

            while (rs.next()) {
                String[] row = new String[]{
                    rs.getString("qty_borrowed"),
                    rs.getString("item_name"),
                    rs.getString("unit"),
                    rs.getString("item_id"),
                    rs.getString("borrow_id"),
                    rs.getString("borrower_id"),
                    rs.getString("date_borrowed"),
                    rs.getString("expected_return_date")
                };
                dataList.add(row);
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error: " + e1.getMessage());
            }
            System.err.println("SQL Error: " + e.getMessage());
        }

        return dataList;
    }

    public String[][] getItemsBorrowed(String borrowerID, String dateBorrowed, String expectedReturnDate) {
        String query = "SELECT qty_borrowed, item_name, unit, item_id, borrow_id FROM borrow JOIN borrower USING(borrower_id) JOIN item USING(item_id) WHERE borrower_id = ? AND date_borrowed = ? and expected_return_date = ? AND actual_return_date IS NULL ORDER BY date_borrowed DESC";
        String[][] data = null;

        try{
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ptmt = conn.prepareStatement(query);
            ptmt.setString(1, borrowerID);
            ptmt.setString(2, dateBorrowed);
            ptmt.setString(3, expectedReturnDate);
            ResultSet rs1 = ptmt.executeQuery();

            int rows = 0;
            while (rs1.next()) {
                rows++;
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            PreparedStatement ptmt1 = conn.prepareStatement(query);
            ptmt1.setString(1, borrowerID);
            ptmt1.setString(2, dateBorrowed);
            ptmt1.setString(3, expectedReturnDate);
            ResultSet rs = ptmt1.executeQuery();

            data = new String[rows][5];
            int rowIndex = 0;
            while (rs.next()) {
                data[rowIndex][0] = rs.getString("qty_borrowed");
                data[rowIndex][1] = rs.getString("item_name");
                data[rowIndex][2] = rs.getString("unit");
                data[rowIndex][3] = rs.getString("item_id");
                data[rowIndex][4] = rs.getString("borrow_id");
                rowIndex++;
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }catch(SQLException e){
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error: " + e1.getMessage());
            }
            System.err.println("SQL Error: " + e.getMessage());
        }

        return data;
    }

    public int getRows(String query) {
        int rows = 0;

        try{
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ptmt = conn.prepareStatement(query);
            ResultSet rs1 = ptmt.executeQuery();

            while (rs1.next()) {
                rows++;
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }catch(SQLException e){
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error Count1: " + e1.getMessage());
            }
            System.err.println("SQL Error Count1: " + e.getMessage());
        }

        return rows;
    }

    public void getReturnLog() {
        String query = "SELECT qty_borrowed, item_name, unit, item_id, borrow_id FROM borrow JOIN borrower USING(borrower_id) JOIN item USING(item_id) WHERE borrower_id = ? AND date_borrowed = ? and expected_return_date = ? AND actual_return_date IS NULL ORDER BY date_borrowed DESC";
        String[][] data = null;

        try{
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ptmt = conn.prepareStatement(query);
            ResultSet rs1 = ptmt.executeQuery();

            int rows = 0;
            while (rs1.next()) {
                rows++;
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            PreparedStatement ptmt1 = conn.prepareStatement(query);
            ResultSet rs = ptmt1.executeQuery();

            data = new String[rows][5];
            int rowIndex = 0;
            while (rs.next()) {
                data[rowIndex][0] = rs.getString("qty_borrowed");
                data[rowIndex][1] = rs.getString("item_name");
                data[rowIndex][2] = rs.getString("unit");
                data[rowIndex][3] = rs.getString("item_id");
                data[rowIndex][4] = rs.getString("borrow_id");
                rowIndex++;
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }catch(SQLException e){
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error: " + e1.getMessage());
            }
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    public List<String[]> getTransactionHistory() {
        // Get borrow details
        String borrowQuery = "SELECT DISTINCT date_borrowed, full_name, borrower_id, expected_return_date, item_name FROM borrow JOIN borrower USING (borrower_id) JOIN item USING (item_id);";
        // Get return details
        String returnQuery = "SELECT rl.return_date, b.full_name, rl.qty_returned, i.item_name, rl.item_condition, rl.late_fee FROM return_log rl JOIN borrower b ON rl.borrower_id = b.borrower_id JOIN item i ON rl.item_id = i.item_id ORDER BY rl.return_date ASC;";
        List<String[]> data = new ArrayList<>();

        try{
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            PreparedStatement ptmt1 = conn.prepareStatement(returnQuery);
            ResultSet rsReturn = ptmt1.executeQuery();
            while(rsReturn.next()) {
                String[] row = new String[6];
                row[0] = "Return";
                row[1] = rsReturn.getString("return_date");
                row[2] = rsReturn.getString("full_name");
                row[3] = "[" + rsReturn.getString("qty_returned") + "x] " + rsReturn.getString("item_name");
                row[4] = rsReturn.getString("item_condition");
                row[5] = rsReturn.getString("late_fee");
                data.add(row);
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ptmt = conn.prepareStatement(borrowQuery);
            ResultSet rsBorrow = ptmt.executeQuery();
            while (rsBorrow.next()) {
                String[] row = new String[6];
                row[0] = "Borrow";
                row[1] = rsBorrow.getString("date_borrowed");
                row[2] = rsBorrow.getString("full_name");
                row[3] = rsBorrow.getString("item_name");
                row[4] = rsBorrow.getString("expected_return_date");
                row[5] = null;
                data.add(row);
            }

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            data.sort(Comparator.comparing((String[] row) -> row[1]).reversed());
        }catch(SQLException e){
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error 1D: " + e1.getMessage());
            }
            System.err.println("SQL Error 2D: " + e.getMessage());
        }

        return data;
    }

    public List<String> getReturnedItems(int borrowId, Timestamp ts) {
        String query = "SELECT rl.qty_returned, i.item_name, i.unit, rl.item_condition, rl.late_fee FROM return_log rl JOIN item i ON rl.item_id = i.item_id WHERE rl.borrow_id = ? AND rl.return_date = ?";
        List<String> itemInfo = new ArrayList<>();
        double lateFee = 0.00;

        try{
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            ptmt = conn.prepareStatement(query);
            ptmt.setInt(1, borrowId);
            ptmt.setTimestamp(2, ts);
            ResultSet rs = ptmt.executeQuery();

            while(rs.next()) {
                String itemConcat;
                System.out.println("Get items returned");
                if(rs.getString("unit") == null) {
                    itemConcat = "[" + rs.getString("qty_returned") + "x] " + rs.getString("item_name")
                                + "\n        --> Item Condition: " + rs.getString("item_condition")
                                + "\n        --> Late Fee: " + rs.getString("late_fee");
                } else {
                    itemConcat = "[" + rs.getString("qty_returned") + "x] " + rs.getString("item_name") + " (" + rs.getString("unit") + ")"
                                + "\n        --> Item Condition: " + rs.getString("item_condition")
                                + "\n        --> Late Fee: " + rs.getString("late_fee");
                }
                itemInfo.add(itemConcat);
                lateFee += rs.getDouble("late_fee");
            }
            itemInfo.add(String.format("%.2f", lateFee) + "");

            conn.commit();
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }catch(SQLException e){
            try {
                conn.rollback();
            } catch (SQLException e1) {
                System.err.println("SQL Error: " + e1.getMessage());
            }
            System.err.println("SQL Error: " + e.getMessage());
        }

        return itemInfo;
    }

    public void updateItemQuantity(int itemId, int newQuantity) {
        String query = "UPDATE item SET qty = ? WHERE item_id = ?";
        try {
            PreparedStatement ptmt = conn.prepareStatement(query);
            ptmt.setInt(1, newQuantity);
            ptmt.setInt(2, itemId);
            ptmt.executeUpdate();
            ptmt.close();
        } catch (SQLException e) {
            System.err.println("SQL Error while updating quantity: " + e.getMessage());
        }
    }

    private void logStaffActivity(String activityType, Integer borrowId, Integer itemId, String borrowerId){        
        String logQuery = "INSERT INTO staff_activity_log(username, activity_type, borrow_id, item_id, borrower_id) VALUES(?, ?, ?, ?, ?)";
        try{
            PreparedStatement logStmt = conn.prepareStatement(logQuery);
            logStmt.setString(1, user);
            logStmt.setString(2, activityType);
            
            if(borrowId != null) logStmt.setInt(3, borrowId);
            else logStmt.setNull(3, java.sql.Types.INTEGER);
            
            if(itemId != null) logStmt.setInt(4, itemId);
            else logStmt.setNull(4, java.sql.Types.INTEGER);
            
            if(borrowerId != null) logStmt.setString(5, borrowerId);
            else logStmt.setNull(5, java.sql.Types.VARCHAR);
            
            logStmt.executeUpdate();
            conn.commit();
            logStmt.close();
        }catch(SQLException e){
            System.err.println("Failed to log staff activity: " + e.getMessage());
        }
    }
}
