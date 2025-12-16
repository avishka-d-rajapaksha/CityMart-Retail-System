package com.inventory.controller;

import com.inventory.dao.UserDAO;
import com.inventory.dao.UserDAOImpl;
import com.inventory.model.User;
import com.inventory.model.User.Role;
import com.inventory.util.SecurityUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class SignUpController {

    // --- UI ELEMENTS (Must match FXML fx:id) ---
    @FXML private TextField fullNameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField userField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private PasswordField passField;
    @FXML private PasswordField confirmPassField;
    @FXML private Button signUpBtn;
    @FXML private Label messageLabel; // Used for errors/success messages

    // --- DATA ACCESS ---
    private UserDAO userDAO = new UserDAOImpl();

    @FXML
    public void initialize() {
        // roleCombo.setValue("Cashier");
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        // 1. Capture Data
        String fullName = fullNameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String username = userField.getText().trim();
        String roleStr = roleCombo.getValue();
        String password = passField.getText();
        String confirmPass = confirmPassField.getText();

        // 2. Validate Input
        if (fullName.isEmpty() || address.isEmpty() || phone.isEmpty() || 
            username.isEmpty() || roleStr == null || password.isEmpty()) {
            showMessage("All fields are required.", false);
            return;
        }

        if (!password.equals(confirmPass)) {
            showMessage("Passwords do not match.", false);
            return;
        }

        try {
            // 3. Check if User Exists
            if (userDAO.findUserByUsername(username) != null) {
                showMessage("Username already taken.", false);
                return;
            }

            // 4. Create User Object
            User newUser = new User();
            newUser.setFullName(fullName);
            newUser.setAddress(address);
            newUser.setPhone(phone);
            newUser.setUsername(username);
            
            // Convert String role to Enum safely
            try {
                newUser.setRole(Role.valueOf(roleStr));
            } catch (IllegalArgumentException e) {
                newUser.setRole(Role.Cashier); // Default fallback
            }

            // Hash the password before saving
            newUser.setPasswordHash(SecurityUtil.hashPassword(password));

            // 5. Save to Database
            if (userDAO.addUser(newUser)) {
                showMessage("Registration Successful!", true);
                // Clear fields or redirect? 
                // clearFields(); 
                // backToLogin(event); // Uncomment to auto-redirect
            } else {
                showMessage("Registration Failed.", false);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Database Error: " + e.getMessage(), false);
        }
    }

    @FXML
    private void backToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/inventory/view/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CityMart - Login");
            stage.centerOnScreen(); // Login should be centered, not full screen
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Error loading Login screen.", false);
        }
    }

    // --- HELPER METHODS ---

    private void showMessage(String msg, boolean success) {
        messageLabel.setText(msg);
        if (success) {
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #dc3545;"); // Red
        }
    }
    
    private void clearFields() {
        fullNameField.clear();
        addressField.clear();
        phoneField.clear();
        userField.clear();
        roleCombo.getSelectionModel().clearSelection();
        passField.clear();
        confirmPassField.clear();
    }
}