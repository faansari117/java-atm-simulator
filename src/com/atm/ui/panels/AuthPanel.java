package com.atm.ui.panels;

import com.atm.exception.AccountLockedException;
import com.atm.exception.InvalidPinException;
import com.atm.model.Customer;
import com.atm.service.BankingService;
import com.atm.ui.ATMFrame;
import com.atm.ui.UITheme;
import com.atm.ui.component.ModernButton;
import com.atm.ui.component.NumericKeypadPanel;
import com.atm.ui.component.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Screen 1: Authentication Panel.
 * Features an on-screen numeric keypad GUI, secure masked PIN input field,
 * dynamic status message box, and 3-strike safe security enforcement.
 */
public class AuthPanel extends JPanel {
    private final ATMFrame parentFrame;
    private final BankingService bankingService;

    private JPasswordField pinField;
    private JLabel statusTitleLabel;
    private JLabel statusSubtitleLabel;
    private JLabel strikeIndicatorLabel;
    private RoundedPanel statusCard;
    private NumericKeypadPanel keypad;
    private ModernButton submitBtn;
    private ModernButton clearBtn;
    private ModernButton unlockDemoBtn;

    public AuthPanel(ATMFrame parentFrame, BankingService bankingService) {
        this.parentFrame = parentFrame;
        this.bankingService = bankingService;

        setLayout(new GridBagLayout());
        setOpaque(false);

        buildUI();
    }

    private void buildUI() {
        // Main container card
        RoundedPanel mainCard = new RoundedPanel(18, UITheme.BG_PANEL, UITheme.BORDER_SUBTLE, 1);
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        mainCard.setPreferredSize(new Dimension(520, 640));

        // 1. Card Chip & Reader Simulation Bar
        RoundedPanel cardSlotBar = new RoundedPanel(10, UITheme.BG_DARK, UITheme.ACCENT_CYAN, 1);
        cardSlotBar.setLayout(new BorderLayout());
        cardSlotBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        cardSlotBar.setMaximumSize(new Dimension(460, 45));

        JLabel chipIcon = new JLabel("💳 SMART CHIP INSERTED");
        chipIcon.setFont(UITheme.FONT_BODY_BOLD);
        chipIcon.setForeground(UITheme.ACCENT_CYAN);

        JLabel cardNumLabel = new JLabel("4532 •••• •••• 8921");
        cardNumLabel.setFont(UITheme.FONT_MONOSPACE_BOLD);
        cardNumLabel.setForeground(UITheme.TEXT_PRIMARY);

        cardSlotBar.add(chipIcon, BorderLayout.WEST);
        cardSlotBar.add(cardNumLabel, BorderLayout.EAST);
        mainCard.add(cardSlotBar);
        mainCard.add(Box.createVerticalStrut(15));

        // 2. Status Message Box
        statusCard = new RoundedPanel(12, UITheme.BG_CARD, UITheme.BORDER_SUBTLE, 1);
        statusCard.setLayout(new BoxLayout(statusCard, BoxLayout.Y_AXIS));
        statusCard.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        statusCard.setMaximumSize(new Dimension(460, 75));

        statusTitleLabel = new JLabel("AUTHENTICATION REQUIRED");
        statusTitleLabel.setFont(UITheme.FONT_HEADER);
        statusTitleLabel.setForeground(UITheme.ACCENT_BLUE);
        statusTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusSubtitleLabel = new JLabel("Please enter your 4-digit security PIN to access your accounts.");
        statusSubtitleLabel.setFont(UITheme.FONT_SUBTITLE);
        statusSubtitleLabel.setForeground(UITheme.TEXT_SECONDARY);
        statusSubtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        strikeIndicatorLabel = new JLabel("Security Attempts: [ ● ● ● ] (3 strikes remaining)");
        strikeIndicatorLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        strikeIndicatorLabel.setForeground(UITheme.ACCENT_GREEN);
        strikeIndicatorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusCard.add(statusTitleLabel);
        statusCard.add(Box.createVerticalStrut(4));
        statusCard.add(statusSubtitleLabel);
        statusCard.add(Box.createVerticalStrut(4));
        statusCard.add(strikeIndicatorLabel);

        mainCard.add(statusCard);
        mainCard.add(Box.createVerticalStrut(20));

        // 3. PIN Mask Input Field
        JPanel pinWrapper = new JPanel();
        pinWrapper.setLayout(new BoxLayout(pinWrapper, BoxLayout.Y_AXIS));
        pinWrapper.setOpaque(false);
        pinWrapper.setMaximumSize(new Dimension(460, 80));

        JLabel pinPrompt = new JLabel("SECURE PERSONAL IDENTIFICATION NUMBER (PIN):");
        pinPrompt.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pinPrompt.setForeground(UITheme.TEXT_SECONDARY);
        pinPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);

