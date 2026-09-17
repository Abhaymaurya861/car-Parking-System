package com.carparking.service;

import com.carparking.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Robust zero-dependency file persistence for parking configuration, slots, and ticket history.
 */
public class StorageService {

    private final Path dataDir;
    private final Path configFile;
    private final Path ticketsFile;
    private final Path slotsFile;

    public StorageService() {
        this("data");
    }

    public StorageService(String directory) {
        this.dataDir = Paths.get(directory);
        this.configFile = dataDir.resolve("config.json");
        this.ticketsFile = dataDir.resolve("tickets.json");
        this.slotsFile = dataDir.resolve("slots.json");
        initDirectory();
    }

    private void initDirectory() {
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (IOException e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }
    }

    // ==========================================
    // TARIFF CONFIG
    // ==========================================

    public TariffConfig loadConfig() {
        TariffConfig config = new TariffConfig();
        if (!Files.exists(configFile)) {
            saveConfig(config);
            return config;
        }

        try {
            String content = Files.readString(configFile, StandardCharsets.UTF_8);
            config.setCurrencySymbol(extractJsonString(content, "currencySymbol", "$"));
            config.setHourlyRate(extractJsonDouble(content, "hourlyRate", 20.0));
            config.setGracePeriodMinutes(extractJsonInt(content, "gracePeriodMinutes", 10));
            config.setMinimumChargeHours(extractJsonInt(content, "minimumChargeHours", 1));
            config.setApplyVehicleMultiplier(extractJsonBool(content, "applyVehicleMultiplier", true));
            config.setTotalSlots(extractJsonInt(content, "totalSlots", 24));
        } catch (Exception e) {
            System.err.println("Warning: failed to parse config.json, using defaults: " + e.getMessage());
        }
        return config;
    }

    public void saveConfig(TariffConfig config) {
        if (config == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"currencySymbol\": \"").append(escapeJson(config.getCurrencySymbol())).append("\",\n");
        sb.append("  \"hourlyRate\": ").append(config.getHourlyRate()).append(",\n");
        sb.append("  \"gracePeriodMinutes\": ").append(config.getGracePeriodMinutes()).append(",\n");
        sb.append("  \"minimumChargeHours\": ").append(config.getMinimumChargeHours()).append(",\n");
        sb.append("  \"applyVehicleMultiplier\": ").append(config.isApplyVehicleMultiplier()).append(",\n");
        sb.append("  \"totalSlots\": ").append(config.getTotalSlots()).append("\n");
        sb.append("}\n");

        writeFile(configFile, sb.toString());
    }

    // ==========================================
    // TICKETS (ACTIVE & HISTORY)
    // ==========================================

    public List<ParkingTicket> loadTickets() {
        List<ParkingTicket> list = new ArrayList<>();
        if (!Files.exists(ticketsFile)) {
            return list;
        }

        try {
            String content = Files.readString(ticketsFile, StandardCharsets.UTF_8).trim();
            if (content.isEmpty() || content.equals("[]")) return list;

            List<String> items = splitJsonArray(content);
            for (String item : items) {
                try {
                    ParkingTicket t = parseTicketJson(item);
                    if (t != null) {
                        list.add(t);
                    }
                } catch (Exception ex) {
                    System.err.println("Error parsing ticket JSON item: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load tickets: " + e.getMessage());
        }
        return list;
    }

    public void saveTickets(List<ParkingTicket> tickets) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < tickets.size(); i++) {
            ParkingTicket t = tickets.get(i);
            sb.append(ticketToJson(t, "  "));
            if (i < tickets.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]\n");

        writeFile(ticketsFile, sb.toString());
    }

    // ==========================================
    // PARKING SLOTS
    // ==========================================

    public List<ParkingSlot> loadSlots(int expectedCount) {
        List<ParkingSlot> slots = new ArrayList<>();
        if (!Files.exists(slotsFile)) {
            return generateDefaultSlots(expectedCount);
        }

        try {
            String content = Files.readString(slotsFile, StandardCharsets.UTF_8).trim();
            if (content.isEmpty() || content.equals("[]")) {
                return generateDefaultSlots(expectedCount);
            }

            List<String> items = splitJsonArray(content);
            for (String item : items) {
                ParkingSlot slot = parseSlotJson(item);
                if (slot != null) {
                    slots.add(slot);
                }
            }

            if (slots.isEmpty()) {
                return generateDefaultSlots(expectedCount);
            }

            // If configured count changed, adjust slots
            while (slots.size() < expectedCount) {
                int num = slots.size() + 1;
                char row = (char) ('A' + ((num - 1) / 10));
                int idx = ((num - 1) % 10) + 1;
                String id = String.format("%c-%02d", row, idx);
                slots.add(new ParkingSlot(id, num, VehicleType.CAR));
            }
        } catch (Exception e) {
            System.err.println("Failed to load slots: " + e.getMessage());
            return generateDefaultSlots(expectedCount);
        }
        return slots;
    }

    public void saveSlots(List<ParkingSlot> slots) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < slots.size(); i++) {
            ParkingSlot s = slots.get(i);
            sb.append(slotToJson(s, "  "));
            if (i < slots.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("]\n");

        writeFile(slotsFile, sb.toString());
    }

    public List<ParkingSlot> generateDefaultSlots(int count) {
        List<ParkingSlot> slots = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            char row = (char) ('A' + ((i - 1) / 10));
            int idx = ((i - 1) % 10) + 1;
            String id = String.format("%c-%02d", row, idx);
            VehicleType type = (i % 8 == 0) ? VehicleType.TRUCK : (i % 5 == 0) ? VehicleType.BIKE : (i % 3 == 0) ? VehicleType.SUV : VehicleType.CAR;
            slots.add(new ParkingSlot(id, i, type));
        }
        return slots;
    }

