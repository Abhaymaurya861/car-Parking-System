package com.carparking.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class ParkingSlot implements Serializable, Comparable<ParkingSlot> {
    private static final long serialVersionUID = 1L;

    private String slotId;
    private int slotNumber;
    private VehicleType supportedType; // null or specific type
    private boolean occupied;
    private Vehicle currentVehicle;
    private LocalDateTime occupiedSince;
    private String currentTicketId;

    public ParkingSlot() {
    }

    public ParkingSlot(String slotId, int slotNumber, VehicleType supportedType) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.supportedType = supportedType;
        this.occupied = false;
    }

    public void park(Vehicle vehicle, String ticketId, LocalDateTime time) {
        this.currentVehicle = vehicle;
        this.currentTicketId = ticketId;
        this.occupiedSince = time != null ? time : LocalDateTime.now();
        this.occupied = true;
    }

    public void vacate() {
        this.currentVehicle = null;
        this.currentTicketId = null;
        this.occupiedSince = null;
        this.occupied = false;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public VehicleType getSupportedType() {
        return supportedType;
    }

    public void setSupportedType(VehicleType supportedType) {
        this.supportedType = supportedType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    public void setCurrentVehicle(Vehicle currentVehicle) {
        this.currentVehicle = currentVehicle;
    }

    public LocalDateTime getOccupiedSince() {
        return occupiedSince;
    }

    public void setOccupiedSince(LocalDateTime occupiedSince) {
        this.occupiedSince = occupiedSince;
    }

    public String getCurrentTicketId() {
        return currentTicketId;
    }

    public void setCurrentTicketId(String currentTicketId) {
        this.currentTicketId = currentTicketId;
    }

    @Override
    public int compareTo(ParkingSlot other) {
        return Integer.compare(this.slotNumber, other.slotNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingSlot)) return false;
        ParkingSlot that = (ParkingSlot) o;
        return Objects.equals(slotId, that.slotId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slotId);
    }

    @Override
    public String toString() {
        return slotId + (occupied ? " [OCCUPIED: " + currentVehicle.getPlateNumber() + "]" : " [AVAILABLE]");
    }
}
