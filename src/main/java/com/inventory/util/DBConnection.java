package com.inventory.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/inventory_system";
    private static final String DB_USER = "root";
    
    // CORRECTED: Empty password for XAMPP
    private static final String DB_PASS = ""; 

    private static DBConnection instance = null;
    private Connection connection = null;

    private DBConnection() {
        initializeConnection();
    }

    private void initializeConnection() {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("JDBC Driver Registered.");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Database Connection Successful.");
        } catch (ClassNotFoundException e) {
            System.err.println("FATAL ERROR: MySQL JDBC Driver not found.");
            throw new RuntimeException("Missing MySQL JDBC Driver.", e);
        } catch (SQLException e) {
            System.err.println("FATAL ERROR: Failed to establish database connection.");
            System.err.println("SQL State: " + e.getSQLState());
            throw new RuntimeException("Database connection failed.", e);
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            System.out.println("Connection lost. Attempting to reconnect...");
            initializeConnection();
        }
        return connection;
    }

    // --- THIS WAS MISSING, NOW RESTORED ---
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Database Connection Closed.");
                }
            } catch (SQLException e) {
                System.err.println("ERROR: Could not close database connection.");
            }
        }
        instance = null; // Allow re-initialization
    }
}