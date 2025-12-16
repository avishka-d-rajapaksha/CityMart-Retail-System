package com.inventory.model;

import java.sql.Date;

// INHERITANCE
public class PerishableProduct extends Product {
    private Date expiryDate;

    public PerishableProduct(int productId, String name, double price, int stockQuantity, int reorderLevel, Date expiryDate) {
        super(productId, name, price, stockQuantity, reorderLevel, "Perishable");
        this.expiryDate = expiryDate;
    }

    @Override
    public Date getExpiryDate() {
        return expiryDate;
    }
}