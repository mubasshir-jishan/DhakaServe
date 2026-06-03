package com.myapp.models;

public class Service {
    private final int serviceId;
    private final int providerId;
    private final String category;
    private final String title;
    private final String description;
    private final double price;
    private final String thumbnailPath;
    private final double providerRating; // New field

    public Service(int serviceId, int providerId, String category,
                   String title, String description, double price, String thumbnailPath, double providerRating) {
        this.serviceId   = serviceId;
        this.providerId  = providerId;
        this.category    = category;
        this.title       = title;
        this.description = description;
        this.price       = price;
        this.thumbnailPath = thumbnailPath;
        this.providerRating = providerRating; // New field
    }

    // Getters
    public int getServiceId()   { return serviceId; }
    public int getProviderId()  { return providerId; }
    public String getCategory() { return category; }
    public String getTitle()    { return title; }
    public String getDescription() { return description; }
    public double getPrice()    { return price; }
    public String getThumbnailPath() { return thumbnailPath; }
    public double getProviderRating() { return providerRating; } // New getter

    @Override
    public String toString() {
        return title + "  –  ৳" + String.format("%.2f", price);
    }
}
