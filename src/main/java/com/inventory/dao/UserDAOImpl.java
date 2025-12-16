package com.inventory.dao;

import com.inventory.model.User;
import com.inventory.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    // QUERY CONSTANTS (Matches your table columns exactly)
    private static final String SELECT_BY_USERNAME = 
        "SELECT * FROM users WHERE username = ?";
    
    private static final String INSERT_USER = 
        "INSERT INTO users (username, full_name, address, phone, password_hash, role) VALUES (?, ?, ?, ?, ?, ?)";
        
    private static final String SELECT_ALL = "SELECT * FROM users";

    @Override
    public User findUserByUsername(String username) throws SQLException {
        User user = null;
        Connection conn = DBConnection.getInstance().getConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USERNAME)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user = mapRowToUser(rs);
            }
        }
        return user;
    }

    @Override
    public boolean addUser(User user) throws SQLException {
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_USER)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName()); // Can be null
            stmt.setString(3, user.getAddress());  // Can be null
            stmt.setString(4, user.getPhone());    // Can be null
            stmt.setString(5, user.getPasswordHash());
            stmt.setString(6, user.getRole().toString()); // "Manager" or "Cashier"
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        Connection conn = DBConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
        }
        return list;
    }

    // Helper method to keep code clean (Avoids code duplication)
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setAddress(rs.getString("address"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        
        // Handle Enum conversion safely
        try {
            user.setRole(User.Role.valueOf(rs.getString("role")));
        } catch (Exception e) {
            user.setRole(User.Role.Cashier); // Default fallback
        }
        
        return user;
    }

    // You can implement update/delete similarly following the pattern above
    @Override
    public boolean updateUser(User user) throws SQLException { return false; } // Implement later
    @Override
    public boolean deleteUser(int userId) throws SQLException { return false; } // Implement later
}