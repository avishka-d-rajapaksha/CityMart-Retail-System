package com.inventory.dao;

import com.inventory.model.NonPerishableProduct;
import com.inventory.model.PerishableProduct;
import com.inventory.model.Product;
import com.inventory.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public List<Product> getAllProducts() throws Exception {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                // Determine object type based on Category (Polymorphism in action)
                String category = rs.getString("category");
                Product p;
                
                if ("Perishable".equalsIgnoreCase(category)) {
                    p = new PerishableProduct(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("reorder_level"),
                        rs.getDate("expiry_date")
                    );
                } else {
                    p = new NonPerishableProduct(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("reorder_level")
                    );
                }
                list.add(p);
            }
        }
        return list;
    }

    @Override
    public boolean addProduct(Product product) throws Exception {
        String sql = "INSERT INTO products (name, category, price, stock_quantity, reorder_level, expiry_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.setInt(5, product.getReorderLevel());
            ps.setDate(6, product.getExpiryDate()); // Will be null for Non-Perishable
            
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateProduct(Product product) throws Exception {
        String sql = "UPDATE products SET name=?, category=?, price=?, stock_quantity=?, reorder_level=?, expiry_date=? WHERE product_id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());
            ps.setDouble(3, product.getPrice());
            ps.setInt(4, product.getStockQuantity());
            ps.setInt(5, product.getReorderLevel());
            ps.setDate(6, product.getExpiryDate());
            ps.setInt(7, product.getProductId());
            
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteProduct(int productId) throws Exception {
        String sql = "DELETE FROM products WHERE product_id=?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        }
    }
    @Override
    public List<Product> searchProducts(String keyword) throws Exception {
        List<Product> list = new ArrayList<>();
        // SQL: Selects products where Name, ID, or Category contains the keyword
        String sql = "SELECT * FROM products WHERE name LIKE ? OR product_id LIKE ? OR category LIKE ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%"; // % allows finding matches anywhere in the text
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Reuse your existing polymorphism logic
                String category = rs.getString("category");
                Product p;
                if ("Perishable".equalsIgnoreCase(category)) {
                    p = new PerishableProduct(
                        rs.getInt("product_id"), rs.getString("name"), rs.getDouble("price"),
                        rs.getInt("stock_quantity"), rs.getInt("reorder_level"), rs.getDate("expiry_date")
                    );
                } else {
                    p = new NonPerishableProduct(
                        rs.getInt("product_id"), rs.getString("name"), rs.getDouble("price"),
                        rs.getInt("stock_quantity"), rs.getInt("reorder_level")
                    );
                }
                list.add(p);
            }
        }
        return list;
    }
}