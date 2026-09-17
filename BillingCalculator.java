package com.carparking.service;

import com.carparking.model.ParkingTicket;
import com.carparking.model.TariffConfig;
import com.carparking.model.VehicleType;

import java.time.Duration;
import java.time.LocalDateTime;

public class BillingCalculator {

    public static class BillingBreakdown {
        public LocalDateTime entryTime;
        public LocalDateTime exitTime;
        public long totalMinutes;
        public String durationDisplay;
        public boolean gracePeriodApplies;
        public long billableHours;
        public double baseHourlyRate;
        public double vehicleMultiplier;
        public double effectiveHourlyRate;
        public double totalAmount;

        @Override
        public String toString() {
            return String.format(
                "Duration: %s (Billable: %d hrs @ %.2f/hr) -> Total: %.2f",
                durationDisplay, billableHours, effectiveHourlyRate, totalAmount
            );
        }
    }

    public static BillingBreakdown calculate(ParkingTicket ticket, LocalDateTime exitTime, TariffConfig config) {
        if (ticket == null || config == null) {
            throw new IllegalArgumentException("Ticket and config must not be null");
        }
        if (exitTime == null) {
            exitTime = LocalDateTime.now();
        }

        LocalDateTime entryTime = ticket.getEntryTime();
        if (exitTime.isBefore(entryTime)) {
            exitTime = entryTime;
        }

        Duration duration = Duration.between(entryTime, exitTime);
        long totalSeconds = Math.max(0, duration.getSeconds());
        long totalMinutes = (totalSeconds + 59) / 60; // round up fractional minute

        BillingBreakdown breakdown = new BillingBreakdown();
        breakdown.entryTime = entryTime;
        breakdown.exitTime = exitTime;
        breakdown.totalMinutes = totalMinutes;

        // Human readable duration
        long dHours = totalMinutes / 60;
        long dMins = totalMinutes % 60;
        breakdown.durationDisplay = (dHours > 0) ? String.format("%dh %02dm", dHours, dMins) : String.format("%dm", dMins);

        // Check grace period (e.g., 10 minutes free exit)
        if (totalMinutes <= config.getGracePeriodMinutes()) {
            breakdown.gracePeriodApplies = true;
            breakdown.billableHours = 0;
            breakdown.baseHourlyRate = config.getHourlyRate();
            breakdown.vehicleMultiplier = 1.0;
            breakdown.effectiveHourlyRate = 0.0;
            breakdown.totalAmount = 0.0;
            return breakdown;
        }

        breakdown.gracePeriodApplies = false;

        // Fixed hour calculation:
        // Ceiling to next full hour: e.g. 15 mins -> 1 hour; 65 mins -> 2 hours
        long billable = (totalMinutes + 59) / 60;
        if (billable < config.getMinimumChargeHours()) {
            billable = config.getMinimumChargeHours();
        }
        breakdown.billableHours = billable;

        // Rate calculation
        double baseRate = config.getHourlyRate();
        VehicleType vType = (ticket.getVehicle() != null && ticket.getVehicle().getType() != null)
                ? ticket.getVehicle().getType()
                : VehicleType.CAR;

        double multiplier = config.isApplyVehicleMultiplier() ? vType.getRateMultiplier() : 1.0;
        double effectiveRate = Math.round(baseRate * multiplier * 100.0) / 100.0;
        double total = Math.round(billable * effectiveRate * 100.0) / 100.0;

        breakdown.baseHourlyRate = baseRate;
        breakdown.vehicleMultiplier = multiplier;
        breakdown.effectiveHourlyRate = effectiveRate;
        breakdown.totalAmount = total;

        return breakdown;
    }

    public static void applyBillingToTicket(ParkingTicket ticket, LocalDateTime exitTime, TariffConfig config, String paymentMethod) {
        BillingBreakdown bb = calculate(ticket, exitTime, config);
        ticket.setExitTime(bb.exitTime);
        ticket.setTotalDurationMinutes(bb.totalMinutes);
        ticket.setBillableHours(bb.billableHours);
        ticket.setHourlyRateApplied(bb.effectiveHourlyRate);
        ticket.setTotalAmount(bb.totalAmount);
        ticket.setPaymentMethod(paymentMethod != null ? paymentMethod : "CASH");
        ticket.setPaymentTime(bb.exitTime);
        ticket.setStatus("COMPLETED");
    }
}
