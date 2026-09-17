package com.carparking;

import com.carparking.service.ParkingManager;
import com.carparking.ui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Set native system look & feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                ParkingManager manager = new ParkingManager();
                MainFrame frame = new MainFrame(manager);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error starting application: " + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