    // ==========================================
    // JSON HELPERS
    // ==========================================

    private String ticketToJson(ParkingTicket t, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("{\n");
        sb.append(indent).append("  \"ticketId\": \"").append(escapeJson(t.getTicketId())).append("\",\n");
        sb.append(indent).append("  \"slotId\": \"").append(escapeJson(t.getSlotId())).append("\",\n");
        sb.append(indent).append("  \"status\": \"").append(escapeJson(t.getStatus())).append("\",\n");
        sb.append(indent).append("  \"entryTime\": \"").append(t.getEntryTime() != null ? t.getEntryTime().toString() : "").append("\",\n");
        sb.append(indent).append("  \"exitTime\": \"").append(t.getExitTime() != null ? t.getExitTime().toString() : "").append("\",\n");
        sb.append(indent).append("  \"totalDurationMinutes\": ").append(t.getTotalDurationMinutes()).append(",\n");
        sb.append(indent).append("  \"billableHours\": ").append(t.getBillableHours()).append(",\n");
        sb.append(indent).append("  \"hourlyRateApplied\": ").append(t.getHourlyRateApplied()).append(",\n");
        sb.append(indent).append("  \"totalAmount\": ").append(t.getTotalAmount()).append(",\n");
        sb.append(indent).append("  \"paymentMethod\": \"").append(escapeJson(t.getPaymentMethod())).append("\",\n");
        sb.append(indent).append("  \"paymentTime\": \"").append(t.getPaymentTime() != null ? t.getPaymentTime().toString() : "").append("\",\n");

        Vehicle v = t.getVehicle();
        sb.append(indent).append("  \"vehicle\": {\n");
        if (v != null) {
            sb.append(indent).append("    \"plateNumber\": \"").append(escapeJson(v.getPlateNumber())).append("\",\n");
            sb.append(indent).append("    \"type\": \"").append(v.getType() != null ? v.getType().name() : "CAR").append("\",\n");
            sb.append(indent).append("    \"color\": \"").append(escapeJson(v.getColor())).append("\",\n");
            sb.append(indent).append("    \"ownerName\": \"").append(escapeJson(v.getOwnerName())).append("\",\n");
            sb.append(indent).append("    \"ownerPhone\": \"").append(escapeJson(v.getOwnerPhone())).append("\"\n");
        }
        sb.append(indent).append("  }\n");
        sb.append(indent).append("}");
        return sb.toString();
    }

    private ParkingTicket parseTicketJson(String json) {
        String ticketId = extractJsonString(json, "ticketId", "");
        if (ticketId.isEmpty()) return null;

        String slotId = extractJsonString(json, "slotId", "");
        String status = extractJsonString(json, "status", "ACTIVE");
        String entryStr = extractJsonString(json, "entryTime", "");
        String exitStr = extractJsonString(json, "exitTime", "");
        long durationMin = extractJsonLong(json, "totalDurationMinutes", 0);
        long billable = extractJsonLong(json, "billableHours", 0);
        double rate = extractJsonDouble(json, "hourlyRateApplied", 0.0);
        double total = extractJsonDouble(json, "totalAmount", 0.0);
        String payMethod = extractJsonString(json, "paymentMethod", "PENDING");
        String payTimeStr = extractJsonString(json, "paymentTime", "");

        // Vehicle
        String plate = extractJsonString(json, "plateNumber", "");
        String typeStr = extractJsonString(json, "type", "CAR");
        String color = extractJsonString(json, "color", "");
        String ownerName = extractJsonString(json, "ownerName", "");
        String ownerPhone = extractJsonString(json, "ownerPhone", "");

        Vehicle vehicle = new Vehicle(plate, VehicleType.fromString(typeStr), color, ownerName, ownerPhone);
        LocalDateTime entryTime = parseIsoDateTime(entryStr, LocalDateTime.now());

        ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, slotId, entryTime);
        ticket.setStatus(status);
        if (!exitStr.isEmpty()) {
            ticket.setExitTime(parseIsoDateTime(exitStr, null));
        }
        ticket.setTotalDurationMinutes(durationMin);
        ticket.setBillableHours(billable);
        ticket.setHourlyRateApplied(rate);
        ticket.setTotalAmount(total);
        ticket.setPaymentMethod(payMethod);
        if (!payTimeStr.isEmpty()) {
            ticket.setPaymentTime(parseIsoDateTime(payTimeStr, null));
        }

