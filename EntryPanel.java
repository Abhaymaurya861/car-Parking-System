package com.carparking.ui;

import com.carparking.model.ParkingSlot;
import com.carparking.model.ParkingTicket;
import com.carparking.model.Vehicle;
import com.carparking.model.VehicleType;
import com.carparking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EntryPanel extends JPanel {

    private final ParkingManager manager;
    private final Frame parentFrame;

    // Form inputs
    private JTextField plateField;
    private JComboBox<VehicleType> typeCombo;
    private JTextField colorField;
    private JTextField ownerNameField;
    private JTextField ownerPhoneField;
    private JComboBox<String> slotCombo;
    private JLabel currentClockLabel;

    // Recent entries table
    private DefaultTableModel recentModel;
    private JTable recentTable;

    public EntryPanel(ParkingManager manager, Frame parentFrame) {
        this.manager = manager;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshAvailableSlots();
        refreshRecentEntries();
    }

    private void initUI() {
        // Top Title
        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("Vehicle Entry Gate");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Register incoming vehicle, allocate parking slot, and issue parking ticket");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        titleBlock.add(title);
        titleBlock.add(subtitle);
        add(titleBlock, BorderLayout.NORTH);

        // Center split: Form on Left, Recent entries on Right
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setOpaque(false);

        // Left Form Card
        JPanel formCard = Theme.createCard();
        formCard.setLayout(new BorderLayout(0, 14));

        JLabel formHeader = new JLabel("Check-in Registration");
        formHeader.setFont(Theme.FONT_SECTION);
        formHeader.setForeground(Theme.TEXT_PRIMARY);
        formCard.add(formHeader, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Plate Number
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel l1 = new JLabel("License Plate *");
        l1.setFont(Theme.FONT_LABEL);
        fieldsPanel.add(l1, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        plateField = Theme.createTextField(15);
        plateField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        fieldsPanel.add(plateField, gbc);

        // Vehicle Type
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel l2 = new JLabel("Vehicle Type *");
        l2.setFont(Theme.FONT_LABEL);
        fieldsPanel.add(l2, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        typeCombo = new JComboBox<>(VehicleType.values());
        typeCombo.setFont(Theme.FONT_BODY);
        typeCombo.setBackground(Color.WHITE);
        typeCombo.addActionListener(e -> refreshAvailableSlots());
        fieldsPanel.add(typeCombo, gbc);

        // Slot Allocation
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel l3 = new JLabel("Assign Slot");
        l3.setFont(Theme.FONT_LABEL);
        fieldsPanel.add(l3, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        slotCombo = new JComboBox<>();
        slotCombo.setFont(Theme.FONT_BODY);
        slotCombo.setBackground(Color.WHITE);
        fieldsPanel.add(slotCombo, gbc);

        // Vehicle Color
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel l4 = new JLabel("Vehicle Color");
        l4.setFont(Theme.FONT_LABEL);
        fieldsPanel.add(l4, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        colorField = Theme.createTextField(15);
        fieldsPanel.add(colorField, gbc);

        // Owner Name
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        JLabel l5 = new JLabel("Driver/Owner Name");
        l5.setFont(Theme.FONT_LABEL);
        fieldsPanel.add(l5, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        ownerNameField = Theme.createTextField(15);
        fieldsPanel.add(ownerNameField, gbc);

        // Owner Phone
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        JLabel l6 = new JLabel("Driver Phone");
        l6.setFont(Theme.FONT_LABEL);
        fieldsPanel.add(l6, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        ownerPhoneField = Theme.createTextField(15);
        fieldsPanel.add(ownerPhoneField, gbc);

        // Current Entry Time preview
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        JLabel l7 = new JLabel("Entry Timestamp");
        l7.setFont(Theme.FONT_LABEL);
        fieldsPanel.add(l7, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        currentClockLabel = new JLabel();
        currentClockLabel.setFont(Theme.FONT_MONO);
        currentClockLabel.setForeground(Theme.PRIMARY);
        updateClock();
        fieldsPanel.add(currentClockLabel, gbc);

        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Submit & Reset buttons
        JPanel formActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        formActions.setOpaque(false);

        JButton clearBtn = Theme.createButton("Clear", new Color(148, 163, 184), Color.WHITE);
        clearBtn.addActionListener(e -> clearForm());

        JButton submitBtn = Theme.createSuccessButton("✓ Park & Issue Ticket");
        submitBtn.addActionListener(e -> handleCheckin());

        formActions.add(clearBtn);
        formActions.add(submitBtn);
        formCard.add(formActions, BorderLayout.SOUTH);

        centerGrid.add(formCard);

        // Right Recent Entries Card
        JPanel recentCard = Theme.createCard();
        recentCard.setLayout(new BorderLayout(0, 12));

        JLabel recentHeader = new JLabel("Recently Parked Vehicles");
        recentHeader.setFont(Theme.FONT_SECTION);
        recentHeader.setForeground(Theme.TEXT_PRIMARY);
        recentCard.add(recentHeader, BorderLayout.NORTH);

        recentModel = new DefaultTableModel(new String[]{"Ticket ID", "Plate No.", "Type", "Slot", "Entry Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTable = new JTable(recentModel);
        recentTable.setFont(Theme.FONT_BODY);
        recentTable.setRowHeight(30);
        recentTable.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
        recentTable.getTableHeader().setBackground(new Color(241, 245, 249));
        recentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScroll = new JScrollPane(recentTable);
        tableScroll.setBorder(new LineBorder(Theme.BORDER, 1));
        recentCard.add(tableScroll, BorderLayout.CENTER);

        centerGrid.add(recentCard);
        add(centerGrid, BorderLayout.CENTER);

        // Timer to update entry clock
        Timer timer = new Timer(1000, e -> updateClock());
        timer.start();
    }

    private void updateClock() {
        if (currentClockLabel != null) {
            currentClockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " (Auto)");
        }
    }

    public void setPreferredSlot(String slotId) {
        refreshAvailableSlots();
        if (slotId != null) {
            for (int i = 0; i < slotCombo.getItemCount(); i++) {
                if (slotCombo.getItemAt(i).contains(slotId)) {
                    slotCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        plateField.requestFocusInWindow();
    }

    public void refreshAvailableSlots() {
        slotCombo.removeAllItems();
        slotCombo.addItem("⚡ Auto-Assign Nearest Vacant Slot");

        VehicleType selectedType = (VehicleType) typeCombo.getSelectedItem();
        List<ParkingSlot> slots = manager.getAllSlots();
        for (ParkingSlot s : slots) {
            if (!s.isOccupied()) {
                String match = (s.getSupportedType() == selectedType) ? " (Recommended)" : "";
                slotCombo.addItem(s.getSlotId() + " - " + (s.getSupportedType() != null ? s.getSupportedType().getDisplayName() : "Any") + match);
            }
        }
    }

    private void handleCheckin() {
        String plate = plateField.getText().trim();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the vehicle license plate number.", "Missing Plate", JOptionPane.WARNING_MESSAGE);
            plateField.requestFocusInWindow();
            return;
        }

        VehicleType type = (VehicleType) typeCombo.getSelectedItem();
        String color = colorField.getText().trim();
        String ownerName = ownerNameField.getText().trim();
        String phone = ownerPhoneField.getText().trim();

        String selectedSlotText = (String) slotCombo.getSelectedItem();
        String preferredSlot = null;
        if (selectedSlotText != null && !selectedSlotText.startsWith("⚡")) {
            preferredSlot = selectedSlotText.split(" ")[0].trim();
        }

        Vehicle vehicle = new Vehicle(plate, type, color, ownerName, phone);

        try {
            ParkingTicket ticket = manager.parkVehicle(vehicle, preferredSlot, LocalDateTime.now());
            JOptionPane.showMessageDialog(this,
                    "Vehicle parked successfully!\nTicket ID: " + ticket.getTicketId() + "\nAssigned Slot: " + ticket.getSlotId(),
                    "Check-in Success", JOptionPane.INFORMATION_MESSAGE);

            // Open digital receipt / entry ticket dialog
            ReceiptDialog dlg = new ReceiptDialog(parentFrame, ticket, manager.getConfig().getCurrencySymbol());
            dlg.setVisible(true);

            clearForm();
            refreshAvailableSlots();
            refreshRecentEntries();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Parking Error: " + ex.getMessage(), "Check-in Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        plateField.setText("");
        colorField.setText("");
        ownerNameField.setText("");
        ownerPhoneField.setText("");
        typeCombo.setSelectedIndex(0);
        if (slotCombo.getItemCount() > 0) {
            slotCombo.setSelectedIndex(0);
        }
        plateField.requestFocusInWindow();
    }

    public void refreshRecentEntries() {
        recentModel.setRowCount(0);
        List<ParkingTicket> tickets = manager.getAllTickets();
        int count = 0;
        for (ParkingTicket t : tickets) {
            Vehicle v = t.getVehicle();
            recentModel.addRow(new Object[]{
                    t.getTicketId(),
                    v != null ? v.getPlateNumber() : "-",
                    v != null && v.getType() != null ? v.getType().getDisplayName() : "-",
                    t.getSlotId(),
                    t.getFormattedEntryTime()
            });
            count++;
            if (count >= 15) break;
        }
    }
}
