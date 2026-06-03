package com.myapp.controllers;

import com.myapp.models.Booking;
import com.myapp.utils.DBUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.controlsfx.control.Rating;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;

public class RatingController {

    @FXML private Label serviceTitleLabel;
    @FXML private Rating starRating;
    @FXML private TextArea commentArea;

    private Booking booking;

    public void setBooking(Booking booking) {
        this.booking = booking;
        serviceTitleLabel.setText("Service: " + booking.getTitle());
    }

    @FXML
    private void handleSubmitReview(ActionEvent event) throws IOException {
        int rating = (int) starRating.getRating();
        if (rating == 0) {
            showAlert(Alert.AlertType.WARNING, "Invalid Rating", "Please select a rating from 1 to 5 stars.");
            return;
        }
        String comment = commentArea.getText().trim();
        Connection conn = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            int providerId = getProviderIdForBooking(conn);
            if (providerId == -1) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not find the service provider.");
                return;
            }

            // 1. Insert the new review
            String insertReviewSql = "INSERT INTO Reviews (bookingId, providerId, customerId, rating, comment, reviewDate) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psReview = conn.prepareStatement(insertReviewSql)) {
                psReview.setInt(1, booking.getBookingId());
                psReview.setInt(2, providerId);
                psReview.setInt(3, booking.getCustomerId());
                psReview.setInt(4, rating);
                psReview.setString(5, comment);
                psReview.setString(6, LocalDateTime.now().toString());
                psReview.executeUpdate();
            }

            // 2. Mark the booking as reviewed
            String updateBookingSql = "UPDATE Bookings SET hasCustomerReviewed = 1 WHERE bookingId = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(updateBookingSql)) {
                psUpdate.setInt(1, booking.getBookingId());
                psUpdate.executeUpdate();
            }

            // 3. Update the provider's average rating
            updateProviderAverageRating(providerId, conn);

            conn.commit(); // Commit all changes if successful

            showAlert(Alert.AlertType.INFORMATION, "Success", "Thank you for your feedback!");
            goBackToHistory(event);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on any SQL error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to submit review: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateProviderAverageRating(int providerId, Connection conn) throws SQLException {
        String sql = """
            UPDATE Users 
            SET totalRatings = (SELECT COUNT(*) FROM Reviews WHERE providerId = ?),
                averageRating = (SELECT AVG(CAST(rating AS REAL)) FROM Reviews WHERE providerId = ?)
            WHERE userId = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            ps.setInt(2, providerId);
            ps.setInt(3, providerId);
            ps.executeUpdate();
        }
    }

    private int getProviderIdForBooking(Connection conn) throws SQLException {
        String sql = "SELECT providerId FROM Services WHERE serviceId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booking.getServiceId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("providerId");
            }
        }
        return -1; // Indicates failure
    }

    @FXML
    private void handleSkip(ActionEvent event) throws IOException {
        goBackToHistory(event);
    }

    private void goBackToHistory(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/booking_history.fxml")));
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 650, 400));
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
