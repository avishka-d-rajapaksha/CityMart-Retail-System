package com.inventory.model;

import java.sql.Date;

public class NonPerishableProduct extends Product {

    public NonPerishableProduct(int productId, String name, double price, int stockQuantity, int reorderLevel) {
        super(productId, name, price, stockQuantity, reorderLevel, "Non-Perishable");
    }

    @Override
    public Date getExpiryDate() {
        return null; // No expiry
    }
}