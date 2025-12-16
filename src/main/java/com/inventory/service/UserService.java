/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


package com.inventory.service;

import com.inventory.dao.UserDAO;
import com.inventory.dao.UserDAOImpl;
import com.inventory.model.User;
import com.inventory.util.AuthenticationException;
import com.inventory.util.SecurityUtil;
import java.util.List;

public class UserService {

    private final UserDAO userDAO;

    /**
     * Constructor uses the concrete DAO implementation.
     */
    public UserService() {
        // Direct instantiation of the implementation class
        this.userDAO = new UserDAOImpl(); 
    }

   
    public User authenticate(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException("Username and password cannot be empty.");
        }
        
        try {
            // 1. Retrieve user data from the database (DAO layer)
            User user = userDAO.findUserByUsername(username);

            if (user == null) {
                // IMPORTANT: Return a generic error to prevent enumeration of valid usernames
                throw new AuthenticationException("Invalid username or password.");
            }

            // 2. Verify the plain password against the stored BCrypt hash (SecurityUtil)
            String storedHash = user.getPasswordHash();
            boolean isPasswordMatch = SecurityUtil.verifyPassword(password, storedHash);

            if (isPasswordMatch) {
                // Authentication successful
                return user;
            } else {
                // Return a generic error for security
                throw new AuthenticationException("Invalid username or password.");
            }

        } catch (AuthenticationException e) {
            // Re-throw specific authentication failures (e.g., if fields were empty)
            throw e;
        } catch (Exception e) {
            // Handle underlying database errors or other unexpected exceptions
            System.err.println("An unexpected error occurred during authentication for user: " + username);
            e.printStackTrace();
            // Wrap the underlying exception in a custom one (Custom Exception requirement)
            throw new AuthenticationException("System error during login. Please try again later.", e);
        }
    }
    
    
    public List<User> getAllUsers() throws Exception {
        return userDAO.getAllUsers();
    }
    
}