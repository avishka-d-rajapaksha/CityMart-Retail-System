package com.inventory.dao;

import com.inventory.model.Sale;
import com.inventory.model.SaleItem;
import java.util.List;

public interface SalesDAO {
    
 
    boolean processSale(Sale sale, List<SaleItem> items) throws Exception;
}