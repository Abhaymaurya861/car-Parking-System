package com.carparking.model;

import java.io.Serializable;

public class TariffConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private String currencySymbol = "$";
    private double hourlyRate = 20.0;           // Fixed charge per hour
    private int gracePeriodMinutes = 10;        // Minutes within which exit is free
    private int minimumChargeHours = 1;         // Minimum billed hours
    private boolean applyVehicleMultiplier = true;
    private int totalSlots = 24;                // Total slots in parking lot

    public TariffConfig() {
    }

    public TariffConfig(String currencySymbol, double hourlyRate, int gracePeriodMinutes, int minimumChargeHours) {
        this.currencySymbol = currencySymbol;
        this.hourlyRate = hourlyRate;
        this.gracePeriodMinutes = gracePeriodMinutes;
        this.minimumChargeHours = minimumChargeHours;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = (currencySymbol != null && !currencySymbol.isBlank()) ? currencySymbol : "$";
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = Math.max(0.0, hourlyRate);
    }

    public int getGracePeriodMinutes() {
        return gracePeriodMinutes;
    }

    public void setGracePeriodMinutes(int gracePeriodMinutes) {
        this.gracePeriodMinutes = Math.max(0, gracePeriodMinutes);
    }

    public int getMinimumChargeHours() {
        return minimumChargeHours;
    }

    public void setMinimumChargeHours(int minimumChargeHours) {
        this.minimumChargeHours = Math.max(1, minimumChargeHours);
    }

    public boolean isApplyVehicleMultiplier() {
        return applyVehicleMultiplier;
    }

    public void setApplyVehicleMultiplier(boolean applyVehicleMultiplier) {
        this.applyVehicleMultiplier = applyVehicleMultiplier;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = Math.max(6, Math.min(200, totalSlots));
    }
}
