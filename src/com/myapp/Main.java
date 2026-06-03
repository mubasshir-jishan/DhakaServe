package com.myapp;

import com.myapp.utils.DBUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        // 1) Initialize our SQLite database & tables
        DBUtil.initialize();

        // 2) Load the login screen
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/login.fxml")));

        // --- MODIFIED LINE ---
        // Increased window size for a better layout
        Scene scene = new Scene(root, 800, 600); // Formerly 550, 450

        primaryStage.setTitle("Service Booking - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // This launches the JavaFX application thread
        launch(args);
    }
}