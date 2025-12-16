package com.inventory.dao;

import com.inventory.model.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    // Critical for Login
    User findUserByUsername(String username) throws SQLException;
    
    // CRUD Operations required for management
    boolean addUser(User user) throws SQLException;
    boolean updateUser(User user) throws SQLException;
    boolean deleteUser(int userId) throws SQLException;
    List<User> getAllUsers() throws SQLException;
}