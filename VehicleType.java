package com.carparking.model;

public enum VehicleType {
    CAR("Car", 1.0, "🚗"),
    SUV("SUV / Van", 1.2, "🚙"),
    BIKE("Bike / Motorcycle", 0.5, "🏍️"),
    TRUCK("Truck / Heavy", 1.5, "🚚");

    private final String displayName;
    private final double rateMultiplier;
    private final String icon;

    VehicleType(String displayName, double rateMultiplier, String icon) {
        this.displayName = displayName;
        this.rateMultiplier = rateMultiplier;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getRateMultiplier() {
        return rateMultiplier;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return displayName + " (" + (int)(rateMultiplier * 100) + "% rate)";
    }

    public static VehicleType fromString(String text) {
        if (text == null) return CAR;
        for (VehicleType vt : values()) {
            if (vt.name().equalsIgnoreCase(text) || vt.displayName.equalsIgnoreCase(text)) {
                return vt;
            }
        }
        return CAR;
    }
}