        return ticket;
    }

    private String slotToJson(ParkingSlot s, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("{\n");
        sb.append(indent).append("  \"slotId\": \"").append(escapeJson(s.getSlotId())).append("\",\n");
        sb.append(indent).append("  \"slotNumber\": ").append(s.getSlotNumber()).append(",\n");
        sb.append(indent).append("  \"supportedType\": \"").append(s.getSupportedType() != null ? s.getSupportedType().name() : "").append("\",\n");
        sb.append(indent).append("  \"occupied\": ").append(s.isOccupied()).append(",\n");
        sb.append(indent).append("  \"currentTicketId\": \"").append(escapeJson(s.getCurrentTicketId() != null ? s.getCurrentTicketId() : "")).append("\",\n");
        sb.append(indent).append("  \"occupiedSince\": \"").append(s.getOccupiedSince() != null ? s.getOccupiedSince().toString() : "").append("\",\n");

        Vehicle v = s.getCurrentVehicle();
        sb.append(indent).append("  \"currentVehicle\": ");
        if (v != null) {
            sb.append("{\n");
            sb.append(indent).append("    \"plateNumber\": \"").append(escapeJson(v.getPlateNumber())).append("\",\n");
            sb.append(indent).append("    \"type\": \"").append(v.getType() != null ? v.getType().name() : "CAR").append("\",\n");
            sb.append(indent).append("    \"color\": \"").append(escapeJson(v.getColor())).append("\",\n");
            sb.append(indent).append("    \"ownerName\": \"").append(escapeJson(v.getOwnerName())).append("\",\n");
            sb.append(indent).append("    \"ownerPhone\": \"").append(escapeJson(v.getOwnerPhone())).append("\"\n");
            sb.append(indent).append("  }\n");
        } else {
            sb.append("null\n");
        }
        sb.append(indent).append("}");
        return sb.toString();
    }

    private ParkingSlot parseSlotJson(String json) {
        String slotId = extractJsonString(json, "slotId", "");
        if (slotId.isEmpty()) return null;

        int slotNumber = extractJsonInt(json, "slotNumber", 1);
        String supportedTypeStr = extractJsonString(json, "supportedType", "CAR");
        boolean occupied = extractJsonBool(json, "occupied", false);
        String currentTicketId = extractJsonString(json, "currentTicketId", "");
        String occupiedSinceStr = extractJsonString(json, "occupiedSince", "");

        VehicleType type = VehicleType.fromString(supportedTypeStr);
        ParkingSlot slot = new ParkingSlot(slotId, slotNumber, type);
        slot.setOccupied(occupied);
        slot.setCurrentTicketId(currentTicketId.isEmpty() ? null : currentTicketId);
        if (!occupiedSinceStr.isEmpty()) {
            slot.setOccupiedSince(parseIsoDateTime(occupiedSinceStr, null));
        }

        if (occupied && json.contains("\"currentVehicle\"") && !json.contains("\"currentVehicle\": null")) {
            String plate = extractJsonString(json, "plateNumber", "");
            String vTypeStr = extractJsonString(json, "type", "CAR");
            String color = extractJsonString(json, "color", "");
            String ownerName = extractJsonString(json, "ownerName", "");
            String ownerPhone = extractJsonString(json, "ownerPhone", "");
            Vehicle v = new Vehicle(plate, VehicleType.fromString(vTypeStr), color, ownerName, ownerPhone);
            slot.setCurrentVehicle(v);
        }

        return slot;
    }

    private void writeFile(Path path, String content) {
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error writing to file " + path + ": " + e.getMessage());
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private LocalDateTime parseIsoDateTime(String str, LocalDateTime fallback) {
        if (str == null || str.isBlank()) return fallback;
        try {
            return LocalDateTime.parse(str.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private String extractJsonString(String json, String key, String defaultVal) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return defaultVal;
    }

    private double extractJsonDouble(String json, String key, double defaultVal) {
        String pattern = "\"" + key + "\"\\s*:\\s*([0-9.]+)";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (Exception ignored) {}
        }
        return defaultVal;
    }

    private long extractJsonLong(String json, String key, long defaultVal) {
        String pattern = "\"" + key + "\"\\s*:\\s*([0-9]+)";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1));
            } catch (Exception ignored) {}
        }
        return defaultVal;
    }

    private int extractJsonInt(String json, String key, int defaultVal) {
        return (int) extractJsonLong(json, key, defaultVal);
    }

    private boolean extractJsonBool(String json, String key, boolean defaultVal) {
        String pattern = "\"" + key + "\"\\s*:\\s*(true|false)";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE).matcher(json);
        if (m.find()) {
            return Boolean.parseBoolean(m.group(1));
        }
        return defaultVal;
    }

    private List<String> splitJsonArray(String json) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    result.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return result;
    }
}
