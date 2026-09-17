package com.carparking.ui;

import com.carparking.service.ParkingManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame implements ParkingManager.ParkingChangeListener, DashboardPanel.NavigationListener {

    private final ParkingManager manager;

    // Panels
    private DashboardPanel dashboardPanel;
    private EntryPanel entryPanel;
    private ExitPanel exitPanel;
    private HistoryPanel historyPanel;
    private SettingsPanel settingsPanel;

    private CardLayout cardLayout;
    private JPanel contentContainer;

    // Header labels
    private JLabel clockLabel;
    private JLabel quickStatusBadge;

    // Nav Buttons
    private JButton navDashBtn;
    private JButton navEntryBtn;
    private JButton navExitBtn;
    private JButton navHistBtn;
    private JButton navSettingsBtn;
    private JButton activeNavBtn;

    public MainFrame(ParkingManager manager) {
        this.manager = manager;
        this.manager.addChangeListener(this);

        setTitle("SmartPark Pro - Car Parking & Hourly Billing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Theme.BG_MAIN);
        setContentPane(rootPanel);

        // 1. Top Header Bar
        rootPanel.add(createHeaderBar(), BorderLayout.NORTH);

        // 2. Navigation Sidebar
        rootPanel.add(createSidebar(), BorderLayout.WEST);

        // 3. Central Content Area with CardLayout
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(Theme.BG_MAIN);

        dashboardPanel = new DashboardPanel(manager, this);
        entryPanel = new EntryPanel(manager, this);
        exitPanel = new ExitPanel(manager, this);
        historyPanel = new HistoryPanel(manager, this);
        settingsPanel = new SettingsPanel(manager);

        contentContainer.add(dashboardPanel, "DASHBOARD");
        contentContainer.add(entryPanel, "ENTRY");
        contentContainer.add(exitPanel, "EXIT");
        contentContainer.add(historyPanel, "HISTORY");
        contentContainer.add(settingsPanel, "SETTINGS");

        rootPanel.add(contentContainer, BorderLayout.CENTER);

        // Start live clock
        Timer clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
        updateClock();
        updateQuickBadge();
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_HEADER);
        header.setBorder(new EmptyBorder(12, 24, 12, 24));

        // Left Brand
        JPanel brandBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandBlock.setOpaque(false);

        JLabel logoLbl = new JLabel("🅿");
        logoLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        logoLbl.setForeground(Color.WHITE);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);

        JLabel appTitle = new JLabel("SmartPark Pro");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        appTitle.setForeground(Color.WHITE);

        JLabel appSub = new JLabel("Automated Parking & Hourly Tariff Billing System");
        appSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        appSub.setForeground(Theme.TEXT_MUTED);

        titleBlock.add(appTitle);
        titleBlock.add(appSub);

        brandBlock.add(logoLbl);
        brandBlock.add(titleBlock);
        header.add(brandBlock, BorderLayout.WEST);

        // Right Info: Status Badge + Live Clock
        JPanel rightBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightBlock.setOpaque(false);

        quickStatusBadge = Theme.createBadge("Available: 0 / 0", Theme.SUCCESS_DARK, Color.WHITE);
        quickStatusBadge.setFont(Theme.FONT_BODY_BOLD);

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Consolas", Font.BOLD, 13));
        clockLabel.setForeground(new Color(203, 213, 225));

        rightBlock.add(quickStatusBadge);
        rightBlock.add(clockLabel);
        header.add(rightBlock, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, new Color(51, 65, 85)));

        JPanel navList = new JPanel(new GridLayout(5, 1, 0, 6));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(16, 12, 16, 12));

        navDashBtn = createNavButton("📊  Dashboard", "DASHBOARD");
        navEntryBtn = createNavButton("🚗  Vehicle Entry", "ENTRY");
        navExitBtn = createNavButton("💳  Exit & Billing", "EXIT");
        navHistBtn = createNavButton("📋  Parking Records", "HISTORY");
        navSettingsBtn = createNavButton("⚙️  Tariff Settings", "SETTINGS");

        navList.add(navDashBtn);
        navList.add(navEntryBtn);
        navList.add(navExitBtn);
        navList.add(navHistBtn);
        navList.add(navSettingsBtn);

        sidebar.add(navList, BorderLayout.NORTH);

        // Bottom Info
        JPanel bottomInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        bottomInfo.setOpaque(false);
        bottomInfo.setBorder(new EmptyBorder(12, 16, 16, 16));

        JLabel vLabel = new JLabel("SmartPark Pro v1.0");
        vLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        vLabel.setForeground(Theme.TEXT_MUTED);

        JLabel statusLbl = new JLabel("● System Online");
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusLbl.setForeground(Theme.SUCCESS);

        bottomInfo.add(vLabel);
        bottomInfo.add(statusLbl);
        sidebar.add(bottomInfo, BorderLayout.SOUTH);

        // Set initial active button
        setActiveNav(navDashBtn);

        return sidebar;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(226, 232, 240));
        btn.setBackground(Theme.BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));

        btn.addActionListener(e -> {
            cardLayout.show(contentContainer, cardName);
            setActiveNav(btn);
            refreshCurrentTab(cardName);
        });

        return btn;
    }

    private void setActiveNav(JButton btn) {
        if (activeNavBtn != null) {
            activeNavBtn.setBackground(Theme.BG_SIDEBAR);
            activeNavBtn.setForeground(new Color(226, 232, 240));
        }
        activeNavBtn = btn;
        if (activeNavBtn != null) {
            activeNavBtn.setBackground(Theme.PRIMARY);
            activeNavBtn.setForeground(Color.WHITE);
        }
    }

    private void refreshCurrentTab(String cardName) {
        switch (cardName) {
            case "DASHBOARD" -> dashboardPanel.refreshData();
            case "ENTRY" -> {
                entryPanel.refreshAvailableSlots();
                entryPanel.refreshRecentEntries();
            }
            case "EXIT" -> exitPanel.refreshActiveVehicles();
            case "HISTORY" -> historyPanel.refreshHistory();
        }
    }

    private void updateClock() {
        clockLabel.setText("🕒 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss")));
    }

    private void updateQuickBadge() {
        int avail = manager.getAvailableSlotsCount();
        int total = manager.getTotalSlots();
        quickStatusBadge.setText(String.format("Available: %d / %d Slots", avail, total));
    }

    // ==========================================
    // PARKING STATE CHANGE LISTENER
    // ==========================================

    @Override
    public void onParkingStateChanged() {
        SwingUtilities.invokeLater(() -> {
            updateQuickBadge();
            dashboardPanel.refreshData();
            entryPanel.refreshAvailableSlots();
            entryPanel.refreshRecentEntries();
            exitPanel.refreshActiveVehicles();
            historyPanel.refreshHistory();
        });
    }

    // ==========================================
    // NAVIGATION LISTENER
    // ==========================================

    @Override
    public void navigateToEntry(String preferredSlotId) {
        cardLayout.show(contentContainer, "ENTRY");
        setActiveNav(navEntryBtn);
        entryPanel.setPreferredSlot(preferredSlotId);
    }

    @Override
    public void navigateToExit(String plateOrSlotId) {
        cardLayout.show(contentContainer, "EXIT");
        setActiveNav(navExitBtn);
        exitPanel.setVehicleForExit(plateOrSlotId);
    }
}
