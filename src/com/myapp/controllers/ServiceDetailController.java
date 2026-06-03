package com.myapp.controllers;

import com.myapp.models.Service;
import com.myapp.utils.DBUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class ServiceDetailController {

    @FXML private Label titleLabel;
    @FXML private Label providerLabel;
    @FXML private Label priceLabel;
    @FXML private Label ratingLabel;
    @FXML private TextArea descArea;
    @FXML private ImageView thumbnailView;
    @FXML private Button bookButton;

    private Service service;

    public void setService(Service service) {
        this.service = service;

        titleLabel.setText(service.getTitle());
        priceLabel.setText("৳ " + String.format("%.2f", service.getPrice()));
        descArea.setText(service.getDescription());
        ratingLabel.setText(String.format("%.1f ★", service.getProviderRating()));

        // Display thumbnail
        Image imageToShow = null;
        if (service.getThumbnailPath() != null && !service.getThumbnailPath().isEmpty()) {
            File file = new File(service.getThumbnailPath());
            if (file.exists()) {
                imageToShow = new Image(file.toURI().toString());
            }
        }
        if (imageToShow == null) {
            try (InputStream is = getClass().getResourceAsStream("/assets/placeholder.png")) {
                if (is != null) imageToShow = new Image(is);
            } catch (Exception e) {
                // Placeholder not found, leave it blank
            }
        }
        thumbnailView.setImage(imageToShow);

        // Fetch and display provider's name
        String providerName = getProviderName(service.getProviderId());
        providerLabel.setText(providerName);
    }

    private String getProviderName(int providerId) {
        String sql = "SELECT name FROM Users WHERE userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Provider";
    }

    @FXML
    private void handleBook(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/views/booking_confirmation.fxml")));
            Parent root = loader.load();

            BookingConfirmationController bc = loader.getController();
            bc.setService(this.service);

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 450, 350));
            stage.setTitle("Confirm Booking");
            stage.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        Parent dash = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/customer_dashboard.fxml")));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(dash, 950, 700));
        stage.centerOnScreen();
    }
}
