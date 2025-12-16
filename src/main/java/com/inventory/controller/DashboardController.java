package com.inventory.controller;

import com.inventory.dao.DashboardDAO;
import com.inventory.dao.DashboardDAOImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DashboardController extends BaseController {

    @FXML private BorderPane mainContainer;
    @FXML private Label dateTimeLabel;
    @FXML private Label lblTotalSales;
    @FXML private Label lblLowStock;
    @FXML private Label lblTotalProducts;
    @FXML private BarChart<String, Number> salesChart;
    @FXML private PieChart stockChart;

    private DashboardDAO dashboardDAO = new DashboardDAOImpl();

    @FXML
    public void initialize() {
        if (dateTimeLabel != null) 
            dateTimeLabel.setText(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy").format(LocalDateTime.now()));
        
        loadDashboardData();

        Platform.runLater(() -> {
            if (mainContainer.getScene() != null) {
                ((Stage) mainContainer.getScene().getWindow()).setMaximized(true);
                super.setupCommonKeyHandlers(mainContainer.getScene());
                
                // Dashboard Specific Arrows
                mainContainer.getScene().setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.RIGHT) handleInventory(null);
                    if (e.getCode() == KeyCode.LEFT) handleUsers(null);
                });
                mainContainer.requestFocus();
            }
        });
    }

    private void loadDashboardData() {
        try {
            lblTotalSales.setText(String.format("Rs %.2f", dashboardDAO.getTotalRevenue()));
            lblTotalProducts.setText(String.valueOf(dashboardDAO.getTotalProducts()));
            int low = dashboardDAO.getLowStockCount();
            lblLowStock.setText(low + " Items");
            lblLowStock.setStyle(low > 0 ? "-fx-text-fill: #dc3545;" : "-fx-text-fill: #28a745;");

            // Charts
            Map<String, Integer> cats = dashboardDAO.getProductCategories();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            cats.forEach((k, v) -> pieData.add(new PieChart.Data(k, v)));
            stockChart.setData(pieData);

            Map<String, Double> sales = dashboardDAO.getRecentSales();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Daily Sales");
            sales.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
            salesChart.getData().clear();
            salesChart.getData().add(series);
        } catch (Exception e) { e.printStackTrace(); }
    }
}