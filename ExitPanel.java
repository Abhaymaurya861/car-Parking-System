package com.carparking.ui;

import com.carparking.model.ParkingSlot;
import com.carparking.model.ParkingTicket;
import com.carparking.model.Vehicle;
import com.carparking.service.BillingCalculator;
import com.carparking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExitPanel extends JPanel {

    private final ParkingManager manager;
    private final Frame parentFrame;

    // Search
    private JTextField searchField;
    private JComboBox<String> activeVehiclesCombo;

    // Current selection & breakdown
    private ParkingTicket activeTicket;
    private BillingCalculator.BillingBreakdown currentBreakdown;

    // Labels for Bill
    private JLabel plateVal;
    private JLabel typeVal;
    private JLabel slotVal;
    private JLabel entryTimeVal;
    private JLabel exitTimeVal;
    private JLabel durationVal;
    private JLabel rateVal;
    private JLabel billableHoursVal;
    private JLabel totalAmountVal;
    private JLabel gracePeriodVal;

    // Payment
    private JComboBox<String> paymentCombo;
    private JButton checkoutBtn;

    public ExitPanel(ParkingManager manager, Frame parentFrame) {
        this.manager = manager;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshActiveVehicles();
    }

    private void initUI() {
        // Top Header
        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("Vehicle Exit & Fixed Hour Billing");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Calculate parking duration, compute fixed hourly charges, collect payment, and clear slot");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        titleBlock.add(title);
        titleBlock.add(subtitle);
        add(titleBlock, BorderLayout.NORTH);

        // Center split
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setOpaque(false);

        // Left Card: Vehicle Selection & Search
        JPanel searchCard = Theme.createCard();
        searchCard.setLayout(new BorderLayout(0, 14));

        JLabel searchHeader = new JLabel("Select Parked Vehicle");
        searchHeader.setFont(Theme.FONT_SECTION);
        searchHeader.setForeground(Theme.TEXT_PRIMARY);
        searchCard.add(searchHeader, BorderLayout.NORTH);

        JPanel searchForm = new JPanel(new GridBagLayout());
        searchForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Search Input
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel sLabel = new JLabel("Search Plate / Ticket / Slot");
        sLabel.setFont(Theme.FONT_LABEL);
        searchForm.add(sLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JPanel searchBoxPanel = new JPanel(new BorderLayout(6, 0));
        searchBoxPanel.setOpaque(false);
        searchField = Theme.createTextField(12);
        searchField.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchField.addActionListener(e -> performSearch());

        JButton searchBtn = Theme.createPrimaryButton("Search");
        searchBtn.addActionListener(e -> performSearch());
        searchBoxPanel.add(searchField, BorderLayout.CENTER);
        searchBoxPanel.add(searchBtn, BorderLayout.EAST);
        searchForm.add(searchBoxPanel, gbc);

        // Or Select From Active List
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel cLabel = new JLabel("Or Choose Parked Vehicle");
        cLabel.setFont(Theme.FONT_LABEL);
        searchForm.add(cLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        activeVehiclesCombo = new JComboBox<>();
        activeVehiclesCombo.setFont(Theme.FONT_BODY);
        activeVehiclesCombo.setBackground(Color.WHITE);
        activeVehiclesCombo.addActionListener(e -> {
            String selected = (String) activeVehiclesCombo.getSelectedItem();
            if (selected != null && !selected.startsWith("--")) {
                String plate = selected.split(" ")[0].trim();
                loadVehicle(plate);
            }
        });
        searchForm.add(activeVehiclesCombo, gbc);

        searchCard.add(searchForm, BorderLayout.CENTER);

        // Tariff Info Note
        JPanel tariffNotice = new JPanel(new BorderLayout(0, 4));
        tariffNotice.setBackground(Theme.WARNING_LIGHT);
        tariffNotice.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.WARNING, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel noticeTitle = new JLabel("ℹ Fixed Hourly Billing Rule");
        noticeTitle.setFont(Theme.FONT_LABEL);
        noticeTitle.setForeground(new Color(180, 83, 9));

        JLabel noticeText = new JLabel("<html>Charges are calculated on a fixed hourly basis. Any fractional hour past the grace period is billed as a full hour.</html>");
        noticeText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noticeText.setForeground(new Color(146, 64, 14));

        tariffNotice.add(noticeTitle, BorderLayout.NORTH);
        tariffNotice.add(noticeText, BorderLayout.CENTER);
        searchCard.add(tariffNotice, BorderLayout.SOUTH);

        centerGrid.add(searchCard);

        // Right Card: Itemized Bill & Checkout
        JPanel billCard = Theme.createCard();
        billCard.setLayout(new BorderLayout(0, 12));

        JLabel billHeader = new JLabel("Live Billing Breakdown");
        billHeader.setFont(Theme.FONT_SECTION);
        billHeader.setForeground(Theme.TEXT_PRIMARY);
        billCard.add(billHeader, BorderLayout.NORTH);

        // Bill Fields Grid
        JPanel billGrid = new JPanel(new GridLayout(9, 2, 8, 8));
        billGrid.setOpaque(false);

        plateVal = addBillRow(billGrid, "License Plate:");
        typeVal = addBillRow(billGrid, "Vehicle Type:");
        slotVal = addBillRow(billGrid, "Assigned Slot:");
        entryTimeVal = addBillRow(billGrid, "Entry Time:");
        exitTimeVal = addBillRow(billGrid, "Exit Time:");
        durationVal = addBillRow(billGrid, "Elapsed Duration:");
        billableHoursVal = addBillRow(billGrid, "Billable Hours:");
        rateVal = addBillRow(billGrid, "Hourly Rate Applied:");
        gracePeriodVal = addBillRow(billGrid, "Grace Period Status:");

        billCard.add(billGrid, BorderLayout.CENTER);

        // Bottom Total and Checkout
        JPanel checkoutSection = new JPanel(new BorderLayout(0, 10));
        checkoutSection.setOpaque(false);
        checkoutSection.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Total Row
        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setBackground(new Color(241, 245, 249));
        totalBox.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel totalLbl = new JLabel("TOTAL DUE:");
        totalLbl.setFont(Theme.FONT_BODY_BOLD);
        totalLbl.setForeground(Theme.TEXT_PRIMARY);

        totalAmountVal = new JLabel("$0.00");
        totalAmountVal.setFont(Theme.FONT_STAT_NUM);
        totalAmountVal.setForeground(Theme.PRIMARY);

        totalBox.add(totalLbl, BorderLayout.WEST);
        totalBox.add(totalAmountVal, BorderLayout.EAST);
        checkoutSection.add(totalBox, BorderLayout.NORTH);

        // Payment Row
        JPanel payRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        payRow.setOpaque(false);

        JLabel payLbl = new JLabel("Payment Method:");
        payLbl.setFont(Theme.FONT_LABEL);

        paymentCombo = new JComboBox<>(new String[]{"CASH", "CARD", "UPI / ONLINE", "WAIVED"});
        paymentCombo.setFont(Theme.FONT_BODY);
        paymentCombo.setBackground(Color.WHITE);

        checkoutBtn = Theme.createSuccessButton("✓ Process Payment & Exit");
        checkoutBtn.setEnabled(false);
        checkoutBtn.addActionListener(e -> processCheckout());

        payRow.add(payLbl);
        payRow.add(paymentCombo);
        payRow.add(checkoutBtn);
        checkoutSection.add(payRow, BorderLayout.SOUTH);

        billCard.add(checkoutSection, BorderLayout.SOUTH);
        centerGrid.add(billCard);

        add(centerGrid, BorderLayout.CENTER);
    }

    private JLabel addBillRow(JPanel container, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_LABEL);
        label.setForeground(Theme.TEXT_SECONDARY);

        JLabel valLabel = new JLabel("-");
        valLabel.setFont(Theme.FONT_BODY_BOLD);
        valLabel.setForeground(Theme.TEXT_PRIMARY);

        container.add(label);
        container.add(valLabel);
        return valLabel;
    }

    public void setVehicleForExit(String identifier) {
        searchField.setText(identifier);
        performSearch();
    }

    private void performSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) return;
        loadVehicle(q);
    }

    private void loadVehicle(String query) {
        ParkingTicket ticket = manager.findActiveTicket(query);
        if (ticket == null) {
            JOptionPane.showMessageDialog(this, "No active vehicle found for: " + query, "Not Found", JOptionPane.INFORMATION_MESSAGE);
            clearBillView();
            return;
        }

        this.activeTicket = ticket;
        LocalDateTime now = LocalDateTime.now();
        this.currentBreakdown = manager.getExitBillingBreakdown(ticket.getTicketId(), now);

        Vehicle v = ticket.getVehicle();
        String sym = manager.getConfig().getCurrencySymbol();

        plateVal.setText(v != null ? v.getPlateNumber() : "-");
        typeVal.setText(v != null && v.getType() != null ? v.getType().getDisplayName() : "-");
        slotVal.setText(ticket.getSlotId());
        entryTimeVal.setText(ticket.getFormattedEntryTime());
        exitTimeVal.setText(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        durationVal.setText(currentBreakdown.durationDisplay);
        billableHoursVal.setText(currentBreakdown.billableHours + " hour(s)");
        rateVal.setText(Theme.formatCurrency(currentBreakdown.effectiveHourlyRate, sym) + " / hr");
        gracePeriodVal.setText(currentBreakdown.gracePeriodApplies ? "Applied (Free Exit)" : "None");
        totalAmountVal.setText(Theme.formatCurrency(currentBreakdown.totalAmount, sym));

        checkoutBtn.setEnabled(true);
    }

    private void clearBillView() {
        activeTicket = null;
        currentBreakdown = null;
        plateVal.setText("-");
        typeVal.setText("-");
        slotVal.setText("-");
        entryTimeVal.setText("-");
        exitTimeVal.setText("-");
        durationVal.setText("-");
        billableHoursVal.setText("-");
        rateVal.setText("-");
        gracePeriodVal.setText("-");
        totalAmountVal.setText(Theme.formatCurrency(0.0, manager.getConfig().getCurrencySymbol()));
        checkoutBtn.setEnabled(false);
    }

    private void processCheckout() {
        if (activeTicket == null || currentBreakdown == null) return;

        String method = (String) paymentCombo.getSelectedItem();
        String sym = manager.getConfig().getCurrencySymbol();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Confirm checkout for %s?\nTotal Payable: %s%.2f via %s",
                        activeTicket.getVehicle().getPlateNumber(),
                        sym, currentBreakdown.totalAmount, method),
                "Confirm Checkout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            ParkingTicket completedTicket = manager.checkoutVehicle(activeTicket.getTicketId(), LocalDateTime.now(), method);

            JOptionPane.showMessageDialog(this,
                    "Checkout completed! Slot " + completedTicket.getSlotId() + " is now VACANT.",
                    "Checkout Success", JOptionPane.INFORMATION_MESSAGE);

            // Show receipt
            ReceiptDialog dlg = new ReceiptDialog(parentFrame, completedTicket, manager.getConfig().getCurrencySymbol());
            dlg.setVisible(true);

            clearBillView();
            searchField.setText("");
            refreshActiveVehicles();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Checkout error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshActiveVehicles() {
        activeVehiclesCombo.removeAllItems();
        activeVehiclesCombo.addItem("-- Select currently parked vehicle --");

        List<ParkingTicket> active = manager.getActiveTickets();
        for (ParkingTicket t : active) {
            Vehicle v = t.getVehicle();
            String plate = (v != null) ? v.getPlateNumber() : "Unknown";
            String slot = t.getSlotId();
            activeVehiclesCombo.addItem(plate + " (Slot " + slot + ") - " + t.getFormattedDuration());
        }
    }
}
