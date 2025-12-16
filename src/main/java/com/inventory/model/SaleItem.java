package com.inventory.model;

public class SaleItem {
    private int id;
    private int saleId;
    private int productId;
    private String productName; // <--- NEW FIELD
    private int quantity;
    private double unitPrice;
    private double subtotal;

    
    public SaleItem() {}

    public SaleItem(int productId, String productName, int quantity, double unitPrice, double subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    // --- NEW GETTER/SETTER ---
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    // -------------------------

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}