        pinField = new JPasswordField(10);
        pinField.setFont(UITheme.FONT_PIN_INPUT);
        pinField.setHorizontalAlignment(JTextField.CENTER);
        pinField.setBackground(UITheme.BG_INPUT);
        pinField.setForeground(UITheme.TEXT_PRIMARY);
        pinField.setCaretColor(UITheme.ACCENT_CYAN);
        pinField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_ACTIVE, 2, true),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        pinField.setMaximumSize(new Dimension(280, 50));
        pinField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Allow pressing Enter on physical keyboard to trigger submit
        pinField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    processAuthentication();
                }
            }
        });

        pinWrapper.add(pinPrompt);
        pinWrapper.add(Box.createVerticalStrut(6));
        pinWrapper.add(pinField);

        mainCard.add(pinWrapper);
        mainCard.add(Box.createVerticalStrut(15));

        // 4. Interactive Numeric Keypad
        keypad = new NumericKeypadPanel(false);
        keypad.setActiveField(pinField);
        keypad.setMaximumSize(new Dimension(280, 240));
        mainCard.add(keypad);
        mainCard.add(Box.createVerticalStrut(15));

        // 5. Action Buttons (Submit & Clear)
        JPanel actionBtnRow = new JPanel(new GridLayout(1, 2, 15, 0));
        actionBtnRow.setOpaque(false);
        actionBtnRow.setMaximumSize(new Dimension(380, 45));

        clearBtn = new ModernButton("CLEAR PIN", ModernButton.Style.SECONDARY);
        clearBtn.addActionListener(e -> {
            pinField.setText("");
            pinField.requestFocusInWindow();
        });

        submitBtn = new ModernButton("ENTER / SUBMIT", ModernButton.Style.SUCCESS);
        submitBtn.setFont(UITheme.FONT_HEADER);
        submitBtn.addActionListener(e -> processAuthentication());

        actionBtnRow.add(clearBtn);
        actionBtnRow.add(submitBtn);
        mainCard.add(actionBtnRow);
        mainCard.add(Box.createVerticalStrut(15));

        // 6. Demo Hint & Administrative Unlock Link
        JPanel demoBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        demoBar.setOpaque(false);

        JLabel demoHint = new JLabel("Default Demo PIN: 1234  | ");
        demoHint.setFont(UITheme.FONT_SUBTITLE);
        demoHint.setForeground(UITheme.TEXT_MUTED);

        unlockDemoBtn = new ModernButton("Unlock Terminal (Admin)", ModernButton.Style.OUTLINE);
        unlockDemoBtn.setPreferredSize(new Dimension(160, 26));
        unlockDemoBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        unlockDemoBtn.addActionListener(e -> adminResetLockout());

        demoBar.add(demoHint);
        demoBar.add(unlockDemoBtn);
        mainCard.add(demoBar);

        add(mainCard);
    }

    /**
     * Executes authentication against the banking service and handles security rules.
     */
    private void processAuthentication() {
        char[] pinChars = pinField.getPassword();
        String enteredPin = new String(pinChars);

        if (enteredPin.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your 4-digit PIN using the on-screen keypad or keyboard.",
                    "PIN Required",
                    JOptionPane.WARNING_MESSAGE);
            pinField.requestFocusInWindow();
            return;
        }

        try {
            Customer customer = bankingService.authenticate(enteredPin);
            // Successful Login
            pinField.setText("");
            resetStatusDisplay();
            parentFrame.onAuthenticationSuccess(customer);

        } catch (InvalidPinException ex) {
            pinField.setText("");
            pinField.requestFocusInWindow();

            int remaining = ex.getRemainingAttempts();
            boolean isLocked = ex.isLocked();

            if (isLocked) {
                applyLockedState();
                JOptionPane.showMessageDialog(this,
                        "ACCOUNT LOCKED!\n\nYou have entered an invalid PIN 3 consecutive times.\n" +
                                "For your security, this terminal has locked access and your card has been retained.\n" +
                                "Please contact Metro Apex Bank customer service at 1-800-555-APEX.",
                        "Security Lockout - Terminal Disabled",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                updateStrikeDisplay(ex.getAttemptNumber(), remaining);
                JOptionPane.showMessageDialog(this,
                        String.format("Invalid PIN entered.\n\nStrike %d of 3.\nYou have %d attempt(s) remaining before account lockout.",
                                ex.getAttemptNumber(), remaining),
                        "Authentication Failed",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (AccountLockedException ex) {
            applyLockedState();
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Account Locked",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStrikeDisplay(int strikeCount, int remaining) {
        statusCard.setBackgroundColor(new Color(0x45, 0x1A, 0x1A)); // Red tint
        statusCard.setBorderColor(UITheme.ACCENT_RED);

        statusTitleLabel.setText("INVALID PIN - STRIKE " + strikeCount + " OF 3");
        statusTitleLabel.setForeground(UITheme.ACCENT_RED);

        statusSubtitleLabel.setText("Security warning: Too many failed attempts will lock your card.");
        statusSubtitleLabel.setForeground(UITheme.TEXT_PRIMARY);

        String strikes = "";
        for (int i = 0; i < 3; i++) {
            if (i < strikeCount) {
                strikes += " ✖";
            } else {
                strikes += " ○";
            }
        }
        strikeIndicatorLabel.setText("Strikes:" + strikes + " (" + remaining + " remaining)");
        strikeIndicatorLabel.setForeground(UITheme.ACCENT_AMBER);
    }

    private void applyLockedState() {
        statusCard.setBackgroundColor(new Color(0x3B, 0x0D, 0x0D));
        statusCard.setBorderColor(UITheme.ACCENT_RED);

        statusTitleLabel.setText("TERMINAL ACCESS LOCKED");
        statusTitleLabel.setForeground(UITheme.ACCENT_RED);

        statusSubtitleLabel.setText("3 consecutive invalid PIN entries detected. Terminal disabled.");
        statusSubtitleLabel.setForeground(UITheme.TEXT_SECONDARY);

        strikeIndicatorLabel.setText("Status: ✖ ✖ ✖ (CARD RETAINED / LOCKED)");
        strikeIndicatorLabel.setForeground(UITheme.ACCENT_RED);

        // Disable input components
        pinField.setEnabled(false);
        submitBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        keypad.setKeypadEnabled(false);
    }

    private void resetStatusDisplay() {
        statusCard.setBackgroundColor(UITheme.BG_CARD);
        statusCard.setBorderColor(UITheme.BORDER_SUBTLE);

        statusTitleLabel.setText("AUTHENTICATION REQUIRED");
        statusTitleLabel.setForeground(UITheme.ACCENT_BLUE);

        statusSubtitleLabel.setText("Please enter your 4-digit security PIN to access your accounts.");
        statusSubtitleLabel.setForeground(UITheme.TEXT_SECONDARY);

        strikeIndicatorLabel.setText("Security Attempts: [ ● ● ● ] (3 strikes remaining)");
        strikeIndicatorLabel.setForeground(UITheme.ACCENT_GREEN);

        pinField.setEnabled(true);
        submitBtn.setEnabled(true);
        clearBtn.setEnabled(true);
        keypad.setKeypadEnabled(true);
    }

    private void adminResetLockout() {
        bankingService.unlockCustomer();
        resetStatusDisplay();
        pinField.setText("");
        pinField.requestFocusInWindow();
        JOptionPane.showMessageDialog(this,
                "Admin Override: Terminal security lockout has been reset.\nCard restored to active status (PIN: 1234).",
                "Lockout Reset Successful",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void onPanelShown() {
        pinField.setText("");
        if (bankingService.isLocked()) {
            applyLockedState();
        } else {
            resetStatusDisplay();
            pinField.requestFocusInWindow();
        }
    }
}
