package com.myapp.controllers;

import com.myapp.utils.DBUtil;
import com.myapp.utils.GeminiApiClient;
import com.myapp.utils.Session;
import javafx.application.Platform;
import javafx.concurrent.Task;
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

public class AddGigController {

    @FXML private ChoiceBox<String> categoryChoice;
    @FXML private TextField titleField;
    @FXML private TextArea descArea;
    @FXML private TextField priceField;
    @FXML private ImageView thumbnailPreview;

    private File selectedThumbnailFile;

    @FXML
    public void initialize() {
        categoryChoice.getSelectionModel().selectFirst();
    }

    /**
     * NEW: Handles the "Generate Description" button click.
     * Calls the Gemini API to generate a description based on the title.
     */
    @FXML
    private void handleGenerateDescription(ActionEvent event) {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert("Error", "Please enter a Gig Title first.");
            return;
        }

        // Disable the button to prevent multiple clicks
        Button generateButton = (Button) event.getSource();
        generateButton.setDisable(true);
        descArea.setText("✨ Generating description with AI...");

        // Create a background task to call the API without freezing the UI
        Task<String> generateTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                // This is executed on a background thread
                return GeminiApiClient.generateDescription(title);
            }
        };

        // What to do when the task succeeds
        generateTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                descArea.setText(generateTask.getValue());
                generateButton.setDisable(false);
            });
        });

        // What to do when the task fails
        generateTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert("Error", "Failed to generate description. Please check your connection and try again.");
                descArea.setText("");
                generateButton.setDisable(false);
            });
            generateTask.getException().printStackTrace();
        });

        // Start the background task
        new Thread(generateTask).start();
    }


    @FXML
    private void handleSelectThumbnail(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Thumbnail Image");
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

        String thumbnailPath = null;
        if (selectedThumbnailFile != null) {
            try {
                thumbnailPath = saveThumbnail(selectedThumbnailFile);
            } catch (IOException e) {
                showAlert("Error", "Could not save thumbnail image. Please try again.");
                e.printStackTrace();
                return;
            }
        }

        String sql = "INSERT INTO Services(providerId, category, title, description, price, thumbnailPath) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Session.getUserId());
            ps.setString(2, category);
            ps.setString(3, title);
            ps.setString(4, desc);
            ps.setDouble(5, price);
            ps.setString(6, thumbnailPath);
            ps.executeUpdate();
            showAlert("Success", "Gig saved successfully!");
            goBack(event);
        } catch (SQLException e) {
            showAlert("Error Saving Gig", e.getMessage());
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
