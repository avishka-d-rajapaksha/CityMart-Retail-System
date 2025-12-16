package com.inventory.dao;

import com.inventory.model.Product;
import java.util.List;

public interface ProductDAO {
    List<Product> getAllProducts() throws Exception;
    boolean addProduct(Product product) throws Exception;
    boolean updateProduct(Product product) throws Exception;
    boolean deleteProduct(int productId) throws Exception;
    
    List<Product> searchProducts(String keyword) throws Exception;
}