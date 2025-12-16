package com.inventory.controller;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.ProductDAOImpl;
import com.inventory.model.NonPerishableProduct;
import com.inventory.model.PerishableProduct;
import com.inventory.model.Product;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.sql.Date;

// 1. INHERITANCE: Extends BaseController (Satisfies OOP Requirement)
public class InventoryController extends BaseController {

    @FXML private BorderPane rootPane;
    @FXML private TextField searchField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private DatePicker expiryPicker;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField reorderField;
    @FXML private Label statusLabel;

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, Integer> colReorder;
    @FXML private TableColumn<Product, Date> colExpiry;

    private ProductDAO productDAO = new ProductDAOImpl();
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        colReorder.setCellValueFactory(new PropertyValueFactory<>("reorderLevel"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        loadData();

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) populateFields(newVal);
        });

        categoryCombo.setOnAction(e -> {
            boolean isPerishable = "Perishable".equals(categoryCombo.getValue());
            expiryPicker.setDisable(!isPerishable);
            if (!isPerishable) expiryPicker.setValue(null);
        });


        // 2. FULL SCREEN LOGIC (Satisfies UI Requirement)
        Platform.runLater(() -> {
            if (rootPane.getScene() != null) {
                Stage stage = (Stage) rootPane.getScene().getWindow();
                stage.setMaximized(true); 
                
                // Inherit Shortcuts from BaseController
                super.setupCommonKeyHandlers(rootPane.getScene());
                
                // Inventory Specific Shortcuts
                rootPane.getScene().setOnKeyPressed(event -> {
                    if (searchField.isFocused() || nameField.isFocused() || priceField.isFocused()) return;

                    if (event.getCode() == KeyCode.LEFT) handleBack(null); 
                    if (event.getCode() == KeyCode.RIGHT) handleSales(null); 
                    if (event.getCode() == KeyCode.DELETE) handleDelete(null);
                });
                rootPane.requestFocus();
            }
        });
    }

    private void loadData() {
        try {
            productList.clear();
            productList.addAll(productDAO.getAllProducts());
            productTable.setItems(productList);
        } catch (Exception e) { statusLabel.setText("Database Error: " + e.getMessage()); }
    }

    private void populateFields(Product p) {
        nameField.setText(p.getName());
        categoryCombo.setValue(p.getCategory());
        priceField.setText(String.valueOf(p.getPrice()));
        stockField.setText(String.valueOf(p.getStockQuantity()));
        reorderField.setText(String.valueOf(p.getReorderLevel()));
        if (p instanceof PerishableProduct && p.getExpiryDate() != null) {
            expiryPicker.setValue(p.getExpiryDate().toLocalDate());
        } else {
            expiryPicker.setValue(null);
        }
    }

    @FXML private void handleAdd(ActionEvent event) {
        if (!validateInput()) return;
        try {
            Product p = createProductFromInput(0);
            if (productDAO.addProduct(p)) {
                showInfo("Success", "Product Added Successfully!");
                handleClear(null);
                loadData();
            } else showError("Failed", "Could not add product.");
        } catch (Exception e) { showError("Error", e.getMessage()); }
    }

    @FXML private void handleUpdate(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Selection Error", "Please select a product."); return; }
        if (!validateInput()) return;

        try {
            Product p = createProductFromInput(selected.getProductId());
            if (productDAO.updateProduct(p)) {
                showInfo("Success", "Product Updated Successfully!");
                handleClear(null);
                loadData();
            }
        } catch (Exception e) { showError("Error", e.getMessage()); }
    }

    @FXML private void handleDelete(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        if (showConfirm("Delete", "Delete " + selected.getName() + "?")) {
            try {
                if (productDAO.deleteProduct(selected.getProductId())) {
                    showInfo("Deleted", "Product Removed.");
                    handleClear(null);
                    loadData();
                }
            } catch (Exception e) { showError("Error", e.getMessage()); }
        }
    }

    @FXML private void handleClear(ActionEvent event) {
        nameField.clear(); categoryCombo.getSelectionModel().clearSelection();
        priceField.clear(); stockField.clear(); reorderField.clear(); expiryPicker.setValue(null);
        statusLabel.setText(""); productTable.getSelectionModel().clearSelection();
    }

    // 3. POLYMORPHISM & ABSTRACTION (Satisfies OOP Requirement)
    private Product createProductFromInput(int id) {
        String name = nameField.getText();
        String cat = categoryCombo.getValue();
        double price = Double.parseDouble(priceField.getText());
        int stock = Integer.parseInt(stockField.getText());
        int reorder = Integer.parseInt(reorderField.getText());
        
        if ("Perishable".equals(cat)) {
            Date date = (expiryPicker.getValue() != null) ? Date.valueOf(expiryPicker.getValue()) : null;
            return new PerishableProduct(id, name, price, stock, reorder, date);
        } else {
            return new NonPerishableProduct(id, name, price, stock, reorder);
        }
    }

    private boolean validateInput() {
        if (nameField.getText().isEmpty() || categoryCombo.getValue() == null || 
            priceField.getText().isEmpty() || stockField.getText().isEmpty()) {
            showError("Validation", "Please fill required fields."); return false;
        }
        try {
            Double.parseDouble(priceField.getText());
            Integer.parseInt(stockField.getText());
            Integer.parseInt(reorderField.getText());
        } catch (NumberFormatException e) {
            showError("Validation", "Price and Stock must be numbers."); return false;
        }
        return true;
    }


    
    // 4. OVERRIDING (Satisfies OOP): Only override what needs to change from BaseController
    @Override
    public void handleInventory(ActionEvent event) {
        handleClear(null); // Refresh logic specific to this page
        loadData();
    }
}