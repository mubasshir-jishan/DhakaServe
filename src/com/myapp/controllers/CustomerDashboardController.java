package com.myapp.controllers;

import com.myapp.models.Service;
import com.myapp.utils.DBUtil;
import com.myapp.utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class CustomerDashboardController {

    @FXML private FlowPane serviceGrid;
    @FXML private TextField searchField;
    @FXML private VBox categoriesBox;
    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + Session.getName() + "!");
        setupCategoryButtons();
        loadServices("SELECT s.*, u.averageRating FROM Services s JOIN Users u ON s.providerId = u.userId ORDER BY u.averageRating DESC");
    }

    private void setupCategoryButtons() {
        Button allBtn = new Button("All Services");
        allBtn.setOnAction(e -> filterByCategory("All"));
        allBtn.getStyleClass().add("category-button");
        allBtn.setMaxWidth(Double.MAX_VALUE);
        categoriesBox.getChildren().add(allBtn);
        String[] cats = {"Household", "Vehicle", "Electrical", "Plumbing", "Cleaning"};
        for (String cat : cats) {
            Button btn = new Button(cat);
            btn.setOnAction(e -> filterByCategory(cat));
            btn.getStyleClass().add("category-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            categoriesBox.getChildren().add(btn);
        }
    }

    private void filterByCategory(String category) {
        String sql = category.equals("All")
                ? "SELECT s.*, u.averageRating FROM Services s JOIN Users u ON s.providerId = u.userId ORDER BY u.averageRating DESC"
                : "SELECT s.*, u.averageRating FROM Services s JOIN Users u ON s.providerId = u.userId WHERE s.category = ? ORDER BY u.averageRating DESC";
        loadServices(sql, category.equals("All") ? new String[0] : new String[]{category});
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String term = searchField.getText().trim().toLowerCase();
        String sql = "SELECT s.*, u.averageRating FROM Services s JOIN Users u ON s.providerId = u.userId WHERE lower(s.title) LIKE ? OR lower(s.description) LIKE ? OR lower(s.category) LIKE ? ORDER BY u.averageRating DESC";
        String searchTerm = "%" + term + "%";
        loadServices(sql, searchTerm, searchTerm, searchTerm);
    }

    private void loadServices(String sql, String... params) {
        serviceGrid.getChildren().clear();
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Service service = new Service(rs.getInt("serviceId"), rs.getInt("providerId"), rs.getString("category"), rs.getString("title"), rs.getString("description"), rs.getDouble("price"), rs.getString("thumbnailPath"), rs.getDouble("averageRating"));
                serviceGrid.getChildren().add(createServiceCard(service));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Node createServiceCard(Service service) {
        VBox card = new VBox(10);
        card.getStyleClass().add("service-card");
        card.setPrefWidth(200);
        card.setPrefHeight(250);
        card.setAlignment(Pos.TOP_LEFT);
        ImageView thumbnailView = new ImageView();
        thumbnailView.setFitHeight(120);
        thumbnailView.setFitWidth(180);
        thumbnailView.setPreserveRatio(false);
        thumbnailView.getStyleClass().add("service-image");
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
        Label titleLabel = new Label(service.getTitle());
        titleLabel.getStyleClass().add("service-title");
        Label priceLabel = new Label(String.format("৳ %.2f", service.getPrice()));
        priceLabel.getStyleClass().add("service-price");
        Label ratingLabel = new Label(String.format("%.1f ★", service.getProviderRating()));
        ratingLabel.getStyleClass().add("service-rating");
        VBox detailsBox = new VBox(5, titleLabel, priceLabel, ratingLabel);
        card.getChildren().addAll(thumbnailView, detailsBox);
        card.setOnMouseClicked(event -> openServiceDetail(service, event));
        return card;
    }

    private void openServiceDetail(Service svc, MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/views/service_detail.fxml")));
            Parent detailRoot = loader.load();
            ServiceDetailController ctrl = loader.getController();
            ctrl.setService(svc);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // --- FIX IS HERE ---
            stage.setScene(new Scene(detailRoot, 600, 550)); // Set a better, consistent size
            // --- END OF FIX ---
            stage.setTitle("Service Details");
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void goToBookingHistory(ActionEvent event) throws IOException {
        loadScene(event, "/views/booking_history.fxml", "My Bookings", 950, 700);
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
