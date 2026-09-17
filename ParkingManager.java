package com.carparking.service;

import com.carparking.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ParkingManager {

    public interface ParkingChangeListener {
        void onParkingStateChanged();
    }

    private final StorageService storageService;
    private TariffConfig config;
    private final List<ParkingSlot> slots = new ArrayList<>();
    private final List<ParkingTicket> tickets = new ArrayList<>();
    private final List<ParkingChangeListener> listeners = new CopyOnWriteArrayList<>();

    public ParkingManager() {
        this(new StorageService());
    }

    public ParkingManager(StorageService storageService) {
        this.storageService = storageService;
        this.config = storageService.loadConfig();
        this.slots.addAll(storageService.loadSlots(this.config.getTotalSlots()));
        this.tickets.addAll(storageService.loadTickets());
        syncSlotsWithActiveTickets();
    }

    private void syncSlotsWithActiveTickets() {
        // Map slotId -> active ticket
        Map<String, ParkingTicket> activeMap = new HashMap<>();
        for (ParkingTicket t : tickets) {
            if (t.isActive() && t.getSlotId() != null) {
                activeMap.put(t.getSlotId(), t);
            }
        }

        for (ParkingSlot s : slots) {
            if (activeMap.containsKey(s.getSlotId())) {
                ParkingTicket t = activeMap.get(s.getSlotId());
                s.setOccupied(true);
                s.setCurrentVehicle(t.getVehicle());
                s.setCurrentTicketId(t.getTicketId());
                s.setOccupiedSince(t.getEntryTime());
            } else {
                s.setOccupied(false);
                s.setCurrentVehicle(null);
                s.setCurrentTicketId(null);
                s.setOccupiedSince(null);
            }
        }
    }

    public synchronized void addChangeListener(ParkingChangeListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeChangeListener(ParkingChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (ParkingChangeListener l : listeners) {
            try {
                l.onParkingStateChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ==========================================
    // VEHICLE ENTRY
    // ==========================================

    public synchronized ParkingTicket parkVehicle(Vehicle vehicle, String preferredSlotId, LocalDateTime entryTime) throws IllegalStateException {
        if (vehicle == null || vehicle.getPlateNumber() == null || vehicle.getPlateNumber().isBlank()) {
            throw new IllegalArgumentException("Valid vehicle plate number is required.");
        }

        String plate = vehicle.getPlateNumber().trim().toUpperCase();
        vehicle.setPlateNumber(plate);

        // Check if vehicle is already parked
        if (findActiveTicketByPlate(plate) != null) {
            throw new IllegalStateException("Vehicle " + plate + " is already parked inside the facility!");
        }

        // Find slot
        ParkingSlot targetSlot = null;
        if (preferredSlotId != null && !preferredSlotId.isBlank()) {
            targetSlot = getSlot(preferredSlotId.trim());
            if (targetSlot == null || targetSlot.isOccupied()) {
                targetSlot = null;
            }
        }

        if (targetSlot == null) {
            targetSlot = findAvailableSlotForType(vehicle.getType());
        }

        if (targetSlot == null) {
            throw new IllegalStateException("Parking full! No available slot found for " + vehicle.getType().getDisplayName() + ".");
        }

        if (entryTime == null) {
            entryTime = LocalDateTime.now();
        }

        String ticketId = generateTicketId();
        ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, targetSlot.getSlotId(), entryTime);

        targetSlot.park(vehicle, ticketId, entryTime);
        tickets.add(0, ticket);

        saveState();
        notifyListeners();
        return ticket;
    }

    // ==========================================
    // VEHICLE EXIT & BILLING
    // ==========================================

    public synchronized BillingCalculator.BillingBreakdown getExitBillingBreakdown(String identifier, LocalDateTime exitTime) {
        ParkingTicket ticket = findActiveTicket(identifier);
        if (ticket == null) {
            throw new IllegalArgumentException("No active parking record found for '" + identifier + "'.");
        }
        return BillingCalculator.calculate(ticket, exitTime != null ? exitTime : LocalDateTime.now(), config);
    }

    public synchronized ParkingTicket checkoutVehicle(String identifier, LocalDateTime exitTime, String paymentMethod) throws IllegalArgumentException {
        ParkingTicket ticket = findActiveTicket(identifier);
        if (ticket == null) {
            throw new IllegalArgumentException("No active parking record found for '" + identifier + "'.");
        }

        if (exitTime == null) {
            exitTime = LocalDateTime.now();
        }

        BillingCalculator.applyBillingToTicket(ticket, exitTime, config, paymentMethod);

        // Vacate slot
        ParkingSlot slot = getSlot(ticket.getSlotId());
        if (slot != null) {
            slot.vacate();
        }

        saveState();
        notifyListeners();
        return ticket;
    }

    // ==========================================
    // LOOKUP METHODS
    // ==========================================

    public synchronized ParkingTicket findActiveTicket(String query) {
        if (query == null || query.isBlank()) return null;
        String q = query.trim().toUpperCase();

        for (ParkingTicket t : tickets) {
            if (t.isActive()) {
                if (t.getTicketId().equalsIgnoreCase(q)) return t;
                if (t.getVehicle() != null && t.getVehicle().getPlateNumber().equalsIgnoreCase(q)) return t;
                if (t.getSlotId() != null && t.getSlotId().equalsIgnoreCase(q)) return t;
            }
        }
        return null;
    }

    public synchronized ParkingTicket findActiveTicketByPlate(String plateNumber) {
        if (plateNumber == null) return null;
        String p = plateNumber.trim().toUpperCase();
        return tickets.stream()
                .filter(t -> t.isActive() && t.getVehicle() != null && t.getVehicle().getPlateNumber().equalsIgnoreCase(p))
                .findFirst()
                .orElse(null);
    }

    public synchronized ParkingSlot getSlot(String slotId) {
        for (ParkingSlot s : slots) {
            if (s.getSlotId().equalsIgnoreCase(slotId)) {
                return s;
            }
        }
        return null;
    }

    public synchronized ParkingSlot findAvailableSlotForType(VehicleType type) {
        // First try matching type slot
        for (ParkingSlot s : slots) {
            if (!s.isOccupied() && s.getSupportedType() == type) {
                return s;
            }
        }
        // If not found, any vacant slot
        for (ParkingSlot s : slots) {
            if (!s.isOccupied()) {
                return s;
            }
        }
        return null;
    }

    // ==========================================
    // METRICS & STATS
    // ==========================================

    public synchronized int getTotalSlots() {
        return slots.size();
    }

    public synchronized int getOccupiedSlotsCount() {
        return (int) slots.stream().filter(ParkingSlot::isOccupied).count();
    }

    public synchronized int getAvailableSlotsCount() {
        return getTotalSlots() - getOccupiedSlotsCount();
    }

    public synchronized double getOccupancyPercentage() {
        if (slots.isEmpty()) return 0.0;
        return ((double) getOccupiedSlotsCount() / slots.size()) * 100.0;
    }

    public synchronized double getTodayRevenue() {
        LocalDate today = LocalDate.now();
        return tickets.stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()) && t.getPaymentTime() != null && t.getPaymentTime().toLocalDate().equals(today))
                .mapToDouble(ParkingTicket::getTotalAmount)
                .sum();
    }

    public synchronized double getTotalRevenue() {
        return tickets.stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                .mapToDouble(ParkingTicket::getTotalAmount)
                .sum();
    }

    public synchronized int getTodayEntriesCount() {
        LocalDate today = LocalDate.now();
        return (int) tickets.stream()
                .filter(t -> t.getEntryTime() != null && t.getEntryTime().toLocalDate().equals(today))
                .count();
    }

    public synchronized List<ParkingSlot> getAllSlots() {
        return Collections.unmodifiableList(new ArrayList<>(slots));
    }

    public synchronized List<ParkingTicket> getAllTickets() {
        return Collections.unmodifiableList(new ArrayList<>(tickets));
    }

    public synchronized List<ParkingTicket> getActiveTickets() {
        return tickets.stream().filter(ParkingTicket::isActive).collect(Collectors.toList());
    }

    public synchronized TariffConfig getConfig() {
        return config;
    }

    public synchronized void updateConfig(TariffConfig newConfig) {
        this.config = newConfig;
        storageService.saveConfig(this.config);

        // Adjust slot count if changed
        if (newConfig.getTotalSlots() != slots.size()) {
            if (newConfig.getTotalSlots() > slots.size()) {
                int current = slots.size();
                for (int i = current + 1; i <= newConfig.getTotalSlots(); i++) {
                    char row = (char) ('A' + ((i - 1) / 10));
                    int idx = ((i - 1) % 10) + 1;
                    String id = String.format("%c-%02d", row, idx);
                    VehicleType type = (i % 8 == 0) ? VehicleType.TRUCK : (i % 5 == 0) ? VehicleType.BIKE : (i % 3 == 0) ? VehicleType.SUV : VehicleType.CAR;
                    slots.add(new ParkingSlot(id, i, type));
                }
            } else {
                // Shrink only vacant slots from end
                for (int i = slots.size() - 1; i >= newConfig.getTotalSlots(); i--) {
                    if (!slots.get(i).isOccupied()) {
                        slots.remove(i);
                    }
                }
            }
            storageService.saveSlots(slots);
        }
        notifyListeners();
    }

    public synchronized void exportHistoryToCsv(File file) throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("Ticket ID,Plate Number,Vehicle Type,Slot ID,Entry Time,Exit Time,Duration (Mins),Billable Hours,Rate Applied,Total Amount,Payment Method,Status\n");
            for (ParkingTicket t : tickets) {
                Vehicle v = t.getVehicle();
                fw.write(String.format(
                        "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%.2f,%.2f,\"%s\",\"%s\"\n",
                        t.getTicketId(),
                        v != null ? v.getPlateNumber() : "",
                        v != null && v.getType() != null ? v.getType().getDisplayName() : "",
                        t.getSlotId() != null ? t.getSlotId() : "",
                        t.getFormattedEntryTime(),
                        t.getFormattedExitTime(),
                        t.getTotalDurationMinutes(),
                        t.getBillableHours(),
                        t.getHourlyRateApplied(),
                        t.getTotalAmount(),
                        t.getPaymentMethod(),
                        t.getStatus()
                ));
            }
        }
    }

    public synchronized void saveState() {
        storageService.saveSlots(slots);
        storageService.saveTickets(tickets);
        storageService.saveConfig(config);
    }

    private String generateTicketId() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = (int) (Math.random() * 9000) + 1000;
        return "TKT-" + datePart + "-" + rand;
    }
}
