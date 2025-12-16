package com.inventory.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.IOException;
import java.util.Optional;

public abstract class BaseController {
// Defines what happens when a product is selected in search (e.g., Add to Cart)
protected java.util.function.Consumer<com.inventory.model.Product> searchResultHandler;
    // --- COMMON UI ELEMENTS ---
    // This links to the "searchField" in EVERY FXML file automatically
    @FXML protected TextField searchField; 

    // =======================================================
    //              1. GLOBAL SEARCH LOGIC (NEW)
    // =======================================================

    @FXML
    public void handleGlobalSearch(ActionEvent event) {
        triggerSearch();
    }

  protected void triggerSearch() {
    if (searchField == null) return;
    String keyword = searchField.getText();

    if (keyword != null && !keyword.trim().isEmpty()) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/inventory/view/Search.fxml"));
            Parent root = loader.load();

            SearchController controller = loader.getController();
            controller.setInitialSearch(keyword);

            // *** CONNECT THE HANDLER HERE ***
            if (searchResultHandler != null) {
                controller.setOnProductSelected(searchResultHandler);
            }

            Stage popupStage = new Stage();
            popupStage.setScene(new Scene(root));
            popupStage.setTitle("Global Product Search");
            popupStage.show();

            searchField.clear();

        } catch (IOException e) {
            e.printStackTrace();
            showError("System Error", "Could not open search window.");
        }
    }
}
    
    //              2. NAVIGATION ACTIONS
    

    @FXML public void handleBack(ActionEvent event) { navigate(event, "Dashboard.fxml", "CityMart - Dashboard"); }
    @FXML public void handleInventory(ActionEvent event) { navigate(event, "Inventory.fxml", "CityMart - Inventory"); }
    @FXML public void handleSales(ActionEvent event) { navigate(event, "Billing.fxml", "CityMart - Billing & POS"); }
    @FXML public void handleReports(ActionEvent event) { navigate(event, "Reports.fxml", "CityMart - Reports"); }
    @FXML public void handleUsers(ActionEvent event) { navigate(event, "Users.fxml", "CityMart - User Management"); }

    @FXML
    public void handleLogout(ActionEvent event) {
        if (showConfirm("Logout", "Are you sure you want to logout?")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/inventory/view/Login.fxml"));
                Parent root = loader.load();
                Stage stage = getCurrentStage(event);
                
                if (stage != null) {
                    // *** FORCE WINDOWED MODE FOR LOGIN ***
                    stage.setScene(new Scene(root));
                    stage.setTitle("CityMart - Login");
                    stage.setMaximized(false); // Turn off full screen
                    stage.setWidth(850);       // Reset width
                    stage.setHeight(550);      // Reset height
                    stage.centerOnScreen();    // Center it
                    stage.show();
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    //              3. SCENE SWITCHER (FULL SCREEN ENFORCED)

    protected void navigate(ActionEvent event, String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/inventory/view/" + fxml));
            Parent root = loader.load();
            Stage stage = getCurrentStage(event);
            
            if (stage != null) {
                // Use setRoot to keep the window state smooth (prevents shrinking)
                if (stage.getScene() == null) {
                    stage.setScene(new Scene(root));
                } else {
                    stage.getScene().setRoot(root);
                }
                stage.setTitle(title);
                
                // *** FORCE FULL SCREEN FOR APP PAGES ***
                if (!stage.isMaximized()) {
                    stage.setMaximized(true);
                }
                stage.show();
                
                // Ensure focus for keyboard shortcuts
                root.requestFocus();
            }
        } catch (IOException e) {
            showError("Navigation Error", "Could not load " + fxml);
            e.printStackTrace();
        }
    }

    protected Stage getCurrentStage(ActionEvent event) {
        if (event != null && event.getSource() instanceof Node) {
            Scene scene = ((Node) event.getSource()).getScene();
            if (scene != null) return (Stage) scene.getWindow();
        }
        // Fallback: Find the active window loop (Safe)
        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window instanceof Stage) return (Stage) window;
        }
        return null;
    }

    //              4. GLOBAL SHORTCUTS (Updated)

    public void setupCommonKeyHandlers(Scene scene) {
        if (scene == null) return;

        // A. Global F-Keys and Escape
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case F1: handleBack(null); event.consume(); break;
                case F2: handleInventory(null); event.consume(); break;
                case F3: handleSales(null); event.consume(); break;
                case F4: handleReports(null); event.consume(); break;
                case F5: handleUsers(null); event.consume(); break;
                case ESCAPE: handleLogout(null); event.consume(); break;
            }
        });

        // B. Search Bar Specific Logic (Press ENTER to search)
        if (searchField != null) {
            searchField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    triggerSearch();
                    event.consume();
                }
            });
        }
    }

    //              5. HELPER METHODS (ALERTS)

    protected void showInfo(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setContentText(m); a.showAndWait();
    }
    protected void showError(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setContentText(m); a.showAndWait();
    }
    protected boolean showConfirm(String t, String m) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION); a.setTitle(t); a.setContentText(m);
        Optional<ButtonType> r = a.showAndWait(); return r.isPresent() && r.get() == ButtonType.OK;
    }
}