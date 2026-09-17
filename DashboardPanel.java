package com.carparking.ui;

import com.carparking.model.ParkingSlot;
import com.carparking.model.Vehicle;
import com.carparking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardPanel extends JPanel {

    public interface NavigationListener {
        void navigateToEntry(String preferredSlotId);
        void navigateToExit(String plateOrSlotId);
    }

    private final ParkingManager manager;
    private final NavigationListener navListener;

    // Stat card labels
    private JLabel totalSlotsLabel;
    private JLabel availableSlotsLabel;
    private JLabel occupiedSlotsLabel;
    private JLabel occupancyRateLabel;
    private JLabel todayRevenueLabel;

    private JPanel slotGridContainer;

    public DashboardPanel(ParkingManager manager, NavigationListener navListener) {
        this.manager = manager;
        this.navListener = navListener;

        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // Top Section: Header + Metrics
        JPanel topPanel = new JPanel(new BorderLayout(0, 16));
        topPanel.setOpaque(false);

        // Header Title and Quick Actions
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("Parking Lot Dashboard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Real-time visual monitoring, slot occupancy & live billing overview");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        titleBlock.add(title);
        titleBlock.add(subtitle);

        JPanel actionBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBlock.setOpaque(false);

        JButton entryBtn = Theme.createSuccessButton("+ Vehicle Entry");
        entryBtn.addActionListener(e -> {
            if (navListener != null) navListener.navigateToEntry(null);
        });

        JButton exitBtn = Theme.createPrimaryButton("- Exit & Pay");
        exitBtn.addActionListener(e -> {
            if (navListener != null) navListener.navigateToExit(null);
        });

        JButton refreshBtn = Theme.createButton("🔄 Refresh", new Color(148, 163, 184), Color.WHITE);
        refreshBtn.addActionListener(e -> refreshData());

        actionBlock.add(entryBtn);
        actionBlock.add(exitBtn);
        actionBlock.add(refreshBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(actionBlock, BorderLayout.EAST);
        topPanel.add(headerPanel, BorderLayout.NORTH);

        // KPI Metric Cards Row
        JPanel kpiRow = new JPanel(new GridLayout(1, 5, 14, 0));
        kpiRow.setOpaque(false);

        totalSlotsLabel = new JLabel("0");
        availableSlotsLabel = new JLabel("0");
        occupiedSlotsLabel = new JLabel("0");
        occupancyRateLabel = new JLabel("0%");
        todayRevenueLabel = new JLabel("$0.00");

        kpiRow.add(createKpiCard("TOTAL SLOTS", totalSlotsLabel, Theme.PRIMARY, "🚗 Total capacity"));
        kpiRow.add(createKpiCard("AVAILABLE", availableSlotsLabel, Theme.SUCCESS_DARK, "🟢 Ready to park"));
        kpiRow.add(createKpiCard("OCCUPIED", occupiedSlotsLabel, Theme.DANGER_DARK, "🔴 Active parked"));
        kpiRow.add(createKpiCard("OCCUPANCY", occupancyRateLabel, Theme.WARNING, "📊 Capacity used"));
        kpiRow.add(createKpiCard("TODAY'S REVENUE", todayRevenueLabel, Theme.PRIMARY, "💰 Fixed hour bill"));

        topPanel.add(kpiRow, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Center Section: Visual Parking Slot Matrix
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        legendPanel.setOpaque(false);
        JLabel gridTitle = new JLabel("Live Parking Grid");
        gridTitle.setFont(Theme.FONT_SECTION);
        gridTitle.setForeground(Theme.TEXT_PRIMARY);

        JLabel availLegend = Theme.createBadge("■ AVAILABLE (Click to Park)", Theme.SUCCESS_LIGHT, Theme.SUCCESS_DARK);
        JLabel occLegend = Theme.createBadge("■ OCCUPIED (Click to Exit)", Theme.DANGER_LIGHT, Theme.DANGER_DARK);

        legendPanel.add(gridTitle);
        legendPanel.add(availLegend);
        legendPanel.add(occLegend);
        centerPanel.add(legendPanel, BorderLayout.NORTH);

        slotGridContainer = new JPanel();
        slotGridContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(slotGridContainer);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));
        scrollPane.getViewport().setBackground(Theme.BG_MAIN);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor, String footer) {
        JPanel card = Theme.createCard();
        card.setLayout(new BorderLayout(0, 6));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_LABEL);
        titleLbl.setForeground(Theme.TEXT_MUTED);

        valueLabel.setFont(Theme.FONT_STAT_NUM);
        valueLabel.setForeground(accentColor);

        JLabel footerLbl = new JLabel(footer);
        footerLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLbl.setForeground(Theme.TEXT_SECONDARY);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(footerLbl, BorderLayout.SOUTH);
        return card;
    }

    public void refreshData() {
        int total = manager.getTotalSlots();
        int occupied = manager.getOccupiedSlotsCount();
        int available = total - occupied;
        double pct = manager.getOccupancyPercentage();
        double rev = manager.getTodayRevenue();
        String sym = manager.getConfig().getCurrencySymbol();

        totalSlotsLabel.setText(String.valueOf(total));
        availableSlotsLabel.setText(String.valueOf(available));
        occupiedSlotsLabel.setText(String.valueOf(occupied));
        occupancyRateLabel.setText(String.format("%.1f%%", pct));
        todayRevenueLabel.setText(Theme.formatCurrency(rev, sym));

        renderSlotGrid();
    }

    private void renderSlotGrid() {
        slotGridContainer.removeAll();
        List<ParkingSlot> slots = manager.getAllSlots();

        // Responsive grid columns based on slot count
        int cols = 6;
        if (slots.size() <= 12) cols = 4;
        else if (slots.size() > 40) cols = 8;

        slotGridContainer.setLayout(new GridLayout(0, cols, 10, 10));

        LocalDateTime now = LocalDateTime.now();

        for (ParkingSlot slot : slots) {
            JPanel slotCard = new JPanel(new BorderLayout(0, 4));
            slotCard.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(slot.isOccupied() ? Theme.DANGER : Theme.SUCCESS, 2, true),
                    new EmptyBorder(10, 10, 10, 10)
            ));
            slotCard.setBackground(slot.isOccupied() ? Theme.DANGER_LIGHT : Theme.SUCCESS_LIGHT);
            slotCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Top row: Slot ID + Icon
            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);
            JLabel slotIdLbl = new JLabel(slot.getSlotId());
            slotIdLbl.setFont(Theme.FONT_BODY_BOLD);
            slotIdLbl.setForeground(Theme.TEXT_PRIMARY);

            String icon = (slot.getSupportedType() != null) ? slot.getSupportedType().getIcon() : "🚗";
            JLabel iconLbl = new JLabel(icon);
            iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

            topRow.add(slotIdLbl, BorderLayout.WEST);
            topRow.add(iconLbl, BorderLayout.EAST);
            slotCard.add(topRow, BorderLayout.NORTH);

            // Center: Plate number or "VACANT"
            JPanel centerBlock = new JPanel(new GridLayout(2, 1, 0, 2));
            centerBlock.setOpaque(false);

            if (slot.isOccupied() && slot.getCurrentVehicle() != null) {
                Vehicle v = slot.getCurrentVehicle();
                JLabel plateLbl = new JLabel(v.getPlateNumber(), SwingConstants.CENTER);
                plateLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                plateLbl.setForeground(Theme.DANGER_DARK);

                // Elapsed time
                String durationStr = "-";
                if (slot.getOccupiedSince() != null) {
                    Duration d = Duration.between(slot.getOccupiedSince(), now);
                    long h = d.toHours();
                    long m = d.toMinutesPart();
                    durationStr = (h > 0) ? String.format("%dh %02dm", h, m) : String.format("%dm", m);
                }
                JLabel timeLbl = new JLabel("⏱ " + durationStr, SwingConstants.CENTER);
                timeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                timeLbl.setForeground(Theme.TEXT_SECONDARY);

                centerBlock.add(plateLbl);
                centerBlock.add(timeLbl);
            } else {
                JLabel vacantLbl = new JLabel("VACANT", SwingConstants.CENTER);
                vacantLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                vacantLbl.setForeground(Theme.SUCCESS_DARK);

                JLabel typeLbl = new JLabel(slot.getSupportedType() != null ? slot.getSupportedType().getDisplayName() : "Any", SwingConstants.CENTER);
                typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                typeLbl.setForeground(Theme.TEXT_SECONDARY);

                centerBlock.add(vacantLbl);
                centerBlock.add(typeLbl);
            }

            slotCard.add(centerBlock, BorderLayout.CENTER);

            // Bottom action hint
            JLabel hintLbl = new JLabel(slot.isOccupied() ? "Click to Exit ➔" : "Click to Park ➔", SwingConstants.CENTER);
            hintLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            hintLbl.setForeground(slot.isOccupied() ? Theme.DANGER_DARK : Theme.SUCCESS_DARK);
            slotCard.add(hintLbl, BorderLayout.SOUTH);

            // Mouse click handler
            slotCard.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (slot.isOccupied()) {
                        if (navListener != null) {
                            String id = slot.getCurrentVehicle() != null ? slot.getCurrentVehicle().getPlateNumber() : slot.getSlotId();
                            navListener.navigateToExit(id);
                        }
                    } else {
                        if (navListener != null) {
                            navListener.navigateToEntry(slot.getSlotId());
                        }
                    }
                }
            });

            slotGridContainer.add(slotCard);
        }

        slotGridContainer.revalidate();
        slotGridContainer.repaint();
    }
}
