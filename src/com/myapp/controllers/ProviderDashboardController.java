package com.myapp.controllers;

import com.myapp.models.Booking;
import com.myapp.models.Service;
import com.myapp.utils.DBUtil;
import com.myapp.utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class ProviderDashboardController {

    @FXML private BorderPane rootPane;
    @FXML private Label welcomeLabel;
    @FXML private TableView<Service> gigsTableView;
    @FXML private TableColumn<Service, String> gigTitleColumn;
    @FXML private TableColumn<Service, String> gigCategoryColumn;
    @FXML private TableColumn<Service, Double> gigPriceColumn;
    @FXML private TableColumn<Service, Double> gigRatingColumn;
    @FXML private TableView<Booking> bookingsTableView;
    @FXML private TableColumn<Booking, String> serviceColumn;
    @FXML private TableColumn<Booking, String> customerColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, String> paymentColumn;
    @FXML private TableColumn<Booking, Void> actionsColumn;

    private final ObservableList<Service> gigList = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookingList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + Session.getName() + "!");
        setupGigsTable();
        setupBookingsTable();
        loadGigs();
        loadBookings();
    }

    private void setupGigsTable() {
        gigsTableView.setItems(gigList);
        gigTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        gigCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        gigPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        gigRatingColumn.setCellValueFactory(new PropertyValueFactory<>("providerRating"));
        gigPriceColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("%.2f", price));
            }
        });
        gigRatingColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double rating, boolean empty) {
                super.updateItem(rating, empty);
                setText(empty || rating == null ? null : String.format("%.1f ★", rating));
            }
        });
    }

    private void setupBookingsTable() {
        bookingsTableView.setItems(bookingList);
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        statusColumn.setCellFactory(this::createStyledCell);
        paymentColumn.setCellFactory(this::createStyledCell);
        addActionsToTable();
    }

    private void loadGigs() {
        gigList.clear();
        String sql = "SELECT s.*, u.averageRating FROM Services s JOIN Users u ON s.providerId = u.userId WHERE s.providerId=?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                gigList.add(new Service(rs.getInt("serviceId"), rs.getInt("providerId"), rs.getString("category"), rs.getString("title"), rs.getString("description"), rs.getDouble("price"), rs.getString("thumbnailPath"), rs.getDouble("averageRating")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load your gigs.");
        }
    }

    private void loadBookings() {
        bookingList.clear();
        String sql = "SELECT b.bookingId, b.serviceId, s.title AS serviceTitle, b.customerId, u.name AS customerName, b.status, b.paymentStatus, b.isCompletedByProvider, b.hasCustomerReviewed FROM Bookings b JOIN Services s ON b.serviceId = s.serviceId JOIN Users u ON b.customerId = u.userId WHERE s.providerId = ? ORDER BY b.bookingDate DESC";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bookingList.add(new Booking(rs.getInt("bookingId"), rs.getInt("serviceId"), rs.getString("serviceTitle"), rs.getInt("customerId"), rs.getString("customerName"), rs.getBoolean("isCompletedByProvider") ? "Completed" : rs.getString("status"), rs.getString("paymentStatus"), rs.getBoolean("isCompletedByProvider"), rs.getBoolean("hasCustomerReviewed")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load bookings.");
        }
    }

    private void addActionsToTable() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button acceptBtn = new Button("Accept");
            private final Button rejectBtn = new Button("Reject");
            private final Button completeBtn = new Button("Mark Completed");
            private final HBox pane = new HBox(5, acceptBtn, rejectBtn, completeBtn);
            {
                pane.setAlignment(Pos.CENTER);
                acceptBtn.getStyleClass().addAll("table-action-button", "action-button-add");
                rejectBtn.getStyleClass().addAll("table-action-button", "action-button-delete");
                completeBtn.getStyleClass().addAll("table-action-button", "header-button");
                acceptBtn.setOnAction(event -> changeBookingStatus(getTableView().getItems().get(getIndex()), "Accepted"));
                rejectBtn.setOnAction(event -> changeBookingStatus(getTableView().getItems().get(getIndex()), "Rejected"));
                completeBtn.setOnAction(event -> handleMarkAsCompleted(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Booking booking = getTableView().getItems().get(getIndex());
                    boolean isPending = "Pending".equals(booking.getStatus());
                    boolean canComplete = "Accepted".equals(booking.getStatus()) && "Paid".equals(booking.getPaymentStatus()) && !booking.isCompletedByProvider();
                    acceptBtn.setVisible(isPending);
                    acceptBtn.setManaged(isPending);
                    rejectBtn.setVisible(isPending);
                    rejectBtn.setManaged(isPending);
                    completeBtn.setVisible(canComplete);
                    completeBtn.setManaged(canComplete);
                    setGraphic(isPending || canComplete ? pane : null);
                }
            }
        });
    }

    private void handleMarkAsCompleted(Booking booking) {
        String sql = "UPDATE Bookings SET isCompletedByProvider = 1, status = 'Completed' WHERE bookingId = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booking.getBookingId());
            if (ps.executeUpdate() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Service marked as completed.");
                loadBookings();
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update booking status.");
        }
    }

    private void changeBookingStatus(Booking booking, String newStatus) {
        if (!"Pending".equals(booking.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Warning", "This action can only be performed on 'Pending' bookings.");
            return;
        }
        String sql = "UPDATE Bookings SET status = ? WHERE bookingId = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, booking.getBookingId());
            ps.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Booking has been " + newStatus.toLowerCase() + ".");
            loadBookings();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Could not update booking status.");
        }
    }

    @FXML
    private void goToEarnings(ActionEvent event) throws IOException {
        loadScene(event, "/views/earnings_view.fxml", "My Earnings", 950, 700);
    }

    @FXML
    private void goToAddGig(ActionEvent event) throws IOException {
        loadScene(event, "/views/add_gig.fxml", "Add New Gig", 550, 600);
    }

    @FXML
    private void goToEditGig(ActionEvent event) throws IOException {
        Service selected = gigsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a gig from the list to edit.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/views/edit_gig.fxml")));
        Parent root = loader.load();
        EditGigController ctrl = loader.getController();
        ctrl.setService(selected);
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setScene(new Scene(root, 550, 600));
        stage.setTitle("Edit Gig");
        stage.centerOnScreen();
    }

    @FXML
    private void handleDeleteGig(ActionEvent event) {
        Service selected = gigsTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a gig to delete.");
            return;
        }
        showAlert(Alert.AlertType.CONFIRMATION, "Confirm Deletion", "Are you sure you want to delete the gig: '" + selected.getTitle() + "'? This action cannot be undone.").filter(response -> response == ButtonType.OK).ifPresent(response -> {
            String sql = "DELETE FROM Services WHERE serviceId = ?";
            try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, selected.getServiceId());
                ps.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Gig deleted successfully.");
                loadGigs();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete the gig. It may have active bookings.");
            }
        });
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        loadScene(event, "/views/login.fxml", "Service Booking - Login", 450, 400);
    }

    private void loadScene(ActionEvent event, String fxmlPath, String title, int width, int height) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, width, height));
        stage.setTitle(title);
        stage.centerOnScreen();
    }

    private Optional<ButtonType> showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    private TableCell<Booking, String> createStyledCell(TableColumn<Booking, String> column) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().clear();
                } else {
                    setText(item);
                    getStyleClass().removeAll("status-accepted", "status-completed", "status-rejected", "status-pending", "status-paid");
                    switch (item) {
                        case "Accepted" -> getStyleClass().add("status-accepted");
                        case "Paid" -> getStyleClass().add("status-paid");
                        case "Completed" -> getStyleClass().add("status-completed");
                        case "Rejected" -> getStyleClass().add("status-rejected");
                        case "Pending" -> getStyleClass().add("status-pending");
                    }
                }
            }
        };
    }
}
