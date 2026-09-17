package com.carparking.ui;

import com.carparking.model.TariffConfig;
import com.carparking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SettingsPanel extends JPanel {

    private final ParkingManager manager;

    private JTextField hourlyRateField;
    private JTextField gracePeriodField;
    private JTextField minHoursField;
    private JTextField currencySymbolField;
    private JSpinner totalSlotsSpinner;
    private JCheckBox vehicleMultiplierCheck;

    public SettingsPanel(ParkingManager manager) {
        this.manager = manager;

        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        loadValues();
    }

    private void initUI() {
        // Top Header
        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("Tariff & Parking Settings");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Configure fixed hourly rates, grace periods, parking capacity, and currency display");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        titleBlock.add(title);
        titleBlock.add(subtitle);
        add(titleBlock, BorderLayout.NORTH);

        // Center Settings Card
        JPanel card = Theme.createCard();
        card.setLayout(new BorderLayout(0, 20));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // Hourly Rate
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        JLabel l1 = new JLabel("Fixed Hourly Rate *");
        l1.setFont(Theme.FONT_LABEL);
        formGrid.add(l1, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        hourlyRateField = Theme.createTextField(10);
        formGrid.add(hourlyRateField, gbc);

        // Grace Period
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        JLabel l2 = new JLabel("Grace Period (Minutes)");
        l2.setFont(Theme.FONT_LABEL);
        formGrid.add(l2, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        gracePeriodField = Theme.createTextField(10);
        formGrid.add(gracePeriodField, gbc);

        // Min Charge Hours
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        JLabel l3 = new JLabel("Minimum Billable Hours");
        l3.setFont(Theme.FONT_LABEL);
        formGrid.add(l3, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        minHoursField = Theme.createTextField(10);
        formGrid.add(minHoursField, gbc);

        // Currency Symbol
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.35;
        JLabel l4 = new JLabel("Currency Symbol");
        l4.setFont(Theme.FONT_LABEL);
        formGrid.add(l4, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        currencySymbolField = Theme.createTextField(6);
        formGrid.add(currencySymbolField, gbc);

        // Total Slots
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.35;
        JLabel l5 = new JLabel("Total Parking Capacity (Slots)");
        l5.setFont(Theme.FONT_LABEL);
        formGrid.add(l5, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        totalSlotsSpinner = new JSpinner(new SpinnerNumberModel(24, 6, 200, 1));
        totalSlotsSpinner.setFont(Theme.FONT_BODY);
        formGrid.add(totalSlotsSpinner, gbc);

        // Vehicle Type Multipliers Checkbox
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.35;
        JLabel l6 = new JLabel("Vehicle Multipliers");
        l6.setFont(Theme.FONT_LABEL);
        formGrid.add(l6, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        vehicleMultiplierCheck = new JCheckBox("Apply type multipliers (Bike 50%, Car 100%, SUV 120%, Truck 150%)");
        vehicleMultiplierCheck.setFont(Theme.FONT_BODY);
        vehicleMultiplierCheck.setOpaque(false);
        formGrid.add(vehicleMultiplierCheck, gbc);

        card.add(formGrid, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);

        JButton resetBtn = Theme.createButton("Reset Defaults", new Color(148, 163, 184), Color.WHITE);
        resetBtn.addActionListener(e -> resetDefaults());

        JButton saveBtn = Theme.createPrimaryButton("💾 Save Configuration");
        saveBtn.addActionListener(e -> saveSettings());

        actionRow.add(resetBtn);
        actionRow.add(saveBtn);
        card.add(actionRow, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);
    }

    private void loadValues() {
        TariffConfig cfg = manager.getConfig();
        hourlyRateField.setText(String.format("%.2f", cfg.getHourlyRate()));
        gracePeriodField.setText(String.valueOf(cfg.getGracePeriodMinutes()));
        minHoursField.setText(String.valueOf(cfg.getMinimumChargeHours()));
        currencySymbolField.setText(cfg.getCurrencySymbol());
        totalSlotsSpinner.setValue(cfg.getTotalSlots());
        vehicleMultiplierCheck.setSelected(cfg.isApplyVehicleMultiplier());
    }

    private void resetDefaults() {
        hourlyRateField.setText("20.00");
        gracePeriodField.setText("10");
        minHoursField.setText("1");
        currencySymbolField.setText("$");
        totalSlotsSpinner.setValue(24);
        vehicleMultiplierCheck.setSelected(true);
    }

    private void saveSettings() {
        try {
            double rate = Double.parseDouble(hourlyRateField.getText().trim());
            int grace = Integer.parseInt(gracePeriodField.getText().trim());
            int minHours = Integer.parseInt(minHoursField.getText().trim());
            String symbol = currencySymbolField.getText().trim();
            int slots = (Integer) totalSlotsSpinner.getValue();
            boolean applyMulti = vehicleMultiplierCheck.isSelected();

            if (rate < 0) throw new IllegalArgumentException("Hourly rate cannot be negative.");
            if (grace < 0) throw new IllegalArgumentException("Grace period cannot be negative.");
            if (minHours < 1) throw new IllegalArgumentException("Minimum hours must be at least 1.");
            if (symbol.isEmpty()) symbol = "$";

            TariffConfig newConfig = new TariffConfig();
            newConfig.setHourlyRate(rate);
            newConfig.setGracePeriodMinutes(grace);
            newConfig.setMinimumChargeHours(minHours);
            newConfig.setCurrencySymbol(symbol);
            newConfig.setTotalSlots(slots);
            newConfig.setApplyVehicleMultiplier(applyMulti);

            manager.updateConfig(newConfig);

            JOptionPane.showMessageDialog(this, "Tariff and settings saved successfully!", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please check that numeric fields are formatted properly.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
