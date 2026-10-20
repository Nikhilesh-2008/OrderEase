package repository;

import database.DBConnection;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public User getUserByUsername(String username) {
        String sql = "SELECT u.user_id, u.username, u.role, c.customer_id, c.name, c.phone " +
                     "FROM users u " +
                     "LEFT JOIN customer c ON u.user_id = c.user_id " +
                     "WHERE u.username = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return null;

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String uname = rs.getString("username");
                    String role = rs.getString("role");
                    int custId = rs.getInt("customer_id"); // 0 if null
                    String custName = rs.getString("name");
                    String phone = rs.getString("phone");

                    // If it's a CUSTOMER and customer profile doesn't exist yet, ensure it exists
                    if ("CUSTOMER".equalsIgnoreCase(role) && custId == 0) {
                        custId = ensureCustomerProfile(con, userId, uname);
                        custName = uname;
                    }

                    return new User(userId, uname, role, custId, custName != null ? custName : uname, phone);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int ensureCustomerProfile(Connection con, int userId, String name) {
        String checkSql = "SELECT customer_id FROM customer WHERE user_id = ?";
        try (PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String insertSql = "INSERT INTO customer (name, phone, user_id) VALUES (?, '', ?)";
        try (PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setInt(2, userId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.role, c.customer_id, c.name, c.phone " +
                     "FROM users u " +
                     "LEFT JOIN customer c ON u.user_id = c.user_id " +
                     "ORDER BY u.user_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (con == null) return list;

            while (rs.next()) {
                list.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("phone")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
