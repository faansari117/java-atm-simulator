package com.atm.ui.component;

import com.atm.model.Customer;
import com.atm.ui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Standardized ATM top header displaying bank branding, terminal security telemetry,
 * real-time clock, and active session identity.
 */
public class HeaderPanel extends JPanel {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy");

    private final JLabel timeLabel;
    private final JLabel dateLabel;
    private final JLabel userBadgeLabel;

    public HeaderPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Left Branding
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);

        JLabel brandTitle = new JLabel("METRO APEX BANK");
        brandTitle.setFont(UITheme.FONT_BRAND);
        brandTitle.setForeground(UITheme.ACCENT_CYAN);

        JLabel terminalSubtitle = new JLabel("SECURE 256-BIT ATM TERMINAL • APX-7809-B2");
        terminalSubtitle.setFont(UITheme.FONT_SUBTITLE);
        terminalSubtitle.setForeground(UITheme.TEXT_SECONDARY);

        brandPanel.add(brandTitle);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(terminalSubtitle);

        // Center Session Badge
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);
        userBadgeLabel = new JLabel("SESSION: READY FOR CARD");
        userBadgeLabel.setFont(UITheme.FONT_BODY_BOLD);
        userBadgeLabel.setForeground(UITheme.TEXT_MUTED);
        userBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_SUBTLE, 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        centerPanel.add(userBadgeLabel);

        // Right Telemetry & Clock
        JPanel telemetryPanel = new JPanel();
        telemetryPanel.setLayout(new BoxLayout(telemetryPanel, BoxLayout.Y_AXIS));
        telemetryPanel.setOpaque(false);

        dateLabel = new JLabel(LocalDateTime.now().format(DATE_FORMAT));
        dateLabel.setFont(UITheme.FONT_SUBTITLE);
        dateLabel.setForeground(UITheme.TEXT_SECONDARY);
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        timeLabel = new JLabel(LocalDateTime.now().format(TIME_FORMAT) + " EST");
        timeLabel.setFont(UITheme.FONT_HEADER);
        timeLabel.setForeground(UITheme.TEXT_PRIMARY);
        timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel statusBadge = new JLabel("● SYSTEM ONLINE");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusBadge.setForeground(UITheme.ACCENT_GREEN);
        statusBadge.setAlignmentX(Component.RIGHT_ALIGNMENT);

        telemetryPanel.add(timeLabel);
        telemetryPanel.add(Box.createVerticalStrut(2));
        telemetryPanel.add(dateLabel);
        telemetryPanel.add(Box.createVerticalStrut(2));
        telemetryPanel.add(statusBadge);

        add(brandPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(telemetryPanel, BorderLayout.EAST);

        // Timer for real-time clock tick
        Timer clockTimer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            timeLabel.setText(now.format(TIME_FORMAT) + " EST");
            dateLabel.setText(now.format(DATE_FORMAT));
        });
        clockTimer.start();
    }

    public void setSessionCustomer(Customer customer) {
        if (customer != null) {
            userBadgeLabel.setText(String.format("CARD: %s (%s)", customer.getMaskedCardNumber(), customer.getFullName()));
            userBadgeLabel.setForeground(UITheme.ACCENT_GREEN);
        } else {
            userBadgeLabel.setText("SESSION: INSERT CARD / ENTER PIN");
            userBadgeLabel.setForeground(UITheme.TEXT_MUTED);
        }
    }
}
