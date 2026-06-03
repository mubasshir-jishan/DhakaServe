package com.myapp.controllers;

import com.myapp.models.Booking;
import com.myapp.models.Service;
import com.myapp.utils.DBUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class PaymentController {

    @FXML private Label serviceTitleLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label feeLabel;
    @FXML private ToggleGroup paymentMethodGroup;
    @FXML private RadioButton bkashRadio;
    @FXML private RadioButton nogadRadio;
    @FXML private RadioButton cardRadio;
    @FXML private Label paymentInstructionLabel;
    @FXML private TextField paymentField;

    private Booking booking;
    private double servicePrice;
    private static final double PLATFORM_FEE_PERCENTAGE = 0.10; // 10%

    @FXML
    public void initialize() {
        // Add a listener to change the instruction text based on selection
        paymentMethodGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == bkashRadio) {
                paymentInstructionLabel.setText("Enter your bKash Transaction ID");
                paymentField.setPromptText("e.g., ABC123XYZ");
            } else if (newValue == nogadRadio) {
                paymentInstructionLabel.setText("Enter your Nogad Transaction ID");
                paymentField.setPromptText("e.g., 123XYZABC");
            } else if (newValue == cardRadio) {
                paymentInstructionLabel.setText("Enter your Card Number (simulated)");
                paymentField.setPromptText("e.g., 1234-5678-9012-3456");
            }
        });
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
        fetchServicePrice();
        double platformFee = servicePrice * PLATFORM_FEE_PERCENTAGE;
        double totalAmount = servicePrice + platformFee;

        serviceTitleLabel.setText("Service: " + booking.getTitle());
        totalAmountLabel.setText(String.format("Total Amount: ৳%.2f", totalAmount));
        feeLabel.setText(String.format("Includes 10%% Platform Fee: ৳%.2f", platformFee));
    }

    private void fetchServicePrice() {
        String sql = "SELECT price FROM Services WHERE serviceId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booking.getServiceId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.servicePrice = rs.getDouble("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePayNow(ActionEvent event) throws IOException {
        String transactionDetails = paymentField.getText().trim();
        if (transactionDetails.isEmpty()) {
            showAlert("Error", "Payment details cannot be empty.");
            return;
        }

        RadioButton selectedRadio = (RadioButton) paymentMethodGroup.getSelectedToggle();
        String paymentMethod = selectedRadio.getText();

        double platformFee = servicePrice * PLATFORM_FEE_PERCENTAGE;
        double payoutAmount = servicePrice; // The provider gets the original price
        double totalAmount = servicePrice + platformFee;

        String paymentSql = "INSERT INTO Payments(bookingId, amountPaid, platformFee, payoutAmount, paymentMethod, transactionId, paymentDate) VALUES(?,?,?,?,?,?,?)";
        String bookingSql = "UPDATE Bookings SET paymentStatus = 'Paid' WHERE bookingId = ?";

        try (Connection conn = DBUtil.getConnection()) {
            // Use transaction to ensure both operations succeed or fail together
            conn.setAutoCommit(false);

            try (PreparedStatement psPayment = conn.prepareStatement(paymentSql);
                 PreparedStatement psBooking = conn.prepareStatement(bookingSql)) {

                // Insert into Payments
                psPayment.setInt(1, booking.getBookingId());
                psPayment.setDouble(2, totalAmount);
                psPayment.setDouble(3, platformFee);
                psPayment.setDouble(4, payoutAmount);
                psPayment.setString(5, paymentMethod);
                psPayment.setString(6, transactionDetails);
                psPayment.setString(7, LocalDateTime.now().toString());
                psPayment.executeUpdate();

                // Update Bookings
                psBooking.setInt(1, booking.getBookingId());
                psBooking.executeUpdate();

                conn.commit(); // Finalize the transaction

                showAlert("Success", "Payment successful!");
                goBack(event);

            } catch (SQLException e) {
                conn.rollback(); // Revert changes if something goes wrong
                showAlert("Error", "Payment failed. Please try again. " + e.getMessage());
                e.printStackTrace();
            }

        } catch (SQLException e) {
            showAlert("Database Error", "Could not connect to the database.");
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/booking_history.fxml")));
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 600, 400));
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
