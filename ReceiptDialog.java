package com.carparking.ui;

import com.carparking.model.ParkingTicket;
import com.carparking.model.Vehicle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.time.format.DateTimeFormatter;

public class ReceiptDialog extends JDialog {

    private final ParkingTicket ticket;
    private final String currencySymbol;
    private JTextArea receiptArea;

    public ReceiptDialog(Frame owner, ParkingTicket ticket, String currencySymbol) {
        super(owner, "Parking Receipt - " + ticket.getTicketId(), true);
        this.ticket = ticket;
        this.currencySymbol = currencySymbol != null ? currencySymbol : "$";

        initComponents();
        setSize(440, 580);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 16));
        contentPane.setBackground(Theme.BG_MAIN);
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);

        // Header Title
        JLabel titleLabel = new JLabel("PARKING RECEIPT", SwingConstants.CENTER);
        titleLabel.setFont(Theme.FONT_SECTION);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        contentPane.add(titleLabel, BorderLayout.NORTH);

        // Receipt Receipt Paper Display
        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        receiptArea.setBackground(Color.WHITE);
        receiptArea.setForeground(new Color(30, 41, 59));
        receiptArea.setMargin(new Insets(16, 20, 16, 20));
        receiptArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        receiptArea.setText(generateReceiptText());

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(null);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton printBtn = Theme.createPrimaryButton("Print Receipt");
        printBtn.addActionListener(e -> {
            try {
                boolean done = receiptArea.print();
                if (done) {
                    JOptionPane.showMessageDialog(this, "Receipt sent to printer.", "Printing", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton closeBtn = Theme.createButton("Close", new Color(100, 116, 139), Color.WHITE);
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(printBtn);
        buttonPanel.add(closeBtn);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
    }

    private String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        String line = "------------------------------------------\n";
        String doubleLine = "==========================================\n";

        sb.append("         CAR PARKING MANAGEMENT           \n");
        sb.append("         Official Parking Receipt         \n");
        sb.append(doubleLine);
        sb.append(String.format(" Ticket ID   : %s\n", ticket.getTicketId()));
        sb.append(String.format(" Slot ID     : %s\n", ticket.getSlotId()));
        sb.append(String.format(" Status      : %s\n", ticket.getStatus()));
        sb.append(line);

        Vehicle v = ticket.getVehicle();
        if (v != null) {
            sb.append(String.format(" Plate No.   : %s\n", v.getPlateNumber()));
            sb.append(String.format(" Vehicle Type: %s\n", v.getType() != null ? v.getType().getDisplayName() : "Car"));
            if (v.getOwnerName() != null && !v.getOwnerName().isBlank()) {
                sb.append(String.format(" Owner/Driver: %s\n", v.getOwnerName()));
            }
        }
        sb.append(line);

        sb.append(String.format(" Entry Time  : %s\n", ticket.getFormattedEntryTime()));
        sb.append(String.format(" Exit Time   : %s\n", ticket.getFormattedExitTime()));
        sb.append(String.format(" Duration    : %s\n", ticket.getFormattedDuration()));
        sb.append(line);

        sb.append(" BILLING BREAKDOWN (FIXED HOURLY RATE):\n");
        sb.append(String.format(" Billable Hrs: %d hour(s)\n", ticket.getBillableHours()));
        sb.append(String.format(" Hourly Rate : %s%.2f / hr\n", currencySymbol, ticket.getHourlyRateApplied()));
        sb.append(line);

        sb.append(String.format(" TOTAL DUE   : %s%.2f\n", currencySymbol, ticket.getTotalAmount()));
        sb.append(String.format(" Paid Via    : %s\n", ticket.getPaymentMethod()));
        if (ticket.getPaymentTime() != null) {
            sb.append(String.format(" Paid At     : %s\n", ticket.getPaymentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        sb.append(doubleLine);
        sb.append("        Thank you for parking with us!    \n");
        sb.append("            Have a safe drive!            \n");

        return sb.toString();
    }
}
