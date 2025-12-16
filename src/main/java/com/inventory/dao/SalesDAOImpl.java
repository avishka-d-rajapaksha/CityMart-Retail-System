package com.inventory.dao;

import com.inventory.model.Sale;
import com.inventory.model.SaleItem;
import com.inventory.service.EmailService; // Make sure this is imported
import com.inventory.util.DBConnection;
import java.sql.*;
import java.util.List;

public class SalesDAOImpl implements SalesDAO {

    @Override
    public boolean processSale(Sale sale, List<SaleItem> items) throws Exception {
        Connection conn = null;
        PreparedStatement psSale = null;
        PreparedStatement psItem = null;
        PreparedStatement psStock = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getInstance().getConnection();
            // 1. Start Transaction
            conn.setAutoCommit(false);

            // 2. Insert Sale Record
            String sqlSale = "INSERT INTO sales (user_id, sale_date, total_amount) VALUES (?, NOW(), ?)";
            psSale = conn.prepareStatement(sqlSale, Statement.RETURN_GENERATED_KEYS);
            psSale.setInt(1, sale.getUserId());
            psSale.setDouble(2, sale.getTotalAmount());

            if (psSale.executeUpdate() == 0) {
                throw new SQLException("Creating sale failed, no rows affected.");
            }

            // Get the generated Sale ID
            int saleId;
            rs = psSale.getGeneratedKeys();
            if (rs.next()) {
                saleId = rs.getInt(1);
            } else {
                throw new SQLException("Creating sale failed, no ID obtained.");
            }

            // 3. Insert Sale Items & Update Stock
            String sqlItem = "INSERT INTO sale_items (sale_id, product_id, product_name, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlStock = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
            
            psItem = conn.prepareStatement(sqlItem);
            psStock = conn.prepareStatement(sqlStock);

            for (SaleItem item : items) {
                // Add Item
                psItem.setInt(1, saleId);
                psItem.setInt(2, item.getProductId());
                psItem.setString(3, item.getProductName()); 
                psItem.setInt(4, item.getQuantity());
                psItem.setDouble(5, item.getUnitPrice());
                psItem.setDouble(6, item.getSubtotal());
                psItem.addBatch();

                // Update Stock
                psStock.setInt(1, item.getQuantity());
                psStock.setInt(2, item.getProductId());
                psStock.addBatch();
            }

            psItem.executeBatch();
            psStock.executeBatch();

            // 4. Commit Transaction (Save everything to DB)
            conn.commit();
            
            // 5. TRIGGER EMAIL ALERT
            // We do this AFTER commit so the sale is safe even if email fails
            checkAndSendAlerts(conn, items);

            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    System.err.println("Transaction rolled back due to error: " + e.getMessage());
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            // Close resources safely
            if (rs != null) rs.close();
            if (psSale != null) psSale.close();
            if (psItem != null) psItem.close();
            if (psStock != null) psStock.close();
            if (conn != null) conn.setAutoCommit(true); // Reset auto-commit
        }
    }

    // Helper to check stock and send email
    private void checkAndSendAlerts(Connection conn, List<SaleItem> items) {
        String sqlCheck = "SELECT name, stock_quantity, reorder_level FROM products WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            for (SaleItem item : items) {
                ps.setInt(1, item.getProductId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("name");
                        int stock = rs.getInt("stock_quantity");
                        int reorder = rs.getInt("reorder_level");

                        // Trigger alert if stock is now Low or Critical
                        if (stock <= reorder) {
                             // Call the Email Service
                             EmailService.sendLowStockAlert(name, stock);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Alert Error: " + e.getMessage());
            // Don't stop the sale transaction just because email failed
        }
    }
}