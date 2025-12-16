package com.inventory.model;

import java.sql.Timestamp;

public class User {

    // Matches the ENUM in your SQL Dump
    public enum Role {
        Manager, Cashier
    }

    private int userId;           // database: user_id
    private String username;      // database: username
    private String fullName;      // database: full_name
    private String address;       // database: address
    private String phone;         // database: phone
    private String passwordHash;  // database: password_hash
    private Role role;            // database: role
    private Timestamp createdAt;  // database: created_at

    // Default Constructor
    public User() {}

    // Constructor for creating new users
    public User(String username, String passwordHash, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}