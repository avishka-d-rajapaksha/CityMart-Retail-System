package com.inventory.dao;

import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.SaleItem;
import java.time.LocalDate;
import java.util.List;

public interface ReportDAO {
    List<Sale> getSalesReport(LocalDate from, LocalDate to) throws Exception;
    List<SaleItem> getProductSalesReport(LocalDate from, LocalDate to) throws Exception;
    
    // --- NEW METHODS ---
    List<Product> getLowStockReport() throws Exception;
    List<Product> getInventoryValuationReport() throws Exception;
}