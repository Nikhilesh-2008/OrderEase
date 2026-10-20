package repository;

import database.DBConnection;
import model.Feedback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FeedbackRepository {

    private static boolean schemaInitialized = false;

    public FeedbackRepository() {
        if (!schemaInitialized) {
            ensureSchema();
            schemaInitialized = true;
        }
    }

    private synchronized void ensureSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS feedback (" +
                     "feedback_id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "order_id INT NOT NULL, " +
                     "customer_id INT, " +
                     "rating INT NOT NULL, " +
                     "comments VARCHAR(255), " +
                     "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                     ")";
        try (Connection con = DBConnection.getConnection();
             Statement st = con != null ? con.createStatement() : null) {
            if (st != null) {
                st.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addFeedback(int orderId, int customerId, int rating, String comments) {
        String sql = "INSERT INTO feedback (order_id, customer_id, rating, comments, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;

            ps.setInt(1, orderId);
            if (customerId > 0) {
                ps.setInt(2, customerId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, rating);
            ps.setString(4, comments != null ? comments.trim() : "");

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasFeedbackForOrder(int orderId) {
        String sql = "SELECT COUNT(*) FROM feedback WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (con == null) return false;
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Feedback> getAllFeedback() {
        List<Feedback> list = new ArrayList<>();
        String sql = "SELECT f.feedback_id, f.order_id, o.token, f.customer_id, " +
                     "COALESCE(c.name, 'Student') AS customer_name, f.rating, f.comments, f.created_at " +
                     "FROM feedback f " +
                     "LEFT JOIN orders o ON f.order_id = o.order_id " +
                     "LEFT JOIN customer c ON f.customer_id = c.customer_id " +
                     "ORDER BY f.feedback_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (con == null) return list;

            while (rs.next()) {
                Feedback fb = new Feedback(
                    rs.getInt("feedback_id"),
                    rs.getInt("order_id"),
                    rs.getInt("token"),
                    rs.getInt("customer_id"),
                    rs.getString("customer_name"),
                    rs.getInt("rating"),
                    rs.getString("comments"),
                    rs.getTimestamp("created_at")
                );
                list.add(fb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getAverageRating() {
        String sql = "SELECT AVG(rating) FROM feedback";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getTotalFeedbackCount() {
        String sql = "SELECT COUNT(*) FROM feedback";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
