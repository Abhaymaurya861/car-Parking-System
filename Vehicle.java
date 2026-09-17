package com.carparking.model;

import java.io.Serializable;
import java.util.Objects;

public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    private String plateNumber;
    private VehicleType type;
    private String color;
    private String ownerName;
    private String ownerPhone;

    public Vehicle() {
    }

    public Vehicle(String plateNumber, VehicleType type, String color, String ownerName, String ownerPhone) {
        this.plateNumber = (plateNumber != null) ? plateNumber.trim().toUpperCase() : "";
        this.type = (type != null) ? type : VehicleType.CAR;
        this.color = (color != null && !color.isBlank()) ? color.trim() : "Standard";
        this.ownerName = (ownerName != null && !ownerName.isBlank()) ? ownerName.trim() : "Visitor";
        this.ownerPhone = (ownerPhone != null && !ownerPhone.isBlank()) ? ownerPhone.trim() : "-";
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = (plateNumber != null) ? plateNumber.trim().toUpperCase() : "";
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(plateNumber, vehicle.plateNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plateNumber);
    }

    @Override
    public String toString() {
        return plateNumber + " [" + type.getDisplayName() + "]";
    }
}
