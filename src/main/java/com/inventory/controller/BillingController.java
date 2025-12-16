package com.inventory.controller;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.ProductDAOImpl;
import com.inventory.model.Product;
import com.inventory.model.SaleItem;
import com.inventory.model.User;
import com.inventory.service.BillingService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.List;

public class BillingController extends BaseController {

    @FXML private BorderPane rootPane;
    // searchField is inherited from BaseController

    @FXML private TextField qtyField;
    @FXML private TextField cashField;
    @FXML private TextField discountField;

    @FXML private Label lblSubtotal;
    @FXML private Label lblVat;
    @FXML private Label lblDiscount;
    @FXML private Label lblGrandTotal;
    @FXML private Label lblBalance;
    @FXML private Label lblTransId;

    @FXML private TableView<SaleItem> cartTable;
    @FXML private TableColumn<SaleItem, Integer> colId;
    @FXML private TableColumn<SaleItem, String> colName;
    @FXML private TableColumn<SaleItem, Double> colPrice;
    @FXML private TableColumn<SaleItem, Integer> colQty;
    @FXML private TableColumn<SaleItem, Double> colTotal;

    private ProductDAO productDAO = new ProductDAOImpl();
    private BillingService billingService = new BillingService();
    private ObservableList<SaleItem> cartList = FXCollections.observableArrayList();
    private User loggedInUser; 

    @FXML
    public void initialize() {
        // 1. Setup Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName")); 
        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        cartTable.setItems(cartList);

        loggedInUser = new User();
        loggedInUser.setUserId(1); 

        // 3. Listeners
        discountField.textProperty().addListener((obs, old, newVal) -> updateTotals());
        cashField.textProperty().addListener((obs, old, newVal) -> updateBalance());

        // 4. Search Bar Action
        searchField.setOnAction(e -> handleScanOrSearch());

        // 5. Link Global Search to Cart
        this.searchResultHandler = this::addToCart;

        // 6. Full Screen & Shortcuts
        Platform.runLater(() -> {
            if (rootPane.getScene() != null) {
                ((Stage) rootPane.getScene().getWindow()).setMaximized(true);
                
                // Inherit F1-F5
                super.setupCommonKeyHandlers(rootPane.getScene());
                
                // BILLING SPECIFIC SHORTCUTS
                rootPane.getScene().setOnKeyPressed(e -> {
                    boolean isTyping = searchField.isFocused() || qtyField.isFocused() || cashField.isFocused();

                    switch (e.getCode()) {
                        // Navigation (Only if NOT typing)
                        case LEFT: 
                            if (!isTyping) handleInventory(null); 
                            break;
                        case RIGHT: 
                            if (!isTyping) handleReports(null); 
                            break;
                        
                        // Actions (Always work)
                        case F10: 
                            handleCheckout(null); 
                            e.consume();
                            break;
                        case DELETE: 
                            // Delete if table has focus OR if not typing in a field
                            if (cartTable.isFocused() || !isTyping) handleRemoveItem(null);
                            break;
                    }
                });
                
                searchField.requestFocus();
            }
        });
    }

    private void handleScanOrSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) return;

        try {
            List<Product> results = productDAO.searchProducts(query);
            
            if (results.size() == 1) {
                addToCart(results.get(0));
            } else if (results.size() > 1) {
                triggerSearch(); // Open popup
            } else {
                showError("Not Found", "Product not found.");
            }
        } catch (Exception e) {
            showError("Error", "Database error: " + e.getMessage());
        }
    }
    
    // *** UPDATED ADD TO CART METHOD ***
    private void addToCart(Product p) {
        int qty = 1;
        try {
            qty = Integer.parseInt(qtyField.getText());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Invalid Quantity", "Please enter a valid positive number.");
            return;
        }

        if (p.getStockQuantity() < qty) {
            showError("Stock Alert", "Insufficient Stock! Available: " + p.getStockQuantity());
            return;
        }

        // Logic: Check if item exists, update it, or add new
        boolean exists = false;
        for (SaleItem item : cartList) {
            if (item.getProductId() == p.getProductId()) {
                int newQty = item.getQuantity() + qty;
                if (p.getStockQuantity() < newQty) {
                    showError("Stock Alert", "Cannot add more. Limit reached.");
                    return;
                }
                item.setQuantity(newQty);
                item.setSubtotal(newQty * item.getUnitPrice());
                cartTable.refresh();
                exists = true;
                break;
            }
        }

        if (!exists) {
            SaleItem newItem = new SaleItem();
            newItem.setProductId(p.getProductId());
            newItem.setProductName(p.getName());
            newItem.setUnitPrice(p.getPrice());
            newItem.setQuantity(qty);
            newItem.setSubtotal(p.getPrice() * qty);
            cartList.add(newItem);
        }

        // *** FIX: RESET FIELDS HERE ***
        updateTotals();
        searchField.clear();      // Clear Search
        qtyField.setText("1");    // Reset Qty to 1
        searchField.requestFocus(); // Ready for next scan
    }
    
    private void updateTotals() {
        double subtotal = billingService.calculateSubtotal(cartList);
        double discount = 0;
        try {
            if (!discountField.getText().isEmpty()) 
                discount = Double.parseDouble(discountField.getText());
        } catch (NumberFormatException e) {}

        double grandTotal = billingService.calculateGrandTotal(subtotal, discount);
        double vat = billingService.calculateVAT(subtotal - discount);

        lblSubtotal.setText(String.format("%.2f", subtotal));
        lblVat.setText(String.format("%.2f", vat));
        lblDiscount.setText(String.format("- %.2f", discount));
        lblGrandTotal.setText(String.format("Rs %.2f", grandTotal));
        
        updateBalance();
    }

    private void updateBalance() {
        try {
            double cash = Double.parseDouble(cashField.getText());
            double total = Double.parseDouble(lblGrandTotal.getText().replace("Rs ", ""));
            double balance = cash - total;
            lblBalance.setText(String.format("%.2f", balance));
            
            if (balance < 0) lblBalance.setStyle("-fx-text-fill: red;");
            else lblBalance.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            lblBalance.setText("0.00");
        }
    }

    @FXML private void handleAddToCart(ActionEvent event) { handleScanOrSearch(); }

    @FXML private void handleRemoveItem(ActionEvent event) {
        SaleItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartList.remove(selected);
            updateTotals();
        } else {
            showError("Selection", "Select an item to remove.");
        }
    }

    @FXML private void handleClear(ActionEvent event) {
        cartList.clear(); searchField.clear(); qtyField.setText("1");
        cashField.clear(); discountField.clear(); updateTotals(); lblBalance.setText("0.00");
        searchField.requestFocus();
    }

    @FXML private void handleCheckout(ActionEvent event) {
        if (cartList.isEmpty()) { showError("Empty Cart", "Cannot checkout empty cart."); return; }
        try {
            double total = Double.parseDouble(lblGrandTotal.getText().replace("Rs ", ""));
            try {
                double cash = Double.parseDouble(cashField.getText());
                if (cash < total) { showError("Payment Error", "Insufficient Cash!"); return; }
            } catch (NumberFormatException e) { showError("Payment Error", "Enter valid cash."); return; }

            boolean success = billingService.checkout(loggedInUser, cartList, total);
            if (success) {
                showInfo("Success", "Transaction Completed!");
                handleClear(null);
            } else showError("Failed", "Transaction failed.");
        } catch (Exception e) { e.printStackTrace(); showError("System Error", e.getMessage()); }
    }
    
    public void setLoggedInUser(User user) { this.loggedInUser = user; }
}