package com.example.harshatrial;

public class ParkingLocation {
    private final int id;
    private final int ownerId;
    private final String name;
    private final String address;
    private final int totalSlots;
    private final int availableSlots;
    private final String status;
    private final String features;
    private final double pricePerHour;

    public ParkingLocation(int id, int ownerId, String name, String address, int totalSlots, int availableSlots, String status) {
        this(id, ownerId, name, address, totalSlots, availableSlots, status, "", 5.0);
    }

    public ParkingLocation(int id, int ownerId, String name, String address, int totalSlots, int availableSlots, String status, String features, double pricePerHour) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.totalSlots = totalSlots;
        this.availableSlots = availableSlots;
        this.status = status;
        this.features = features;
        this.pricePerHour = pricePerHour;
    }

    public int getId() { return id; }
    public int getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public int getTotalSlots() { return totalSlots; }
    public int getAvailableSlots() { return availableSlots; }
    public String getStatus() { return status; }
    public String getFeatures() { return features; }
    public double getPricePerHour() { return pricePerHour; }
}
