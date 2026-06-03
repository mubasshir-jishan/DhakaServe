package com.myapp.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Earnings {
    private final StringProperty serviceName;
    private final DoubleProperty totalPaid;
    private final DoubleProperty platformFee;
    private final DoubleProperty payout;
    private final StringProperty status;

    public Earnings(String serviceName, double totalPaid, double platformFee, double payout, String status) {
        this.serviceName = new SimpleStringProperty(serviceName);
        this.totalPaid = new SimpleDoubleProperty(totalPaid);
        this.platformFee = new SimpleDoubleProperty(platformFee);
        this.payout = new SimpleDoubleProperty(payout);
        this.status = new SimpleStringProperty(status);
    }

    // Property getters
    public StringProperty serviceNameProperty() {
        return serviceName;
    }

    public DoubleProperty totalPaidProperty() {
        return totalPaid;
    }

    public DoubleProperty platformFeeProperty() {
        return platformFee;
    }

    public DoubleProperty payoutProperty() {
        return payout;
    }

    public StringProperty statusProperty() {
        return status;
    }
}
