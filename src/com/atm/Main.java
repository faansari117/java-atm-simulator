package com.atm;

import com.atm.service.BankingService;
import com.atm.service.BankingServiceImpl;
import com.atm.ui.ATMFrame;

import javax.swing.*;

/**
 * Application entry point for the Automated Teller Machine (ATM) simulation.
 * Initializes core banking services and launches the Swing GUI on the Event Dispatch Thread (EDT).
 */
public class Main {
    public static void main(String[] args) {
        // Enable high-DPI scaling and font sub-pixel antialiasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Apply modern Look and Feel if available
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // Fallback to default Java Swing look and feel
            }

            // Initialize decoupled backend business logic
            BankingService bankingService = new BankingServiceImpl();

            // Launch front-end GUI presentation layer
            ATMFrame frame = new ATMFrame(bankingService);
            frame.setVisible(true);
        });
    }
}
