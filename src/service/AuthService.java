package service;

import database.DBConnection;
import model.User;
import repository.UserRepository;
import security.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AuthService {

    private final UserRepository userRepository = new UserRepository();

    public String login(String username, String password, String selectedRole) {

        String sql = "SELECT user_id, username, password_hash, role FROM users WHERE username = ?";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                return "DATABASE_ERROR";
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        return "INVALID_CREDENTIALS";
                    }

                    String storedPasswordHash = rs.getString("password_hash");
                    String databaseRole = rs.getString("role");

                    String enteredPasswordHash = PasswordUtil.hashPassword(password);

                    if (!storedPasswordHash.equals(enteredPasswordHash)) {
                        return "INVALID_CREDENTIALS";
                    }

                    if (!databaseRole.equalsIgnoreCase(selectedRole)) {
                        return "UNAUTHORIZED";
                    }

                    return databaseRole;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "DATABASE_ERROR";
        }
    }

    public User getAuthenticatedUser(String username) {
        return userRepository.getUserByUsername(username);
    }

    public String register(String username, String password) {
        return register(username, password, "");
    }

    public String register(String username, String password, String phone) {

        String insertUserSql = "INSERT INTO users (username, password, password_hash, role) VALUES (?, ?, ?, 'CUSTOMER')";
        String insertCustomerSql = "INSERT INTO customer (name, phone, user_id) VALUES (?, ?, ?)";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            if (con == null) {
                return "DATABASE_ERROR";
            }

            // Begin ACID Transaction
            con.setAutoCommit(false);

            String passwordHash = PasswordUtil.hashPassword(password);
            int newUserId = -1;

            // 1. Insert into users
            try (PreparedStatement psUser = con.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                psUser.setString(1, username);
                psUser.setString(2, passwordHash);
                psUser.setString(3, passwordHash);

                psUser.executeUpdate();

                try (ResultSet rs = psUser.getGeneratedKeys()) {
                    if (rs.next()) {
                        newUserId = rs.getInt(1);
                    }
                }
            }

            if (newUserId <= 0) {
                con.rollback();
                return "DATABASE_ERROR";
            }

            // 2. Insert into customer
            try (PreparedStatement psCust = con.prepareStatement(insertCustomerSql)) {
                psCust.setString(1, username);
                psCust.setString(2, phone != null ? phone : "");
                psCust.setInt(3, newUserId);

                psCust.executeUpdate();
            }

            // Commit transaction
            con.commit();
            return "SUCCESS";

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return "USERNAME_EXISTS";

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return "DATABASE_ERROR";

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
}