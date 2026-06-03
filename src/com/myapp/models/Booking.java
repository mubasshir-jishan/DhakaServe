package com.myapp.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Booking {
    // These fields hold the raw data for a booking
    private final int bookingId;
    private final int serviceId;
    private final int customerId;
    private final boolean isCompletedByProvider;
    private final boolean hasCustomerReviewed;


    // These are special JavaFX properties. The TableView binds directly to these
    // to display data and listen for changes.
    private final StringProperty title; // This is the Service Title
    private final StringProperty customerName; // Holds the Provider's or Customer's Name depending on the context
    private final StringProperty status;
    private final StringProperty paymentStatus;

    public Booking(int bookingId, int serviceId, String serviceTitle,
                   int customerId, String name, String status, String paymentStatus,
                   boolean isCompleted, boolean hasReviewed) {
        this.bookingId = bookingId;
        this.serviceId = serviceId;
        this.customerId = customerId;
        this.isCompletedByProvider = isCompleted;
        this.hasCustomerReviewed = hasReviewed;

        // When a Booking object is created, we initialize the JavaFX properties with the data.
        this.title = new SimpleStringProperty(serviceTitle);
        this.customerName = new SimpleStringProperty(name);
        this.status = new SimpleStringProperty(status);
        this.paymentStatus = new SimpleStringProperty(paymentStatus);
    }

    // --- Standard "getter" methods to access the raw data ---
    public int getBookingId() { return bookingId; }
    public int getServiceId() { return serviceId; }
    public int getCustomerId() { return customerId; }
    public boolean isCompletedByProvider() { return isCompletedByProvider; }
    public boolean hasCustomerReviewed() { return hasCustomerReviewed; }

    // --- JavaFX Property "getter" methods ---
    // The TableView columns use these to get the data. For a property named 'X',
    // the TableView looks for a method named 'XProperty()'.

    public String getTitle() { return title.get(); }
    public StringProperty titleProperty() { return title; }

    public String getCustomerName() { return customerName.get(); }
    public StringProperty customerNameProperty() { return customerName; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public String getPaymentStatus() { return paymentStatus.get(); }
    public StringProperty paymentStatusProperty() { return paymentStatus; }
}
