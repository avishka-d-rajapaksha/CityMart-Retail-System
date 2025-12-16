package com.inventory.model;

import java.sql.Date;

// ABSTRACT CLASS (Abstraction)
public abstract class Product {
    protected int productId;
    protected String name;
    protected double price;
    protected int stockQuantity;
    protected int reorderLevel;
    protected String category;

    public Product(int productId, String name, double price, int stockQuantity, int reorderLevel, String category) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
        this.category = category;
    }

    // Abstract method (Polymorphism)
    public abstract Date getExpiryDate();

    // Getters and Setters (Encapsulation)
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}