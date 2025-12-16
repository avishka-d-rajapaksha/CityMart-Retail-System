package com.inventory.dao;

import java.sql.SQLException;
import java.util.Map;

public interface DashboardDAO {
    // KPI Cards
    double getTotalRevenue() throws SQLException;
    int getTotalProducts() throws SQLException;
    int getLowStockCount() throws SQLException;

    // Charts
    Map<String, Integer> getProductCategories() throws SQLException; // For Pie Chart
    Map<String, Double> getRecentSales() throws SQLException;        // For Bar Chart
}