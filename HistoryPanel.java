package com.carparking.ui;

import com.carparking.model.ParkingTicket;
import com.carparking.model.Vehicle;
import com.carparking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class HistoryPanel extends JPanel {

    private final ParkingManager manager;
    private final Frame parentFrame;

    private DefaultTableModel tableModel;
    private JTable historyTable;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JCheckBox todayOnlyCheck;
    private JLabel countLabel;

    public HistoryPanel(ParkingManager manager, Frame parentFrame) {
        this.manager = manager;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshHistory();
    }

    private void initUI() {
        // Top Header
        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        JLabel title = new JLabel("Parking Records & History");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Audit all active and historical parking tickets, billing totals, and export data");
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        titleBlock.add(title);
        titleBlock.add(subtitle);

        // Filter Bar Card
        JPanel filterCard = Theme.createCard();
        filterCard.setLayout(new BorderLayout(16, 0));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftFilters.setOpaque(false);

        JLabel searchLbl = new JLabel("Filter:");
        searchLbl.setFont(Theme.FONT_LABEL);

        searchField = Theme.createTextField(14);
        searchField.putClientProperty("JTextField.placeholderText", "Search Plate/ID...");
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilter();
            }
        });

        statusFilter = new JComboBox<>(new String[]{"All Statuses", "ACTIVE", "COMPLETED"});
        statusFilter.setFont(Theme.FONT_BODY);
        statusFilter.setBackground(Color.WHITE);
        statusFilter.addActionListener(e -> applyFilter());

        todayOnlyCheck = new JCheckBox("Today Only");
        todayOnlyCheck.setFont(Theme.FONT_BODY);
        todayOnlyCheck.setOpaque(false);
        todayOnlyCheck.addActionListener(e -> refreshHistory());

        countLabel = new JLabel("0 records");
        countLabel.setFont(Theme.FONT_LABEL);
        countLabel.setForeground(Theme.TEXT_SECONDARY);

        leftFilters.add(searchLbl);
        leftFilters.add(searchField);
        leftFilters.add(statusFilter);
        leftFilters.add(todayOnlyCheck);
        leftFilters.add(countLabel);

        // Right Actions
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton viewReceiptBtn = Theme.createPrimaryButton("View / Print Receipt");
        viewReceiptBtn.addActionListener(e -> viewSelectedReceipt());

        JButton exportBtn = Theme.createButton("⬇ Export CSV", new Color(15, 118, 110), Color.WHITE);
        exportBtn.addActionListener(e -> handleExportCsv());

        JButton refreshBtn = Theme.createButton("🔄", new Color(148, 163, 184), Color.WHITE);
        refreshBtn.addActionListener(e -> refreshHistory());

        rightActions.add(viewReceiptBtn);
        rightActions.add(exportBtn);
        rightActions.add(refreshBtn);

        filterCard.add(leftFilters, BorderLayout.WEST);
        filterCard.add(rightActions, BorderLayout.EAST);

        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setOpaque(false);
        topContainer.add(titleBlock, BorderLayout.NORTH);
        topContainer.add(filterCard, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "Ticket ID", "Plate No", "Type", "Slot", "Entry Time",
                "Exit Time", "Duration", "Hrs", "Rate", "Total", "Method", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        historyTable.setRowSorter(sorter);
        historyTable.setFont(Theme.FONT_BODY);
        historyTable.setRowHeight(30);
        historyTable.getTableHeader().setFont(Theme.FONT_BODY_BOLD);
        historyTable.getTableHeader().setBackground(new Color(241, 245, 249));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer for Status column
        historyTable.getColumnModel().getColumn(11).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(Theme.FONT_LABEL);
                String val = String.valueOf(value);
                if ("ACTIVE".equalsIgnoreCase(val)) {
                    lbl.setForeground(Theme.DANGER_DARK);
                    lbl.setBackground(Theme.DANGER_LIGHT);
                } else if ("COMPLETED".equalsIgnoreCase(val)) {
                    lbl.setForeground(Theme.SUCCESS_DARK);
                    lbl.setBackground(Theme.SUCCESS_LIGHT);
                } else {
                    lbl.setForeground(Theme.TEXT_SECONDARY);
                }
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(Theme.BORDER, 1));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshHistory() {
        tableModel.setRowCount(0);
        List<ParkingTicket> tickets = manager.getAllTickets();
        String sym = manager.getConfig().getCurrencySymbol();
        boolean todayOnly = todayOnlyCheck.isSelected();
        LocalDate today = LocalDate.now();

        int displayed = 0;
        for (ParkingTicket t : tickets) {
            if (todayOnly && (t.getEntryTime() == null || !t.getEntryTime().toLocalDate().equals(today))) {
                continue;
            }

            Vehicle v = t.getVehicle();
            tableModel.addRow(new Object[]{
                    t.getTicketId(),
                    v != null ? v.getPlateNumber() : "-",
                    v != null && v.getType() != null ? v.getType().getDisplayName() : "-",
                    t.getSlotId(),
                    t.getFormattedEntryTime(),
                    t.getFormattedExitTime(),
                    t.getFormattedDuration(),
                    t.getBillableHours(),
                    Theme.formatCurrency(t.getHourlyRateApplied(), sym),
                    Theme.formatCurrency(t.getTotalAmount(), sym),
                    t.getPaymentMethod(),
                    t.getStatus()
            });
            displayed++;
        }

        countLabel.setText(displayed + " records");
        applyFilter();
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        String status = (String) statusFilter.getSelectedItem();

        RowFilter<DefaultTableModel, Object> rf = null;
        try {
            if (!text.isEmpty() && !"All Statuses".equals(status)) {
                RowFilter<DefaultTableModel, Object> textFilter = RowFilter.regexFilter("(?i)" + text);
                RowFilter<DefaultTableModel, Object> stFilter = RowFilter.regexFilter("(?i)^" + status + "$", 11);
                rf = RowFilter.andFilter(List.of(textFilter, stFilter));
            } else if (!text.isEmpty()) {
                rf = RowFilter.regexFilter("(?i)" + text);
            } else if (!"All Statuses".equals(status)) {
                rf = RowFilter.regexFilter("(?i)^" + status + "$", 11);
            }
        } catch (java.util.regex.PatternSyntaxException ignored) {}

        sorter.setRowFilter(rf);
    }

    private void viewSelectedReceipt() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a ticket from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = historyTable.convertRowIndexToModel(selectedRow);
        String ticketId = (String) tableModel.getValueAt(modelRow, 0);

        ParkingTicket ticket = null;
        for (ParkingTicket t : manager.getAllTickets()) {
            if (t.getTicketId().equalsIgnoreCase(ticketId)) {
                ticket = t;
                break;
            }
        }

        if (ticket != null) {
            ReceiptDialog dlg = new ReceiptDialog(parentFrame, ticket, manager.getConfig().getCurrencySymbol());
            dlg.setVisible(true);
        }
    }

    private void handleExportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("parking_history_" + LocalDate.now() + ".csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                manager.exportHistoryToCsv(chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Exported successfully to " + chooser.getSelectedFile().getName(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
