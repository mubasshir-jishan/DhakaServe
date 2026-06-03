package com.myapp.controllers;

import com.myapp.models.Service;
import com.myapp.utils.DBUtil;
import com.myapp.utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class BookingConfirmationController {
    @FXML private Label serviceLabel;
    @FXML private Label providerLabel;
    @FXML private Label priceLabel;
    @FXML private Label dateLabel;

    private Service service;

    public void setService(Service svc) {
        this.service = svc;
        serviceLabel.setText("Service: " + svc.getTitle());
        providerLabel.setText("Provider ID: " + svc.getProviderId());
        priceLabel.setText("Price: ৳" + String.format("%.2f", svc.getPrice()));

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        dateLabel.setText("Date: " + now);
    }

    @FXML
    private void handleConfirm(ActionEvent event) throws IOException {
        String sql = "INSERT INTO Bookings(customerId,serviceId,status,bookingDate) VALUES(?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt   (1, Session.getUserId());
            ps.setInt   (2, service.getServiceId());
            ps.setString(3, "Pending");
            ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Booking successful! The provider has been notified.");
            alert.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Booking failed: " + e.getMessage());
            alert.showAndWait();
        }

        handleCancel(event);
    }

    @FXML
    private void handleCancel(ActionEvent event) throws IOException {
        Parent dash = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/customer_dashboard.fxml")));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(dash, 600, 400));
    }
}
