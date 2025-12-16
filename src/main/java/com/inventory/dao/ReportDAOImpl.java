package com.inventory.dao;

import com.inventory.model.NonPerishableProduct;
import com.inventory.model.PerishableProduct;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.SaleItem;
import com.inventory.util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDAOImpl implements ReportDAO {

    @Override
    public List<Sale> getSalesReport(LocalDate from, LocalDate to) throws Exception {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT s.sale_id, s.sale_date, s.total_amount, u.username FROM sales s JOIN users u ON s.user_id = u.user_id WHERE DATE(s.sale_date) BETWEEN ? AND ? ORDER BY s.sale_date DESC";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from)); ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Sale s = new Sale(); s.setSaleId(rs.getInt("sale_id")); s.setSaleDate(rs.getTimestamp("sale_date")); s.setTotalAmount(rs.getDouble("total_amount")); s.setCashierName(rs.getString("username"));
                list.add(s);
            }
        }
        return list;
    }

    @Override
    public List<SaleItem> getProductSalesReport(LocalDate from, LocalDate to) throws Exception {
        List<SaleItem> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, SUM(si.quantity) as total_qty, SUM(si.subtotal) as total_rev FROM sale_items si JOIN sales s ON si.sale_id = s.sale_id JOIN products p ON si.product_id = p.product_id WHERE DATE(s.sale_date) BETWEEN ? AND ? GROUP BY p.product_id, p.name ORDER BY total_rev DESC";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from)); ps.setDate(2, Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                SaleItem item = new SaleItem(); item.setProductId(rs.getInt("product_id")); item.setProductName(rs.getString("name")); item.setQuantity(rs.getInt("total_qty")); item.setSubtotal(rs.getDouble("total_rev"));
                list.add(item);
            }
        }
        return list;
    }

    // --- NEW METHODS ---

    @Override
    public List<Product> getLowStockReport() throws Exception {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stock_quantity <= reorder_level";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRowToProduct(rs));
        }
        return list;
    }

    @Override
    public List<Product> getInventoryValuationReport() throws Exception {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY category, name";
        try (Connection conn = DBConnection.getInstance().getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRowToProduct(rs));
        }
        return list;
    }

    // Helper for Polymorphism
    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        String category = rs.getString("category");
        if ("Perishable".equalsIgnoreCase(category)) {
            return new PerishableProduct(rs.getInt("product_id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock_quantity"), rs.getInt("reorder_level"), rs.getDate("expiry_date"));
        } else {
            return new NonPerishableProduct(rs.getInt("product_id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock_quantity"), rs.getInt("reorder_level"));
        }
    }
}