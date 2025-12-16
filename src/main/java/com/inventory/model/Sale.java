package com.inventory.model;

import java.sql.Timestamp;

public class Sale {
    private int saleId;
    private int userId;
    private String cashierName; // <--- This was missing
    private Timestamp saleDate;
    private double totalAmount;

    public Sale() {}

    public Sale(int userId, double totalAmount) {
        this.userId = userId;
        this.totalAmount = totalAmount;
    }

    // --- Getters and Setters ---
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getCashierName() { return cashierName; }
    public void setCashierName(String cashierName) { this.cashierName = cashierName; }

    public Timestamp getSaleDate() { return saleDate; }
    public void setSaleDate(Timestamp saleDate) { this.saleDate = saleDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}