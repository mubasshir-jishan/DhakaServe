package com.myapp.controllers;

import com.myapp.models.Earnings;
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
import javafx.scene.control.Label; // <-- Make sure this import is present
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class EarningsController {

    @FXML private TableView<Earnings> earningsTableView;
    @FXML private TableColumn<Earnings, String> serviceColumn;
    @FXML private TableColumn<Earnings, Number> totalColumn;
    @FXML private TableColumn<Earnings, Number> feeColumn;
    @FXML private TableColumn<Earnings, Number> payoutColumn;
    @FXML private TableColumn<Earnings, String> statusColumn;

    // 1. Add the FXML variable for the new label
    @FXML private Label totalPayoutLabel;

    private final ObservableList<Earnings> earningsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        serviceColumn.setCellValueFactory(cellData -> cellData.getValue().serviceNameProperty());
        totalColumn.setCellValueFactory(cellData -> cellData.getValue().totalPaidProperty());
        feeColumn.setCellValueFactory(cellData -> cellData.getValue().platformFeeProperty());
        payoutColumn.setCellValueFactory(cellData -> cellData.getValue().payoutProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        earningsTableView.setItems(earningsList);
        loadEarnings();
    }

    // 2. Modify the loadEarnings method
    private void loadEarnings() {
        earningsList.clear();
        String sql = """
            SELECT s.title, p.amountPaid, p.platformFee, p.payoutAmount
            FROM Payments p
            JOIN Bookings b ON p.bookingId = b.bookingId
            JOIN Services s ON b.serviceId = s.serviceId
            WHERE s.providerId = ?
            """;

        double totalPayout = 0.0; // Variable to store the sum of payouts

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.getUserId()); //
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                double payout = rs.getDouble("payoutAmount");
                totalPayout += payout; // Add the current row's payout to the total

                earningsList.add(new Earnings(
                        rs.getString("title"),
                        rs.getDouble("amountPaid"),
                        rs.getDouble("platformFee"),
                        payout,
                        "Completed"
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Set the text of the label with the final calculated total
        totalPayoutLabel.setText(String.format("Total Earnings: ৳%.2f", totalPayout));
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        Parent dash = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/provider_dashboard.fxml")));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(dash, 950, 700)); // Correct size
        stage.centerOnScreen();
    }
}