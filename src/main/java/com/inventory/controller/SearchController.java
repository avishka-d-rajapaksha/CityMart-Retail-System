package com.inventory.controller;

import com.inventory.dao.ProductDAO;
import com.inventory.dao.ProductDAOImpl;
import com.inventory.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class SearchController {

    @FXML private TextField searchField;
    @FXML private TableView<Product> resultsTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private Label statusLabel;

    private ProductDAO productDAO = new ProductDAOImpl();
    private ObservableList<Product> searchResults = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Setup Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        // 2. Enable "Enter" key to search again inside the popup
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleSearch(null);
        });
    }

    // This method is called by BaseController to pass the text from the header
    public void setInitialSearch(String keyword) {
        searchField.setText(keyword);
        performSearch(keyword);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        performSearch(searchField.getText());
    }

    private void performSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return;

        try {
            searchResults.clear();
            // Call the Database
            searchResults.addAll(productDAO.searchProducts(keyword));
            resultsTable.setItems(searchResults);
            
            statusLabel.setText(searchResults.size() + " results found.");
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error searching database.");
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        // Close the popup window
        ((Stage)((Node)event.getSource()).getScene().getWindow()).close();
    }
    
private java.util.function.Consumer<Product> onProductSelected;

    public void setOnProductSelected(java.util.function.Consumer<Product> callback) {
        this.onProductSelected = callback;
    }

    @FXML
    private void handleSelect(ActionEvent event) {
        Product selected = resultsTable.getSelectionModel().getSelectedItem();
        if (selected != null && onProductSelected != null) {
            onProductSelected.accept(selected);
            handleClose(event);
        } else if (selected == null) {
            statusLabel.setText("Please select an item first.");
            statusLabel.setStyle("-fx-text-fill: red;");
        } else {
            handleClose(event); // Just close if no callback
        }
    }
    
}