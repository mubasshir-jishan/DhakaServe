package com.myapp.controllers;

import com.myapp.utils.DBUtil;
import com.myapp.utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = usernameField.getText().trim();
        String pass  = passwordField.getText();

        if (email.isEmpty() || pass.isEmpty()) {
            showAlert("Error", "Please enter both email and password.");
            return;
        }

        String sql = "SELECT userId, name, userType FROM Users WHERE email=? AND password=?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, pass);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int    id   = rs.getInt("userId");
                String name = rs.getString("name");
                String type = rs.getString("userType");

                Session.set(id, name, type);

                String fxml;
                int width, height;

                if (type.equals("Provider")) {
                    fxml = "/views/provider_dashboard.fxml";
                    width = 950;
                    height = 700;
                } else {
                    fxml = "/views/customer_dashboard.fxml";
                    width = 950;
                    height = 700;
                }

                Parent dashRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(dashRoot, width, height));
                stage.setTitle(type + " Dashboard");
                stage.centerOnScreen();

            } else {
                showAlert("Login Failed", "Invalid email or password.");
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void goToRegister(ActionEvent event) throws IOException {
        Parent regRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/register.fxml")));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        // Set a slightly larger height for the register page to accommodate more fields
        stage.setScene(new Scene(regRoot, 800, 700));
        stage.setTitle("Create Account");
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