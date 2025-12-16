package com.inventory.controller;

import com.inventory.dao.ReportDAO;
import com.inventory.dao.ReportDAOImpl;
import com.inventory.dao.UserDAO;
import com.inventory.dao.UserDAOImpl;
import com.inventory.model.Product;
import com.inventory.model.Sale;
import com.inventory.model.SaleItem;
import com.inventory.model.User;
import com.inventory.model.User.Role;
import com.inventory.service.EmailService; 
import com.inventory.util.SecurityUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ReportsController extends BaseController {

    @FXML private BorderPane rootPane;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private Label statusLabel;
    
    // Printable Area (For Screen Preview only)
    @FXML private VBox printableArea;
    @FXML private Label lblDateRange;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblGeneratedBy;

    // Table
    @FXML private TableView<Object> reportTable;
    @FXML private TableColumn<Object, Object> colId;
    @FXML private TableColumn<Object, Object> colDate; 
    @FXML private TableColumn<Object, Object> colCashier; 
    @FXML private TableColumn<Object, Object> colTotal;

    private ReportDAO reportsDAO = new ReportDAOImpl();
    private UserDAO userDAO = new UserDAOImpl();
    private ObservableList<Object> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (reportTypeCombo != null) {
            reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Daily Sales Report", "Product Performance Report", "Low Stock Alerts", "Inventory Valuation"
            ));
            reportTypeCombo.getSelectionModel().selectFirst();
        }

        fromDate.setValue(LocalDate.now().withDayOfMonth(1));
        toDate.setValue(LocalDate.now());
        reportTable.setItems(tableData);
        
        Platform.runLater(() -> {
            if (rootPane.getScene() != null) {
                ((Stage) rootPane.getScene().getWindow()).setMaximized(true);
                super.setupCommonKeyHandlers(rootPane.getScene());
                rootPane.getScene().setOnKeyPressed(e -> {
                    if (searchField.isFocused()) return;
                    if (e.getCode() == KeyCode.LEFT) handleSales(null);
                    if (e.getCode() == KeyCode.RIGHT) handleUsers(null);
                    if (e.isControlDown() && e.getCode() == KeyCode.P) handlePrint(null);
                    if (e.isControlDown() && e.getCode() == KeyCode.E) handleEmail(null);
                });
                rootPane.requestFocus();
            }
        });
        handleGenerate(null);
    }

    @FXML
    private void handleGenerate(ActionEvent event) {
        if (authenticateManager()) generateReportData();
        else { statusLabel.setText("Authentication Failed."); statusLabel.setStyle("-fx-text-fill: red;"); }
    }

    private boolean authenticateManager() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Security Check");
        dialog.setHeaderText("Manager Authorization Needed");
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
                if (dbUser != null && dbUser.getRole() == Role.Manager) 
                    return SecurityUtil.verifyPassword(result.get().getValue(), dbUser.getPasswordHash());
            } catch (Exception e) { e.printStackTrace(); }
        }
        return false;
    }

    private void generateReportData() {
        try {
            String reportType = reportTypeCombo.getValue();
            LocalDate from = fromDate.getValue();
            LocalDate to = toDate.getValue();
            tableData.clear();

            if ("Daily Sales Report".equals(reportType)) {
                configureTable("SALE ID", "saleId", "DATE", "saleDate", "CASHIER", "cashierName", "TOTAL (Rs)", "totalAmount");
                List<Sale> sales = reportsDAO.getSalesReport(from, to);
                tableData.addAll(sales);
                lblTotalRevenue.setText(String.format("Rs %.2f", sales.stream().mapToDouble(Sale::getTotalAmount).sum()));
            } else if ("Product Performance Report".equals(reportType)) {
                configureTable("PROD ID", "productId", "NAME", "productName", "QTY SOLD", "quantity", "REVENUE (Rs)", "subtotal");
                List<SaleItem> items = reportsDAO.getProductSalesReport(from, to);
                tableData.addAll(items);
                lblTotalRevenue.setText(String.format("Rs %.2f", items.stream().mapToDouble(SaleItem::getSubtotal).sum()));
            } else if ("Low Stock Alerts".equals(reportType)) {
                configureTable("ID", "productId", "NAME", "name", "STOCK", "stockQuantity", "ALERT LEVEL", "reorderLevel");
                List<Product> items = reportsDAO.getLowStockReport();
                tableData.addAll(items);
                lblTotalRevenue.setText(items.size() + " Items"); 
            } else if ("Inventory Valuation".equals(reportType)) {
                configureTable("ID", "productId", "NAME", "name", "STOCK", "stockQuantity", "PRICE (Rs)", "price");
                List<Product> items = reportsDAO.getInventoryValuationReport();
                tableData.addAll(items);
                lblTotalRevenue.setText(String.format("Rs %.2f", items.stream().mapToDouble(p -> p.getPrice() * p.getStockQuantity()).sum()));
            }
            lblDateRange.setText(reportType + " (" + LocalDate.now() + ")");
            statusLabel.setText(tableData.size() + " records.");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) { e.printStackTrace(); statusLabel.setText("Error: " + e.getMessage()); }
    }
    
    private void configureTable(String t1, String p1, String t2, String p2, String t3, String p3, String t4, String p4) {
        colId.setText(t1); colId.setCellValueFactory(new PropertyValueFactory<>(p1));
        colDate.setText(t2); colDate.setCellValueFactory(new PropertyValueFactory<>(p2));
        colCashier.setText(t3); colCashier.setCellValueFactory(new PropertyValueFactory<>(p3));
        colTotal.setText(t4); colTotal.setCellValueFactory(new PropertyValueFactory<>(p4));
    }

    // --- 1. GENERATE HTML (Now delegates to EmailService) ---
    private String generateReportHtml() {
        try {
            String reportType = reportTypeCombo.getValue();
            
            // Build the table rows manually
            StringBuilder rows = new StringBuilder();
            for (Object item : tableData) {
                rows.append("<tr>");
                if (item instanceof Sale) {
                    Sale s = (Sale) item;
                    rows.append("<td>#").append(s.getSaleId()).append("</td><td>").append(s.getSaleDate()).append("</td><td>").append(s.getCashierName()).append("</td><td style='text-align:right'>").append(String.format("%.2f", s.getTotalAmount())).append("</td>");
                } else if (item instanceof SaleItem) {
                    SaleItem si = (SaleItem) item;
                    rows.append("<td>").append(si.getProductId()).append("</td><td>").append(si.getProductName()).append("</td><td style='text-align:center'>").append(si.getQuantity()).append("</td><td style='text-align:right'>").append(String.format("%.2f", si.getSubtotal())).append("</td>");
                } else if (item instanceof Product) {
                    Product p = (Product) item;
                    String lastCol = "Low Stock Alerts".equals(reportType) ? String.valueOf(p.getReorderLevel()) : String.format("%.2f", p.getPrice());
                    rows.append("<td>").append(p.getProductId()).append("</td><td>").append(p.getName()).append("</td><td style='text-align:center'>").append(p.getStockQuantity()).append("</td><td style='text-align:right'>").append(lastCol).append("</td>");
                }
                rows.append("</tr>");
            }

            // Get formatted Total Revenue string
            String totalRevStr = lblTotalRevenue.getText().replace("Rs ", "");

            // ASK EmailService to combine this with the template
            return EmailService.buildReportHtml(
                reportType,
                rows.toString(),
                fromDate.getValue().toString(),
                toDate.getValue().toString(),
                totalRevStr
            );

        } catch (Exception e) {
            e.printStackTrace();
            showError("Generate Error", e.getMessage());
            return null;
        }
    }

    @FXML
    private void handlePrint(ActionEvent event) {
        String html = generateReportHtml();
        if (html != null) printHtml(html);
    }

    @FXML
    private void handleEmail(ActionEvent event) {
        String html = generateReportHtml();
        if (html != null) {
            String type = reportTypeCombo.getValue();
            // Use the new public method we added to EmailService
            EmailService.sendHtmlEmail("Report: " + type, html);
            showInfo("Email Sent", "The " + type + " has been emailed to the Manager.");
        }
    }

    private void printHtml(String htmlContent) {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.loadContent(htmlContent);
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                PrinterJob job = PrinterJob.createPrinterJob();
                if (job != null && job.showPrintDialog(rootPane.getScene().getWindow())) {
                    engine.print(job);
                    job.endJob();
                    showInfo("Success", "Sent to printer.");
                }
            }
        });
    }
}