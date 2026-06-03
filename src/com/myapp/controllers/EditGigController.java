package com.myapp.controllers;

import com.myapp.models.Service;
import com.myapp.utils.DBUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class EditGigController {
    @FXML private ChoiceBox<String> categoryChoice;
    @FXML private TextField titleField;
    @FXML private TextArea descArea;
    @FXML private TextField priceField;
    @FXML private ImageView thumbnailPreview;

    private Service service;
    private File selectedThumbnailFile;

    @FXML
    public void initialize() {
        // Items are added in FXML now
    }

    public void setService(Service svc) {
        this.service = svc;
        categoryChoice.getSelectionModel().select(svc.getCategory());
        titleField.setText(svc.getTitle());
        descArea.setText(svc.getDescription());
        priceField.setText(String.valueOf(svc.getPrice()));

        if (svc.getThumbnailPath() != null && !svc.getThumbnailPath().isEmpty()) {
            try {
                File imageFile = new File(svc.getThumbnailPath());
                if(imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    thumbnailPreview.setImage(image);
                }
            } catch (Exception e) {
                System.err.println("Error loading thumbnail: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSelectThumbnail(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select New Thumbnail Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            this.selectedThumbnailFile = selectedFile;
            try {
                Image image = new Image(selectedFile.toURI().toString());
                thumbnailPreview.setImage(image);
            } catch (Exception e) {
                showAlert("Error", "Failed to load preview image.");
            }
        }
    }

    @FXML
    private void handleSave(ActionEvent event) throws IOException {
        String category = categoryChoice.getValue();
        String title = titleField.getText().trim();
        String desc = descArea.getText().trim();
        String priceStr = priceField.getText().trim();

        if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty() || category == null) {
            showAlert("Error", "All fields are required.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showAlert("Error", "Price must be a valid number.");
            return;
        }

        String thumbnailPath = service.getThumbnailPath();
        if (selectedThumbnailFile != null) {
            try {
                thumbnailPath = saveThumbnail(selectedThumbnailFile);
            } catch (IOException e) {
                showAlert("Error", "Could not save the new thumbnail image.");
                e.printStackTrace();
                return;
            }
        }

        String sql = "UPDATE Services SET category=?, title=?, description=?, price=?, thumbnailPath=? WHERE serviceId=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ps.setString(2, title);
            ps.setString(3, desc);
            ps.setDouble(4, price);
            ps.setString(5, thumbnailPath);
            ps.setInt(6, service.getServiceId());
            ps.executeUpdate();
            showAlert("Success", "Gig updated successfully!");
            goBack(event);
        } catch (SQLException e) {
            showAlert("Error Updating Gig", e.getMessage());
        }
    }

    private String saveThumbnail(File imageFile) throws IOException {
        File imagesDir = new File("images");
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }

        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getName();
        Path destinationPath = new File(imagesDir, fileName).toPath();

        Files.copy(imageFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

        return destinationPath.toString();
    }

    // In the goBack method
    @FXML
    private void goBack(ActionEvent event) throws IOException {
        Parent dash = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/provider_dashboard.fxml")));
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(dash, 950, 700)); // Correct size
        stage.centerOnScreen();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
