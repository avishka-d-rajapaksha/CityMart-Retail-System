package com.inventory.dao;

import com.inventory.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDAOImpl implements DashboardDAO {

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }

    @Override
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM sales";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    @Override
    public int getTotalProducts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int getLowStockCount() throws SQLException {
        // Counts products where stock is lower than the reorder level
        String sql = "SELECT COUNT(*) FROM products WHERE stock_quantity <= reorder_level";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public Map<String, Integer> getProductCategories() throws SQLException {
        Map<String, Integer> data = new HashMap<>();
        // Group by Category (Perishable vs Non-Perishable)
        String sql = "SELECT category, COUNT(*) FROM products GROUP BY category";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString(1), rs.getInt(2));
            }
        }
        return data;
    }

    @Override
    public Map<String, Double> getRecentSales() throws SQLException {
        Map<String, Double> data = new LinkedHashMap<>();
        String sql = "SELECT DATE(sale_date) as sdate, SUM(total_amount) " +
                     "FROM sales " +
                     "GROUP BY DATE(sale_date) " +
                     "ORDER BY sdate DESC LIMIT 7";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                data.put(rs.getString(1), rs.getDouble(2));
            }
        }
        return data;
    }
}