package repository;

import database.DBConnection;
import model.FoodItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class FoodRepository {

    public static final String CANTEEN_SNACKS = "Snacks & Shakes Corner";
    public static final String CANTEEN_CHAI = "Chai & Juice Bar";
    public static final String CANTEEN_FOOD_COURT = "Food Court (Meals & Tiffins)";

    public static final String[] CANTEENS = {
        CANTEEN_SNACKS,
        CANTEEN_CHAI,
        CANTEEN_FOOD_COURT
    };

    private static boolean schemaInitialized = false;

    public FoodRepository() {
        if (!schemaInitialized) {
            ensureSchema();
            schemaInitialized = true;
        }
    }

    private synchronized void ensureSchema() {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            // Safe column additions for MySQL
            try (Statement st = con.createStatement()) {
                st.executeUpdate("ALTER TABLE menu ADD COLUMN canteen VARCHAR(100) DEFAULT 'Snacks & Shakes Corner'");
            } catch (SQLException ignored) {
                // Column already exists
            }

            try (Statement st = con.createStatement()) {
                st.executeUpdate("ALTER TABLE menu ADD COLUMN category VARCHAR(50) DEFAULT 'Fast Food'");
            } catch (SQLException ignored) {
                // Column already exists
            }

            // Check if menu needs college seed data
            int count = 0;
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM menu")) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }

            if (count <= 5) {
                seedCollegeMenu(con);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedCollegeMenu(Connection con) {
        String insertSql = "INSERT INTO menu (item_name, price, available, canteen, category) VALUES (?, ?, ?, ?, ?)";
        Object[][] seeds = {
            // Canteen 1: Snacks & Shakes Corner
            {"Crispy Veg Burger", 80.00, true, CANTEEN_SNACKS, "Fast Food"},
            {"Cheese Burger", 95.00, true, CANTEEN_SNACKS, "Fast Food"},
            {"French Fries", 60.00, true, CANTEEN_SNACKS, "Snacks"},
            {"Peri Peri Fries", 75.00, true, CANTEEN_SNACKS, "Snacks"},
            {"Veg Grilled Sandwich", 70.00, true, CANTEEN_SNACKS, "Fast Food"},
            {"Paneer Tikka Roll", 85.00, true, CANTEEN_SNACKS, "Fast Food"},
            {"Crispy Samosa (2 pcs)", 30.00, true, CANTEEN_SNACKS, "Snacks"},
            {"Veg Puff", 25.00, true, CANTEEN_SNACKS, "Snacks"},
            {"Chocolate Thickshake", 80.00, true, CANTEEN_SNACKS, "Milkshakes"},
            {"Oreo Milkshake", 90.00, true, CANTEEN_SNACKS, "Milkshakes"},
            {"Mango Milkshake", 80.00, true, CANTEEN_SNACKS, "Milkshakes"},
            {"Strawberry Milkshake", 85.00, true, CANTEEN_SNACKS, "Milkshakes"},

            // Canteen 2: Chai & Juice Bar
            {"Special Masala Chai", 15.00, true, CANTEEN_CHAI, "Tea & Coffee"},
            {"Ginger Elaichi Tea", 20.00, true, CANTEEN_CHAI, "Tea & Coffee"},
            {"South Indian Filter Coffee", 25.00, true, CANTEEN_CHAI, "Tea & Coffee"},
            {"Cold Coffee with Ice Cream", 55.00, true, CANTEEN_CHAI, "Tea & Coffee"},
            {"Fresh Watermelon Juice", 40.00, true, CANTEEN_CHAI, "Fresh Juices"},
            {"Mosambi (Sweet Lime) Juice", 50.00, true, CANTEEN_CHAI, "Fresh Juices"},
            {"Fresh Orange Juice", 50.00, true, CANTEEN_CHAI, "Fresh Juices"},
            {"Lemon Mint Cooler", 35.00, true, CANTEEN_CHAI, "Fresh Juices"},
            {"Pineapple Juice", 45.00, true, CANTEEN_CHAI, "Fresh Juices"},

            // Canteen 3: Food Court - Meals & Tiffins
            {"Idli Sambar (3 pcs)", 40.00, true, CANTEEN_FOOD_COURT, "Tiffins"},
            {"Crispy Masala Dosa", 60.00, true, CANTEEN_FOOD_COURT, "Tiffins"},
            {"Puri Bhaji (3 pcs)", 50.00, true, CANTEEN_FOOD_COURT, "Tiffins"},
            {"Veg Dum Biryani", 110.00, true, CANTEEN_FOOD_COURT, "Biryanis"},
            {"Chicken Dum Biryani", 150.00, true, CANTEEN_FOOD_COURT, "Biryanis"},
            {"Chapati with Paneer Curry", 90.00, true, CANTEEN_FOOD_COURT, "Chapati & Meals"},
            {"Chapati with Mixed Veg Curry", 80.00, true, CANTEEN_FOOD_COURT, "Chapati & Meals"},
            {"Veg Fried Rice with Manchurian", 100.00, true, CANTEEN_FOOD_COURT, "Fast Food & Rice"},
            {"Egg Fried Rice", 90.00, true, CANTEEN_FOOD_COURT, "Fast Food & Rice"},
            {"Full South Indian Meals Thali", 110.00, true, CANTEEN_FOOD_COURT, "Chapati & Meals"}
        };

        try (PreparedStatement ps = con.prepareStatement(insertSql)) {
            for (Object[] item : seeds) {
                // Check if already exists by name
                String checkSql = "SELECT COUNT(*) FROM menu WHERE item_name = ?";
                try (PreparedStatement psCheck = con.prepareStatement(checkSql)) {
                    psCheck.setString(1, (String) item[0]);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            continue;
                        }
                    }
                }

                ps.setString(1, (String) item[0]);
                ps.setDouble(2, (Double) item[1]);
                ps.setBoolean(3, (Boolean) item[2]);
                ps.setString(4, (String) item[3]);
                ps.setString(5, (String) item[4]);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<FoodItem> getAllFoodItems() {
        return getFoodItemsByCanteen(null);
    }

    public ArrayList<FoodItem> getFoodItemsByCanteen(String canteenFilter) {
        ArrayList<FoodItem> foodList = new ArrayList<>();
        boolean hasFilter = canteenFilter != null && !canteenFilter.trim().isEmpty() && !canteenFilter.equalsIgnoreCase("All Canteens");

        String sql = hasFilter 
            ? "SELECT item_id, item_name, price, available, canteen, category FROM menu WHERE canteen = ? ORDER BY item_id"
            : "SELECT item_id, item_name, price, available, canteen, category FROM menu ORDER BY canteen, item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return foodList;

            if (hasFilter) {
                ps.setString(1, canteenFilter.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("item_id");
                    String name = rs.getString("item_name");
                    double price = rs.getDouble("price");
                    boolean available = rs.getBoolean("available");
                    String canteen = rs.getString("canteen");
                    String category = rs.getString("category");

                    FoodItem food = new FoodItem(id, name, price, available, canteen, category);
                    foodList.add(food);
                }
            }

        } catch (SQLException e) {
            // Fallback for schemas without canteen/category
            return getAllFoodItemsFallback();
        }

        return foodList;
    }

    private ArrayList<FoodItem> getAllFoodItemsFallback() {
        ArrayList<FoodItem> foodList = new ArrayList<>();
        String sql = "SELECT item_id, item_name, price, available FROM menu ORDER BY item_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (con == null) return foodList;

            while (rs.next()) {
                int id = rs.getInt("item_id");
                String name = rs.getString("item_name");
                double price = rs.getDouble("price");
                boolean available = rs.getBoolean("available");

                FoodItem food = new FoodItem(id, name, price, available);
                foodList.add(food);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return foodList;
    }

    public FoodItem getFoodItemById(int itemId) {
        String sql = "SELECT item_id, item_name, price, available, canteen, category FROM menu WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return null;

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new FoodItem(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getBoolean("available"),
                        rs.getString("canteen"),
                        rs.getString("category")
                    );
                }
            }
        } catch (SQLException e) {
            // Fallback
            return getFoodItemByIdFallback(itemId);
        }
        return null;
    }

    private FoodItem getFoodItemByIdFallback(int itemId) {
        String sql = "SELECT item_id, item_name, price, available FROM menu WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return null;

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new FoodItem(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getBoolean("available")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addFoodItem(String name, double price, boolean available) {
        return addFoodItem(name, price, available, CANTEEN_SNACKS, "General");
    }

    public boolean addFoodItem(String name, double price, boolean available, String canteen, String category) {
        String sql = "INSERT INTO menu (item_name, price, available, canteen, category) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (con == null) return false;

            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setBoolean(3, available);
            ps.setString(4, canteen != null ? canteen : CANTEEN_SNACKS);
            ps.setString(5, category != null ? category : "General");

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            // Fallback to 3 parameters
            return addFoodItemFallback(name, price, available);
        }
    }

    private boolean addFoodItemFallback(String name, double price, boolean available) {
        String sql = "INSERT INTO menu (item_name, price, available) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (con == null) return false;

            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setBoolean(3, available);

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateFoodItem(int itemId, String name, double price, boolean available) {
        return updateFoodItem(itemId, name, price, available, CANTEEN_SNACKS, "General");
    }

    public boolean updateFoodItem(int itemId, String name, double price, boolean available, String canteen, String category) {
        String sql = "UPDATE menu SET item_name = ?, price = ?, available = ?, canteen = ?, category = ? WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return false;

            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setBoolean(3, available);
            ps.setString(4, canteen != null ? canteen : CANTEEN_SNACKS);
            ps.setString(5, category != null ? category : "General");
            ps.setInt(6, itemId);

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            return updateFoodItemFallback(itemId, name, price, available);
        }
    }

    private boolean updateFoodItemFallback(int itemId, String name, double price, boolean available) {
        String sql = "UPDATE menu SET item_name = ?, price = ?, available = ? WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return false;

            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setBoolean(3, available);
            ps.setInt(4, itemId);

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean toggleAvailability(int itemId, boolean available) {
        String sql = "UPDATE menu SET available = ? WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return false;

            ps.setBoolean(1, available);
            ps.setInt(2, itemId);

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFoodItem(int itemId) {
        String sql = "DELETE FROM menu WHERE item_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return false;

            ps.setInt(1, itemId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}