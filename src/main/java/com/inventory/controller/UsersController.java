package com.inventory.controller;

import com.inventory.dao.UserDAO;
import com.inventory.dao.UserDAOImpl;
import com.inventory.model.User;
import com.inventory.model.User.Role;
import com.inventory.util.SecurityUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Optional;

public class UsersController extends BaseController {

    @FXML private BorderPane rootPane;
    @FXML private TextField searchField;

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label statusLabel;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Timestamp> colCreated;

    private UserDAO userDAO = new UserDAOImpl();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadData();

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) populateFields(newVal);
        });

        setupSearch();

        Platform.runLater(() -> {
            if (rootPane.getScene() != null) {
                ((Stage) rootPane.getScene().getWindow()).setMaximized(true);
                super.setupCommonKeyHandlers(rootPane.getScene());
                
                rootPane.getScene().setOnKeyPressed(e -> {
                    if (searchField.isFocused() || usernameField.isFocused() || passwordField.isFocused()) return;
                    if (e.getCode() == KeyCode.LEFT) handleReports(null);
                    if (e.getCode() == KeyCode.RIGHT) handleBack(null);
                    if (e.getCode() == KeyCode.DELETE) handleDelete(null);
                });
                rootPane.requestFocus();
            }
        });
    }

    //                  SECURE CRUD OPERATIONS

    private void loadData() {
        try {
            userList.clear();
            userList.addAll(userDAO.getAllUsers());
            userTable.setItems(userList);
            setupSearch();
        } catch (Exception e) { statusLabel.setText("DB Error"); }
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        if (!validateInput(true)) return;

        // 1. Security Check
        if (!authenticateManager()) {
            showError("Access Denied", "Manager authorization required.");
            return;
        }

        try {
            User u = new User();
            u.setUsername(usernameField.getText());
            u.setFullName(fullNameField.getText());
            u.setRole(Role.valueOf(roleCombo.getValue()));
            u.setPasswordHash(SecurityUtil.hashPassword(passwordField.getText()));

            if (userDAO.addUser(u)) {
                showInfo("Success", "User Added!");
                handleClear(null); loadData();
            } else showError("Error", "Failed to add user.");
        } catch (Exception e) { showError("Error", e.getMessage()); }
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select User", "Please select a user to update."); return; }
        if (!validateInput(false)) return;

        // 1. Security Check
        if (!authenticateManager()) {
            showError("Access Denied", "Manager authorization required to update users.");
            return;
        }

        try {
            selected.setFullName(fullNameField.getText());
            selected.setRole(Role.valueOf(roleCombo.getValue()));
            if (!passwordField.getText().isEmpty()) 
                selected.setPasswordHash(SecurityUtil.hashPassword(passwordField.getText()));

            if (userDAO.updateUser(selected)) {
                showInfo("Success", "User Updated!");
                handleClear(null); loadData();
            }
        } catch (Exception e) { showError("Error", e.getMessage()); }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select User", "Please select a user to delete."); return; }
        
        if ("admin".equalsIgnoreCase(selected.getUsername())) {
            showError("Action Denied", "Cannot delete Main Admin.");
            return;
        }

        // 1. Security Check
        if (!authenticateManager()) {
            showError("Access Denied", "Manager authorization required to delete users.");
            return;
        }

        if (showConfirm("Delete", "Permanently delete user " + selected.getUsername() + "?")) {
            try {
                if (userDAO.deleteUser(selected.getUserId())) {
                    showInfo("Deleted", "User Removed.");
                    handleClear(null); loadData();
                }
            } catch (Exception e) { showError("Error", e.getMessage()); }
        }
    }

    // --- SECURITY POPUP HELPER ---
    private boolean authenticateManager() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Security Check");
        dialog.setHeaderText("Manager Authorization Required");

        ButtonType loginBtn = new ButtonType("Authorize", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        TextField username = new TextField(); username.setPromptText("Manager Username");
        PasswordField password = new PasswordField(); password.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0); grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1); grid.add(password, 1, 1);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(username::requestFocus);

        dialog.setResultConverter(btn -> btn == loginBtn ? new Pair<>(username.getText(), password.getText()) : null);
        Optional<Pair<String, String>> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                User dbUser = userDAO.findUserByUsername(result.get().getKey());
                if (dbUser != null && dbUser.getRole() == Role.Manager) {
                    return SecurityUtil.verifyPassword(result.get().getValue(), dbUser.getPasswordHash());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        return false;
    }

    @FXML private void handleClear(ActionEvent event) {
        usernameField.clear(); fullNameField.clear(); passwordField.clear();
        roleCombo.getSelectionModel().clearSelection();
        userTable.getSelectionModel().clearSelection();
        usernameField.setDisable(false);
        statusLabel.setText("");
    }

    private void populateFields(User u) {
        usernameField.setText(u.getUsername());
        usernameField.setDisable(true);
        fullNameField.setText(u.getFullName());
        if (u.getRole() != null) roleCombo.setValue(u.getRole().toString());
        passwordField.clear();
    }

    private boolean validateInput(boolean passReq) {
        if (usernameField.getText().isEmpty() || roleCombo.getValue() == null) return false;
        if (passReq && passwordField.getText().isEmpty()) return false;
        return true;
    }

    private void setupSearch() {
        FilteredList<User> filtered = new FilteredList<>(userList, p -> true);
        searchField.textProperty().addListener((obs, old, newVal) -> {
            filtered.setPredicate(u -> newVal == null || newVal.isEmpty() || 
                u.getUsername().toLowerCase().contains(newVal.toLowerCase()));
        });
        userTable.setItems(filtered);
    }

    @Override
    public void handleUsers(ActionEvent event) { handleClear(null); loadData(); }
}