package com.myapp.controllers;

import com.myapp.utils.DBUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passField;
    @FXML private ChoiceBox<String> typeChoice;

    @FXML
    private void initialize() {
        // Set the items for the ChoiceBox
        typeChoice.setItems(FXCollections.observableArrayList("Customer", "Provider"));
        typeChoice.getSelectionModel().selectFirst(); // Default to "Customer"
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass  = passField.getText();
        String type  = typeChoice.getValue();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || type == null) {
            showAlert("Error", "All fields are required.");
            return;
        }

        String sql = "INSERT INTO Users(name,email,password,userType) VALUES(?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, pass);
            ps.setString(4, type);
            ps.executeUpdate();

            showAlert("Success", "Registration successful! Please log in.");
            goToLogin(event);

        } catch (SQLException e) {
            showAlert("Registration Failed", "An account with this email may already exist.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) throws IOException {
        Parent loginRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/login.fxml")));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loginRoot, 800, 650)); // Match login screen size
        stage.setTitle("Service Booking - Login");
        stage.centerOnScreen();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}