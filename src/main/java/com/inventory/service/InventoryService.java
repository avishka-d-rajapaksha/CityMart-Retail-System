package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.ProductDAOImpl;
import com.inventory.model.Product;
import java.util.List;

public class InventoryService {

    private ProductDAO productDAO = new ProductDAOImpl();

    public List<Product> getAllProducts() throws Exception {
        return productDAO.getAllProducts();
    }

    public boolean addProduct(Product product) throws Exception {
        // Business Rule: Price and Stock cannot be negative
        if (product.getPrice() < 0 || product.getStockQuantity() < 0) {
            throw new Exception("Price and Stock must be non-negative.");
        }
        return productDAO.addProduct(product);
    }

    public boolean updateProduct(Product product) throws Exception {
        return productDAO.updateProduct(product);
    }

    public boolean deleteProduct(int productId) throws Exception {
        return productDAO.deleteProduct(productId);
    }
    
    public List<Product> searchProducts(String keyword) throws Exception {
        return productDAO.searchProducts(keyword);
    }
}