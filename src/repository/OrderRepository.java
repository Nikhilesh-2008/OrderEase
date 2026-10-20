package repository;

import database.DBConnection;
import model.Order;
import model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    public synchronized Order createOrder(int customerId, List<OrderItem> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot create an order with no items.");
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            if (con == null) {
                throw new SQLException("Failed to establish database connection.");
            }

            con.setAutoCommit(false);

            // 1. Validate items and verify current database prices & availability
            double totalAmount = 0.0;
            String checkItemSql = "SELECT item_name, price, available FROM menu WHERE item_id = ?";
            try (PreparedStatement psCheck = con.prepareStatement(checkItemSql)) {
                for (OrderItem item : items) {
                    psCheck.setInt(1, item.getItemId());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Menu item ID " + item.getItemId() + " not found.");
                        }
                        boolean available = rs.getBoolean("available");
                        if (!available) {
                            throw new SQLException("Item '" + rs.getString("item_name") + "' is currently out of stock.");
                        }
                        double currentPrice = rs.getDouble("price");
                        item.setUnitPrice(currentPrice);
                        item.setItemName(rs.getString("item_name"));
                        totalAmount += currentPrice * item.getQuantity();
                    }
                }
            }

            // 2. Allocate next collision-safe token
            int nextToken = 101;
            String tokenSql = "SELECT COALESCE(MAX(token), 100) + 1 AS next_token FROM orders FOR UPDATE";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(tokenSql)) {
                if (rs.next()) {
                    nextToken = rs.getInt("next_token");
                }
            }

            // 3. Insert order header
            int orderId = -1;
            String insertOrderSql = "INSERT INTO orders (token, customer_id, order_time, total_amount, status) " +
                                   "VALUES (?, ?, NOW(), ?, 'PLACED')";
            try (PreparedStatement psOrder = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, nextToken);
                if (customerId > 0) {
                    psOrder.setInt(2, customerId);
                } else {
                    psOrder.setNull(2, java.sql.Types.INTEGER);
                }
                psOrder.setDouble(3, totalAmount);

                psOrder.executeUpdate();
                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    }
                }
            }

            if (orderId <= 0) {
                throw new SQLException("Failed to retrieve generated order ID.");
            }

            // 4. Insert order items
            String insertItemSql = "INSERT INTO order_items (order_id, item_id, quantity, unit_price) " +
                                  "VALUES (?, ?, ?, ?)";
            try (PreparedStatement psItem = con.prepareStatement(insertItemSql)) {
                for (OrderItem item : items) {
                    psItem.setInt(1, orderId);
                    psItem.setInt(2, item.getItemId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setDouble(4, item.getUnitPrice());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            con.commit();

            // Build returned Order object
            Order order = new Order();
            order.setOrderId(orderId);
            order.setToken(nextToken);
            order.setCustomerId(customerId);
            order.setTotalAmount(totalAmount);
            order.setStatus("PLACED");
            order.setOrderTime(new Timestamp(System.currentTimeMillis()));
            order.setItems(items);

            return order;

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.token, o.customer_id, c.name AS customer_name, " +
                     "o.order_time, o.total_amount, o.status " +
                     "FROM orders o " +
                     "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                     "WHERE o.customer_id = ? " +
                     "ORDER BY o.order_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return list;

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("token"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getTimestamp("order_time"),
                        rs.getDouble("total_amount"),
                        rs.getString("status")
                    );
                    loadItemsForOrder(con, order);
                    list.add(order);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> getAllActiveOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.token, o.customer_id, c.name AS customer_name, " +
                     "o.order_time, o.total_amount, o.status " +
                     "FROM orders o " +
                     "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                     "WHERE o.status IN ('PLACED', 'PREPARING', 'READY') " +
                     "ORDER BY o.order_id ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (con == null) return list;

            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("order_id"),
                    rs.getInt("token"),
                    rs.getInt("customer_id"),
                    rs.getString("customer_name"),
                    rs.getTimestamp("order_time"),
                    rs.getDouble("total_amount"),
                    rs.getString("status")
                );
                loadItemsForOrder(con, order);
                list.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.token, o.customer_id, c.name AS customer_name, " +
                     "o.order_time, o.total_amount, o.status " +
                     "FROM orders o " +
                     "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                     "ORDER BY o.order_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (con == null) return list;

            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("order_id"),
                    rs.getInt("token"),
                    rs.getInt("customer_id"),
                    rs.getString("customer_name"),
                    rs.getTimestamp("order_time"),
                    rs.getDouble("total_amount"),
                    rs.getString("status")
                );
                loadItemsForOrder(con, order);
                list.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return false;

            ps.setString(1, newStatus);
            ps.setInt(2, orderId);

            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelOrder(int orderId) {
        String sql = "UPDATE orders SET status = 'CANCELLED' WHERE order_id = ? AND status = 'PLACED'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setInt(1, orderId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT o.order_id, o.token, o.customer_id, c.name AS customer_name, " +
                     "o.order_time, o.total_amount, o.status " +
                     "FROM orders o " +
                     "LEFT JOIN customer c ON o.customer_id = c.customer_id " +
                     "WHERE o.order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return null;
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("token"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getTimestamp("order_time"),
                        rs.getDouble("total_amount"),
                        rs.getString("status")
                    );
                    loadItemsForOrder(con, order);
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadItemsForOrder(Connection con, Order order) {
        String sql = "SELECT oi.order_item_id, oi.item_id, m.item_name, oi.quantity, oi.unit_price " +
                     "FROM order_items oi " +
                     "JOIN menu m ON oi.item_id = m.item_id " +
                     "WHERE oi.order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, order.getOrderId());
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                        rs.getInt("order_item_id"),
                        order.getOrderId(),
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price")
                    );
                    items.add(item);
                }
                order.setItems(items);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getQueuePosition(int orderId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE status IN ('PLACED', 'PREPARING') AND order_id < ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return 0;
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public java.util.Map<String, Double> getSalesByCanteen() {
        java.util.Map<String, Double> map = new java.util.LinkedHashMap<>();
        map.put(FoodRepository.CANTEEN_SNACKS, 0.0);
        map.put(FoodRepository.CANTEEN_CHAI, 0.0);
        map.put(FoodRepository.CANTEEN_FOOD_COURT, 0.0);

        String sql = "SELECT m.canteen, SUM(oi.quantity * oi.unit_price) as total_sales " +
                     "FROM order_items oi " +
                     "JOIN menu m ON oi.item_id = m.item_id " +
                     "GROUP BY m.canteen";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (con != null) {
                while (rs.next()) {
                    String c = rs.getString("canteen");
                    double s = rs.getDouble("total_sales");
                    if (c != null) {
                        map.put(c, s);
                    }
                }
            }
        } catch (SQLException e) {
            // Fallback gracefully
        }
        return map;
    }
}
