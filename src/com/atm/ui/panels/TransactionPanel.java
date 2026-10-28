package com.atm.ui.panels;

import com.atm.exception.*;
import com.atm.model.AccountType;
import com.atm.model.TransactionRecord;
import com.atm.service.BankingService;
import com.atm.ui.ATMFrame;
import com.atm.ui.UITheme;
import com.atm.ui.component.ModernButton;
import com.atm.ui.component.NumericKeypadPanel;
import com.atm.ui.component.RoundedPanel;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Screen 3: Transaction Execution Panel.
 * Adapts to Deposit, Withdraw, Transfer, and PIN Change operations.
 * Houses universal entry fields, account dropdown selectors, on-screen numeric keypad,
 * quick preset chips, and intercepts custom checked exceptions with explicit UI dialogs.
 */
public class TransactionPanel extends JPanel {

    public enum Mode {
        DEPOSIT("DEPOSIT FUNDS", "Cash & Check Envelope Deposit", UITheme.ACCENT_GREEN),
        WITHDRAW("WITHDRAW CASH", "Cash Dispenser Terminal", UITheme.ACCENT_BLUE),
        TRANSFER("TRANSFER ALLOCATIONS", "Cross-Account Inter-Allocation Transfer", UITheme.ACCENT_PURPLE),
        PIN_CHANGE("UPDATE PIN", "Security Credential Management", UITheme.ACCENT_AMBER);

        final String title;
        final String subtitle;
        final Color themeColor;

        Mode(String title, String subtitle, Color themeColor) {
            this.title = title;
            this.subtitle = subtitle;
            this.themeColor = themeColor;
        }
    }

    private final ATMFrame parentFrame;
    private final BankingService bankingService;

    private Mode currentMode = Mode.DEPOSIT;

    // Header Labels
    private JLabel modeTitleLabel;
    private JLabel modeSubtitleLabel;

    // Financial Controls
    private JPanel formCard;
    private JComboBox<AccountType> primaryAccountCombo;
    private JComboBox<AccountType> targetAccountCombo;
    private JLabel targetAccountLabel;
    private JTextField amountField;
    private JLabel availableBalanceBadge;
    private JLabel dailyLimitBadge;

    // PIN Change Controls
    private JPanel pinChangeCard;
    private JPasswordField currentPinField;
    private JPasswordField newPinField;
    private JPasswordField confirmPinField;

    // Keypad and Action Controls
    private NumericKeypadPanel keypad;
    private ModernButton submitBtn;
    private ModernButton clearBtn;
    private ModernButton backBtn;

    // Currently focused text component for keypad input
    private JTextComponent currentFocusedField;

