package com.carparking.test;

import com.carparking.service.ParkingManager;
import com.carparking.ui.MainFrame;

import javax.swing.*;

public class GuiSmokeTest {
    public static void main(String[] args) {
        System.out.println("Starting GUI Smoke Test...");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            try {
                ParkingManager pm = new ParkingManager();
                MainFrame frame = new MainFrame(pm);
                frame.pack();
                System.out.println("✓ MainFrame instantiated successfully!");
                System.out.println("✓ Width: " + frame.getWidth() + ", Height: " + frame.getHeight());
                frame.dispose();
                System.out.println("✓ GUI Smoke Test PASSED!");
                System.exit(0);
            } catch (Throwable t) {
                System.err.println("✗ GUI Smoke Test FAILED: " + t.getMessage());
                t.printStackTrace();
                System.exit(1);
            }
        });
    }
}
