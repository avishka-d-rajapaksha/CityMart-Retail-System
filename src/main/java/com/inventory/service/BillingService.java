package com.inventory.service;

import com.inventory.dao.SalesDAO;
import com.inventory.dao.SalesDAOImpl;
import com.inventory.model.Sale;
import com.inventory.model.SaleItem;
import com.inventory.model.User;
import java.util.List;

public class BillingService {

    private SalesDAO salesDAO = new SalesDAOImpl();
    private static final double VAT_RATE = 0.05; // 5% VAT

    // --- Calculations ---

    public double calculateSubtotal(List<SaleItem> items) {
        return items.stream().mapToDouble(SaleItem::getSubtotal).sum();
    }

    public double calculateVAT(double subtotal) {
        return subtotal * VAT_RATE;
    }

    public double calculateGrandTotal(double subtotal, double discount) {
        double taxableAmount = subtotal - discount;
        // Typically VAT is applied after discount, or before depending on region. 
        // Assuming VAT on discounted price:
        double vat = calculateVAT(taxableAmount > 0 ? taxableAmount : 0);
        return taxableAmount + vat; 
        
        // OR if VAT is on subtotal:
        // return (subtotal + calculateVAT(subtotal)) - discount;
    }

    // --- Transaction Processing ---

    public boolean checkout(User cashier, List<SaleItem> items, double finalAmount) throws Exception {
        if (items.isEmpty()) {
            throw new Exception("Cart is empty. Add items first.");
        }

        Sale sale = new Sale();
        // If no cashier is logged in (e.g., testing), use ID 1 (Admin) fallback
        sale.setUserId(cashier != null ? cashier.getUserId() : 1);
        sale.setTotalAmount(finalAmount);

        return salesDAO.processSale(sale, items);
    }
}