    public TransactionPanel(ATMFrame parentFrame, BankingService bankingService) {
        this.parentFrame = parentFrame;
        this.bankingService = bankingService;

        setLayout(new BorderLayout(0, 15));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 30, 20, 30));

        buildUI();
    }

    private void buildUI() {
        // --- TOP: Mode Header & Back Navigation ---
        RoundedPanel headerCard = new RoundedPanel(12, UITheme.BG_PANEL, UITheme.BORDER_SUBTLE, 1);
        headerCard.setLayout(new BorderLayout());
        headerCard.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        modeTitleLabel = new JLabel("TRANSACTION EXECUTION");
        modeTitleLabel.setFont(UITheme.FONT_TITLE);
        modeTitleLabel.setForeground(UITheme.TEXT_PRIMARY);

        modeSubtitleLabel = new JLabel("Please enter your transaction parameters.");
        modeSubtitleLabel.setFont(UITheme.FONT_SUBTITLE);
        modeSubtitleLabel.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(modeTitleLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(modeSubtitleLabel);

        backBtn = new ModernButton("← Return to Dashboard", ModernButton.Style.SECONDARY);
        backBtn.setPreferredSize(new Dimension(180, 35));
        backBtn.addActionListener(e -> parentFrame.navigateToDashboard());

        headerCard.add(titlePanel, BorderLayout.WEST);
        headerCard.add(backBtn, BorderLayout.EAST);
        add(headerCard, BorderLayout.NORTH);

        // --- CENTER: Form Panel (Left) + Numeric Keypad & Quick Chips (Right) ---
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setOpaque(false);

        // LEFT COLUMN: Forms (Financial Entry & PIN Change)
        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new CardLayout());
        leftColumn.setOpaque(false);

        // Form 1: Financial Transaction Card (Deposit, Withdraw, Transfer)
        formCard = buildFinancialForm();
        leftColumn.add(formCard, "FINANCIAL");

        // Form 2: PIN Change Card
        pinChangeCard = buildPinChangeForm();
        leftColumn.add(pinChangeCard, "PIN_CHANGE");

        centerGrid.add(leftColumn);

        // RIGHT COLUMN: Keypad & Quick Amount Chips
        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setOpaque(false);

        RoundedPanel keypadContainer = new RoundedPanel(14, UITheme.BG_PANEL, UITheme.BORDER_SUBTLE, 1);
        keypadContainer.setLayout(new BoxLayout(keypadContainer, BoxLayout.Y_AXIS));
        keypadContainer.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel keypadHeader = new JLabel("ATM INTERACTIVE KEYPAD");
        keypadHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        keypadHeader.setForeground(UITheme.TEXT_SECONDARY);
        keypadHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        keypadContainer.add(keypadHeader);
        keypadContainer.add(Box.createVerticalStrut(10));

        // Quick Preset Chips (For fast ATM withdrawals/deposits)
        JPanel quickChipsRow = new JPanel(new GridLayout(1, 5, 6, 0));
        quickChipsRow.setOpaque(false);
        quickChipsRow.setMaximumSize(new Dimension(320, 35));

        int[] presets = {20, 50, 100, 200, 500};
        for (int amt : presets) {
            ModernButton chip = new ModernButton("$" + amt, ModernButton.Style.OUTLINE);
            chip.setFont(new Font("Segoe UI", Font.BOLD, 12));
            chip.addActionListener(e -> {
                if (currentMode != Mode.PIN_CHANGE) {
                    amountField.setText(String.valueOf(amt));
                    amountField.requestFocusInWindow();
                }
            });
            quickChipsRow.add(chip);
        }

        keypadContainer.add(quickChipsRow);
        keypadContainer.add(Box.createVerticalStrut(12));

        // Numeric Keypad (With decimal point enabled)
        keypad = new NumericKeypadPanel(true);
        keypad.setMaximumSize(new Dimension(300, 260));
        keypadContainer.add(keypad);
        keypadContainer.add(Box.createVerticalStrut(15));

        // Action Controls (Submit / Clear)
        JPanel actionRow = new JPanel(new GridLayout(1, 2, 12, 0));
        actionRow.setOpaque(false);
        actionRow.setMaximumSize(new Dimension(320, 45));

        clearBtn = new ModernButton("CLEAR", ModernButton.Style.SECONDARY);
        clearBtn.addActionListener(e -> clearActiveField());

        submitBtn = new ModernButton("CONFIRM & EXECUTE", ModernButton.Style.PRIMARY);
        submitBtn.setFont(UITheme.FONT_HEADER);
        submitBtn.addActionListener(e -> executeTransaction());

        actionRow.add(clearBtn);
        actionRow.add(submitBtn);
        keypadContainer.add(actionRow);

        rightColumn.add(keypadContainer);
        centerGrid.add(rightColumn);

        add(centerGrid, BorderLayout.CENTER);
    }

    private RoundedPanel buildFinancialForm() {
        RoundedPanel card = new RoundedPanel(14, UITheme.BG_PANEL, UITheme.BORDER_SUBTLE, 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Primary Account Selector
        JLabel acctLabel = new JLabel("SELECT ACCOUNT CHANNEL:");
        acctLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        acctLabel.setForeground(UITheme.TEXT_SECONDARY);

        primaryAccountCombo = new JComboBox<>(AccountType.values());
        primaryAccountCombo.setFont(UITheme.FONT_BODY_BOLD);
        primaryAccountCombo.setBackground(UITheme.BG_INPUT);
        primaryAccountCombo.setForeground(UITheme.TEXT_PRIMARY);
        primaryAccountCombo.setMaximumSize(new Dimension(400, 40));
        primaryAccountCombo.addActionListener(e -> updateBalanceBadges());

        // Target Account Selector (Visible only in TRANSFER mode)
        targetAccountLabel = new JLabel("TRANSFER DESTINATION CHANNEL:");
        targetAccountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        targetAccountLabel.setForeground(UITheme.ACCENT_CYAN);

        targetAccountCombo = new JComboBox<>(AccountType.values());
        targetAccountCombo.setFont(UITheme.FONT_BODY_BOLD);
        targetAccountCombo.setBackground(UITheme.BG_INPUT);
        targetAccountCombo.setForeground(UITheme.TEXT_PRIMARY);
        targetAccountCombo.setMaximumSize(new Dimension(400, 40));

        // Available Balance & Limit Badges Card
        RoundedPanel badgeCard = new RoundedPanel(8, UITheme.BG_CARD, UITheme.BORDER_SUBTLE, 1);
        badgeCard.setLayout(new GridLayout(2, 1, 4, 4));
        badgeCard.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        badgeCard.setMaximumSize(new Dimension(400, 55));

        availableBalanceBadge = new JLabel("Available Balance: $0.00");
        availableBalanceBadge.setFont(UITheme.FONT_BODY_BOLD);
        availableBalanceBadge.setForeground(UITheme.ACCENT_GREEN);

        dailyLimitBadge = new JLabel("Daily Cash Dispensing Allowance: $1,000.00");
        dailyLimitBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dailyLimitBadge.setForeground(UITheme.ACCENT_AMBER);

        badgeCard.add(availableBalanceBadge);
        badgeCard.add(dailyLimitBadge);

        // Universal Entry Field
        JLabel amtPrompt = new JLabel("ENTER TRANSACTION AMOUNT (USD):");
        amtPrompt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        amtPrompt.setForeground(UITheme.TEXT_SECONDARY);

        JPanel amtWrapper = new JPanel(new BorderLayout(8, 0));
        amtWrapper.setOpaque(false);
        amtWrapper.setMaximumSize(new Dimension(400, 50));

        JLabel dollarSign = new JLabel("$");
        dollarSign.setFont(UITheme.FONT_DISPLAY_AMOUNT);
        dollarSign.setForeground(UITheme.ACCENT_CYAN);

        amountField = new JTextField();
        amountField.setFont(UITheme.FONT_DISPLAY_AMOUNT);
        amountField.setBackground(UITheme.BG_INPUT);
        amountField.setForeground(UITheme.TEXT_PRIMARY);
        amountField.setCaretColor(UITheme.ACCENT_CYAN);
        amountField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_ACTIVE, 2, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));

        amountField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setActiveKeypadTarget(amountField);
            }
        });

        amtWrapper.add(dollarSign, BorderLayout.WEST);
        amtWrapper.add(amountField, BorderLayout.CENTER);

        // Assemble Left Card
        card.add(acctLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(primaryAccountCombo);
        card.add(Box.createVerticalStrut(10));

        card.add(targetAccountLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(targetAccountCombo);
        card.add(Box.createVerticalStrut(12));

        card.add(badgeCard);
        card.add(Box.createVerticalStrut(16));

        card.add(amtPrompt);
        card.add(Box.createVerticalStrut(6));
        card.add(amtWrapper);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private RoundedPanel buildPinChangeForm() {
        RoundedPanel card = new RoundedPanel(14, UITheme.BG_PANEL, UITheme.BORDER_SUBTLE, 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel info = new JLabel("ENTER 4-DIGIT PIN CREDENTIALS:");
        info.setFont(UITheme.FONT_HEADER);
        info.setForeground(UITheme.ACCENT_AMBER);
        card.add(info);
        card.add(Box.createVerticalStrut(15));

        // 1. Current PIN
        JLabel currPrompt = new JLabel("Current PIN:");
        currPrompt.setFont(UITheme.FONT_BODY_BOLD);
        currPrompt.setForeground(UITheme.TEXT_SECONDARY);

        currentPinField = createPinField();
        currentPinField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setActiveKeypadTarget(currentPinField);
            }
        });

        card.add(currPrompt);
        card.add(Box.createVerticalStrut(4));
        card.add(currentPinField);
        card.add(Box.createVerticalStrut(12));

        // 2. New PIN
        JLabel newPrompt = new JLabel("New 4-Digit PIN:");
        newPrompt.setFont(UITheme.FONT_BODY_BOLD);
        newPrompt.setForeground(UITheme.TEXT_SECONDARY);

        newPinField = createPinField();
        newPinField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setActiveKeypadTarget(newPinField);
            }
        });

        card.add(newPrompt);
        card.add(Box.createVerticalStrut(4));
        card.add(newPinField);
        card.add(Box.createVerticalStrut(12));

        // 3. Confirm New PIN
        JLabel confirmPrompt = new JLabel("Confirm New PIN:");
        confirmPrompt.setFont(UITheme.FONT_BODY_BOLD);
        confirmPrompt.setForeground(UITheme.TEXT_SECONDARY);

        confirmPinField = createPinField();
        confirmPinField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setActiveKeypadTarget(confirmPinField);
            }
        });

        card.add(confirmPrompt);
        card.add(Box.createVerticalStrut(4));
        card.add(confirmPinField);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPasswordField createPinField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(UITheme.FONT_PIN_INPUT);
        pf.setHorizontalAlignment(JTextField.CENTER);
        pf.setBackground(UITheme.BG_INPUT);
        pf.setForeground(UITheme.TEXT_PRIMARY);
        pf.setCaretColor(UITheme.ACCENT_CYAN);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_ACTIVE, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        pf.setMaximumSize(new Dimension(400, 45));
        return pf;
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
        modeTitleLabel.setText(mode.title);
        modeTitleLabel.setForeground(mode.themeColor);
        modeSubtitleLabel.setText(mode.subtitle);

        CardLayout cl = (CardLayout) ((JPanel) formCard.getParent()).getLayout();
        if (mode == Mode.PIN_CHANGE) {
            cl.show((JPanel) formCard.getParent(), "PIN_CHANGE");
            currentPinField.setText("");
            newPinField.setText("");
            confirmPinField.setText("");
            setActiveKeypadTarget(currentPinField);
            currentPinField.requestFocusInWindow();
        } else {
            cl.show((JPanel) formCard.getParent(), "FINANCIAL");
            amountField.setText("");
            setActiveKeypadTarget(amountField);
            amountField.requestFocusInWindow();

            boolean isTransfer = (mode == Mode.TRANSFER);
            targetAccountLabel.setVisible(isTransfer);
            targetAccountCombo.setVisible(isTransfer);

            if (isTransfer) {
                primaryAccountCombo.setSelectedItem(AccountType.CHECKING);
                targetAccountCombo.setSelectedItem(AccountType.SAVINGS);
            }

            updateBalanceBadges();
        }

        // Update submit button appearance
        submitBtn.setText(mode == Mode.DEPOSIT ? "DEPOSIT FUNDS" :
                mode == Mode.WITHDRAW ? "DISPENSE CASH" :
                        mode == Mode.TRANSFER ? "EXECUTE TRANSFER" : "UPDATE PIN");
    }

    private void setActiveKeypadTarget(JTextComponent field) {
        this.currentFocusedField = field;
        keypad.setActiveField(field);
    }

    private void clearActiveField() {
        if (currentFocusedField != null) {
            currentFocusedField.setText("");
            currentFocusedField.requestFocusInWindow();
        } else if (currentMode == Mode.PIN_CHANGE) {
            currentPinField.setText("");
            newPinField.setText("");
            confirmPinField.setText("");
            currentPinField.requestFocusInWindow();
        } else {
            amountField.setText("");
            amountField.requestFocusInWindow();
        }
    }

    private void updateBalanceBadges() {
        if (!bankingService.isAuthenticated()) return;

        AccountType selected = (AccountType) primaryAccountCombo.getSelectedItem();
        if (selected != null) {
            BigDecimal bal = bankingService.getBalance(selected);
            BigDecimal remainingLimit = bankingService.getRemainingDailyWithdrawalLimit(selected);

            availableBalanceBadge.setText(String.format("Available Balance in %s: $%,.2f", selected.getDisplayName(), bal));

            if (currentMode == Mode.WITHDRAW) {
                dailyLimitBadge.setVisible(true);
                dailyLimitBadge.setText(String.format("Remaining Today's Cash Dispensing Limit: $%,.2f", remainingLimit));
            } else {
                dailyLimitBadge.setVisible(false);
            }
        }
    }

    /**
     * Dispatches the transaction to the backend BankingService.
     * Intercepts custom checked exceptions and presents them cleanly to the user.
     */
    private void executeTransaction() {
        if (currentMode == Mode.PIN_CHANGE) {
            executePinChange();
            return;
        }

        String amtStr = amountField.getText().trim();
        if (amtStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid monetary amount.",
                    "Input Missing",
                    JOptionPane.WARNING_MESSAGE);
            amountField.requestFocusInWindow();
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amtStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid monetary format. Please use numeric digits (e.g. 100 or 50.25).",
                    "Invalid Format",
                    JOptionPane.ERROR_MESSAGE);
            amountField.requestFocusInWindow();
            return;
        }

        AccountType primaryAcct = (AccountType) primaryAccountCombo.getSelectedItem();
        AccountType targetAcct = (AccountType) targetAccountCombo.getSelectedItem();

        try {
            switch (currentMode) {
                case DEPOSIT:
                    TransactionRecord depRecord = bankingService.deposit(primaryAcct, amount);
                    JOptionPane.showMessageDialog(this,
                            String.format("CASH SUCCESSFULLY DEPOSITED!\n\n" +
                                            "Account: %s\n" +
                                            "Amount Credited: $%,.2f\n" +
                                            "New Available Balance: $%,.2f\n" +
                                            "Transaction Reference: %s",
                                    primaryAcct.getDisplayName(),
                                    amount,
                                    depRecord.getBalanceAfter(),
                                    depRecord.getReferenceId()),
                            "Deposit Confirmed",
                            JOptionPane.INFORMATION_MESSAGE);
                    parentFrame.navigateToDashboard();
                    break;

                case WITHDRAW:
                    TransactionRecord withRecord = bankingService.withdraw(primaryAcct, amount);
                    JOptionPane.showMessageDialog(this,
                            String.format("CASH DISPENSED SUCCESSFULLY!\n\n" +
                                            "Please take your cash from the dispenser below.\n\n" +
                                            "Account: %s\n" +
                                            "Dispensed Amount: $%,.2f\n" +
                                            "Remaining Balance: $%,.2f\n" +
                                            "Transaction Reference: %s",
                                    primaryAcct.getDisplayName(),
                                    amount,
                                    withRecord.getBalanceAfter(),
                                    withRecord.getReferenceId()),
                            "Cash Dispensed",
                            JOptionPane.INFORMATION_MESSAGE);
                    parentFrame.navigateToDashboard();
                    break;

                case TRANSFER:
                    TransactionRecord[] transferRecords = bankingService.transfer(primaryAcct, targetAcct, amount);
                    JOptionPane.showMessageDialog(this,
                            String.format("CROSS-ALLOCATION TRANSFER COMPLETE!\n\n" +
                                            "From: %s\n" +
                                            "To: %s\n" +
                                            "Transferred Amount: $%,.2f\n" +
                                            "Source New Balance: $%,.2f\n" +
                                            "Destination New Balance: $%,.2f\n" +
                                            "Transaction Ref: %s",
                                    primaryAcct.getDisplayName(),
                                    targetAcct.getDisplayName(),
                                    amount,
                                    transferRecords[0].getBalanceAfter(),
                                    transferRecords[1].getBalanceAfter(),
                                    transferRecords[0].getReferenceId()),
                            "Transfer Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    parentFrame.navigateToDashboard();
                    break;

                default:
                    break;
            }

        } catch (InsufficientFundsException ex) {
            JOptionPane.showMessageDialog(this,
                    String.format("TRANSACTION DECLINED: INSUFFICIENT FUNDS!\n\n" +
                                    "Account: %s\n" +
                                    "Requested Amount: $%,.2f\n" +
                                    "Available Balance: $%,.2f\n\n" +
                                    "Please enter an amount within your available balance.",
                            ex.getAccountType().getDisplayName(),
                            ex.getRequestedAmount(),
                            ex.getAvailableBalance()),
                    "Insufficient Funds",
                    JOptionPane.WARNING_MESSAGE);
            amountField.requestFocusInWindow();

        } catch (DailyLimitExceededException ex) {
            JOptionPane.showMessageDialog(this,
                    String.format("TRANSACTION DECLINED: DAILY LIMIT EXCEEDED!\n\n" +
                                    "Requested Withdrawal: $%,.2f\n" +
                                    "Already Withdrawn Today: $%,.2f\n" +
                                    "Statutory Daily Limit: $%,.2f\n\n" +
                                    "Remaining Daily Headroom: $%,.2f\n\n" +
                                    "ATM security rules limit cash dispensing to $1,000.00 per day.",
                            ex.getAttemptedAmount(),
                            ex.getCurrentDailyWithdrawn(),
                            ex.getDailyLimit(),
                            ex.getRemainingDailyLimit()),
                    "Daily Limit Exceeded",
                    JOptionPane.WARNING_MESSAGE);
            amountField.requestFocusInWindow();

        } catch (InvalidTransactionException ex) {
            JOptionPane.showMessageDialog(this,
                    "INVALID TRANSACTION:\n\n" + ex.getMessage(),
                    "Transaction Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            amountField.requestFocusInWindow();

        } catch (AccountLockedException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Terminal Locked",
                    JOptionPane.ERROR_MESSAGE);
            parentFrame.navigateToAuth();
        }
    }

    private void executePinChange() {
        String oldPin = new String(currentPinField.getPassword());
        String newPin = new String(newPinField.getPassword());
        String confirmPin = new String(confirmPinField.getPassword());

        try {
            bankingService.changePin(oldPin, newPin, confirmPin);
            JOptionPane.showMessageDialog(this,
                    "SECURITY PIN UPDATED SUCCESSFULLY!\n\nYour new PIN is now active for future transactions.",
                    "PIN Updated",
                    JOptionPane.INFORMATION_MESSAGE);
            parentFrame.navigateToDashboard();

        } catch (InvalidPinException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Current PIN Verification Failed",
                    JOptionPane.ERROR_MESSAGE);
            currentPinField.setText("");
            currentPinField.requestFocusInWindow();

        } catch (InvalidTransactionException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "PIN Format Error",
                    JOptionPane.WARNING_MESSAGE);
            newPinField.setText("");
            confirmPinField.setText("");
            newPinField.requestFocusInWindow();

        } catch (AccountLockedException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Terminal Locked",
                    JOptionPane.ERROR_MESSAGE);
            parentFrame.navigateToAuth();
        }
    }

    public void onPanelShown() {
        updateBalanceBadges();
    }
}
