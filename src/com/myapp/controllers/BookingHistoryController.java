package com.myapp.controllers;

import com.myapp.models.Booking;
import com.myapp.utils.DBUtil;
import com.myapp.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class BookingHistoryController {
    @FXML private TableView<Booking> historyTableView;
    @FXML private TableColumn<Booking, String> serviceColumn;
    @FXML private TableColumn<Booking, String> providerColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, String> paymentColumn;
    @FXML private Button payNowButton;
    @FXML private Button reviewButton;

    private final ObservableList<Booking> historyList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        historyTableView.setItems(historyList);
        setupTableColumns();
        historyTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                payNowButton.setDisable(!("Accepted".equals(newVal.getStatus()) && "Unpaid".equals(newVal.getPaymentStatus())));
                reviewButton.setDisable(!(newVal.isCompletedByProvider() && !newVal.hasCustomerReviewed()));
            } else {
                payNowButton.setDisable(true);
                reviewButton.setDisable(true);
            }
        });
        loadHistory();
    }

    private void setupTableColumns() {
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        providerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                if (!empty && item != null) {
                    switch (item) {
                        case "Accepted" -> setTextFill(Color.GREEN);
                        case "Rejected" -> setTextFill(Color.RED);
                        case "Pending" -> setTextFill(Color.ORANGE);
                        case "Completed" -> setTextFill(Color.PURPLE);
                        default -> setTextFill(Color.BLACK);
                    }
                }
            }
        });
        paymentColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTextFill("Paid".equals(item) ? Color.ROYALBLUE : Color.GRAY);
            }
        });
    }

    private void loadHistory() {
        historyList.clear();
        String sql = "SELECT b.bookingId, b.serviceId, s.title AS serviceTitle, s.providerId, u.name AS providerName, b.status, b.paymentStatus, b.isCompletedByProvider, b.hasCustomerReviewed FROM Bookings b JOIN Services s ON b.serviceId = s.serviceId JOIN Users u ON s.providerId = u.userId WHERE b.customerId = ? ORDER BY b.bookingDate DESC";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                historyList.add(new Booking(rs.getInt("bookingId"), rs.getInt("serviceId"), rs.getString("serviceTitle"), Session.getUserId(), rs.getString("providerName"), rs.getBoolean("isCompletedByProvider") ? "Completed" : rs.getString("status"), rs.getString("paymentStatus"), rs.getBoolean("isCompletedByProvider"), rs.getBoolean("hasCustomerReviewed")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePayNow(ActionEvent event) throws IOException {
        Booking selectedBooking = historyTableView.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) return;
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/views/payment_screen.fxml")));
        Parent root = loader.load();
        PaymentController paymentController = loader.getController();
        paymentController.setBooking(selectedBooking);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 400, 450));
        stage.setTitle("Complete Payment");
        stage.centerOnScreen();
    }

    @FXML
    private void handleLeaveReview(ActionEvent event) throws IOException {
        Booking selectedBooking = historyTableView.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) return;
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/views/rating_screen.fxml")));
        Parent root = loader.load();
        RatingController ratingController = loader.getController();
        ratingController.setBooking(selectedBooking);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 450, 400));
        stage.setTitle("Leave a Review");
        stage.centerOnScreen();
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        Parent dash = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/customer_dashboard.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(dash, 950, 700));
        stage.centerOnScreen();
    }
}
