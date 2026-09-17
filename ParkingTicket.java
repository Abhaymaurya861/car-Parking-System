package com.carparking.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParkingTicket implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter SHORT_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String ticketId;
    private Vehicle vehicle;
    private String slotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private String status; // "ACTIVE", "COMPLETED", "CANCELLED"
    private long totalDurationMinutes;
    private long billableHours;
    private double hourlyRateApplied;
    private double totalAmount;
    private String paymentMethod; // "CASH", "CARD", "UPI/ONLINE", "WAIVED", "PENDING"
    private LocalDateTime paymentTime;

    public ParkingTicket() {
    }

    public ParkingTicket(String ticketId, Vehicle vehicle, String slotId, LocalDateTime entryTime) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.slotId = slotId;
        this.entryTime = entryTime != null ? entryTime : LocalDateTime.now();
        this.status = "ACTIVE";
        this.paymentMethod = "PENDING";
    }

    public String getFormattedDuration() {
        LocalDateTime end = (exitTime != null) ? exitTime : LocalDateTime.now();
        Duration d = Duration.between(entryTime, end);
        if (d.isNegative()) d = Duration.ZERO;
        long days = d.toDays();
        long hours = d.toHoursPart();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();

        if (days > 0) {
            return String.format("%dd %02dh %02dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh %02dm %02ds", hours, minutes, seconds);
        } else {
            return String.format("%dm %02ds", minutes, seconds);
        }
    }

    public String getFormattedEntryTime() {
        return entryTime != null ? entryTime.format(TIME_FMT) : "-";
    }

    public String getFormattedExitTime() {
        return exitTime != null ? exitTime.format(TIME_FMT) : "Parked (In Progress)";
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public void setTotalDurationMinutes(long totalDurationMinutes) {
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public long getBillableHours() {
        return billableHours;
    }

    public void setBillableHours(long billableHours) {
        this.billableHours = billableHours;
    }

    public double getHourlyRateApplied() {
        return hourlyRateApplied;
    }

    public void setHourlyRateApplied(double hourlyRateApplied) {
        this.hourlyRateApplied = hourlyRateApplied;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }
}
