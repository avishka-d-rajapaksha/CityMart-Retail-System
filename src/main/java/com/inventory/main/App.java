package com.inventory.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.inventory.util.DBConnection; // Import for resource management


public class App extends Application {

   
   @Override

    public void start(Stage stage) {
        try {
            System.out.println(">>> DEBUG: Attempting to load Login.fxml...");
            
            // This is where it crashes usually
            Parent root = FXMLLoader.load(getClass().getResource("/com/inventory/view/Login.fxml"));
            
            System.out.println(">>> DEBUG: FXML Loaded successfully!");
            
            Scene scene = new Scene(root);
            stage.setTitle("CityMart Inventory System");
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            // THIS WILL PRINT THE HIDDEN ERROR
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("CRASH DETECTED IN START METHOD:");
            e.printStackTrace();
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }


    
   
    @Override
    public void stop() {
        DBConnection.getInstance().closeConnection();
        System.out.println("Application Shut Down. Database Connection Closed.");
    }

   
    public static void main(String[] args) {
        launch(args);
    }
}