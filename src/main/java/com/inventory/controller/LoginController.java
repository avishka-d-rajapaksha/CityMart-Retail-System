package com.inventory.controller;

import com.inventory.dao.UserDAO;
import com.inventory.dao.UserDAOImpl;
import com.inventory.model.User;
import com.inventory.model.User.Role;
import com.inventory.service.EmailService; // Ensure this import is present
import com.inventory.util.SecurityUtil;

import java.io.IOException;
import java.util.Optional;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.scene.input.KeyCode; 

public class LoginController {

    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private TextField showPassField; 
    @FXML private Button loginBtn;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAOImpl();

    @FXML
    public void initialize() {
        // 1. Sync Password Fields for "Eye" Icon
        if (showPassField != null) {
            passField.textProperty().bindBidirectional(showPassField.textProperty());
        }

        // 2. Setup Keyboard Shortcuts (ENTER to Login)
        Platform.runLater(() -> {
            if (loginBtn.getScene() != null) {
                loginBtn.getScene().setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case ENTER:
                            // Only attempt login if no dialog is open and button is enabled
                            if (!loginBtn.isDisabled() && !loginBtn.isPressed()) {
                                System.out.println("ENTER Key: Logging in...");
                                handleLogin(new ActionEvent(loginBtn, null));
                            }
                            break;
                        case ESCAPE:
                            System.out.println("ESC Key: Exiting...");
                            Platform.exit();
                            break;
                    }
                });
            }
        });
    }

    @FXML
    private void togglePassword(ActionEvent event) {
        if (passField.isVisible()) {
            passField.setVisible(false);
            showPassField.setVisible(true);
        } else {
            showPassField.setVisible(false);
            passField.setVisible(true);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = userField.getText().trim();
        String password = passField.getText(); 

        try {
            User user = userDAO.findUserByUsername(username);

            if (user != null) {
                // Verify Hash
                if (SecurityUtil.verifyPassword(password, user.getPasswordHash())) {
                    System.out.println("Login Success: " + username);
                    
                    // This satisfies the "Optional Enhancement" for logging/notifications
                    EmailService.sendLoginAlert(user.getUsername(), user.getRole().toString());

                    loadDashboard(event, user);
                } else {
                    errorLabel.setText("Invalid username or password.");
                }
            } else {
                errorLabel.setText("Invalid username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Database Error.");
        }
    }

    //       SECURE SIGN-UP LOGIC (POPUP)

    @FXML
    private void handleSignUp(ActionEvent event) {
        // 1. Create Dialog
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Admin Authorization");
        dialog.setHeaderText("Manager Approval Required");
        dialog.setContentText("Please enter Manager credentials:");

        ButtonType loginButtonType = new ButtonType("Authorize", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // 2. Create Fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Manager Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Manager Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(username::requestFocus);

        // 3. Convert Result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(credentials -> {
            checkAdminAndOpenSignUp(credentials.getKey(), credentials.getValue(), event);
        });
    }

    private void checkAdminAndOpenSignUp(String username, String password, ActionEvent event) {
        try {
            User admin = userDAO.findUserByUsername(username);

            if (admin != null) {
                if (admin.getRole() == Role.Manager) {
                    if (SecurityUtil.verifyPassword(password, admin.getPasswordHash())) {
                        openSignUpScreen(event);
                    } else {
                        errorLabel.setText("Authorization Failed: Wrong Password");
                    }
                } else {
                    errorLabel.setText("Authorization Failed: Must be a Manager");
                }
            } else {
                errorLabel.setText("Authorization Failed: User not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Database Error.");
        }
    }

    //       SCENE TRANSITIONS

    private void openSignUpScreen(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/inventory/view/SignUp.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CityMart - Create New Account");
            
            // *** WINDOWED MODE FOR SIGN UP ***
            stage.setMaximized(false);
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Error loading Sign Up screen.");
        }
    }

    private void loadDashboard(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/inventory/view/Dashboard.fxml"));
            Parent root = loader.load();
            

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("CityMart - Dashboard");
            
            // *** FULL SCREEN MODE FOR DASHBOARD ***
            stage.setMaximized(true); 
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Error loading Dashboard.");
        }
    }
}