package com.atm.ui.panels;

import com.atm.model.Account;
import com.atm.model.AccountType;
import com.atm.model.Customer;
import com.atm.service.BankingService;
import com.atm.ui.ATMFrame;
import com.atm.ui.UITheme;
import com.atm.ui.component.ModernButton;
import com.atm.ui.component.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Screen 2: Main Dashboard Panel.
 * Presents real-time multi-account matrix balances, daily withdrawal quota gauge,
 * and an interactive grid of primary ATM operations.
 */
public class DashboardPanel extends JPanel {
    private final ATMFrame parentFrame;
    private final BankingService bankingService;

    private JLabel welcomeLabel;
    private JLabel cardDetailsLabel;
    private JLabel checkingBalanceLabel;
    private JLabel savingsBalanceLabel;
    private JLabel dailyLimitRemainingLabel;
    private JProgressBar dailyLimitBar;

    public DashboardPanel(ATMFrame parentFrame, BankingService bankingService) {
        this.parentFrame = parentFrame;
        this.bankingService = bankingService;

        setLayout(new BorderLayout(0, 15));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 30, 20, 30));

        buildUI();
    }

    private void buildUI() {
        // --- TOP SECTION: Customer Banner & Multi-Account Matrix ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Welcome Header Bar
        RoundedPanel welcomeCard = new RoundedPanel(12, UITheme.BG_PANEL, UITheme.BORDER_SUBTLE, 1);
        welcomeCard.setLayout(new BorderLayout());
        welcomeCard.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel welcomeTextPanel = new JPanel();
        welcomeTextPanel.setLayout(new BoxLayout(welcomeTextPanel, BoxLayout.Y_AXIS));
        welcomeTextPanel.setOpaque(false);

        welcomeLabel = new JLabel("Welcome, Alexander Mercer");
        welcomeLabel.setFont(UITheme.FONT_TITLE);
        welcomeLabel.setForeground(UITheme.TEXT_PRIMARY);

        cardDetailsLabel = new JLabel("Card: 4532 •••• •••• 8921 | Verified Tier-1 Client");
        cardDetailsLabel.setFont(UITheme.FONT_SUBTITLE);
        cardDetailsLabel.setForeground(UITheme.TEXT_SECONDARY);

        welcomeTextPanel.add(welcomeLabel);
        welcomeTextPanel.add(Box.createVerticalStrut(2));
        welcomeTextPanel.add(cardDetailsLabel);

        ModernButton refreshBtn = new ModernButton("⟳ Refresh", ModernButton.Style.OUTLINE);
        refreshBtn.setPreferredSize(new Dimension(100, 32));
        refreshBtn.addActionListener(e -> refreshBalances());

        welcomeCard.add(welcomeTextPanel, BorderLayout.WEST);
        welcomeCard.add(refreshBtn, BorderLayout.EAST);
        topContainer.add(welcomeCard);
        topContainer.add(Box.createVerticalStrut(12));

        // Account Matrix Row (Checking, Savings, Daily Limit)
        JPanel matrixRow = new JPanel(new GridLayout(1, 3, 15, 0));
        matrixRow.setOpaque(false);

        // Checking Card
        RoundedPanel checkingCard = createAccountCard(
                "CHECKING ALLOCATION",
                "CHK-89210-01",
                UITheme.ACCENT_BLUE,
                checkingBalanceLabel = new JLabel("$0.00"),
                "Available for cash withdrawal & POS"
        );

        // Savings Card
        RoundedPanel savingsCard = createAccountCard(
                "SAVINGS ALLOCATION",
                "SAV-89210-02",
                UITheme.ACCENT_GREEN,
                savingsBalanceLabel = new JLabel("$0.00"),
                "High-Yield Interest Bearing Account"
        );

        // Daily Limit Quota Card
        RoundedPanel limitCard = new RoundedPanel(12, UITheme.BG_PANEL, UITheme.BORDER_SUBTLE, 1);
        limitCard.setLayout(new BoxLayout(limitCard, BoxLayout.Y_AXIS));
        limitCard.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel limitTitle = new JLabel("DAILY ATM WITHDRAWAL LIMIT");
        limitTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        limitTitle.setForeground(UITheme.ACCENT_AMBER);

        dailyLimitRemainingLabel = new JLabel("$1,000.00 Remaining");
        dailyLimitRemainingLabel.setFont(UITheme.FONT_TITLE);
        dailyLimitRemainingLabel.setForeground(UITheme.TEXT_PRIMARY);

        dailyLimitBar = new JProgressBar(0, 1000);
        dailyLimitBar.setValue(1000);
        dailyLimitBar.setForeground(UITheme.ACCENT_GREEN);
        dailyLimitBar.setBackground(UITheme.BG_DARK);
        dailyLimitBar.setBorderPainted(false);
        dailyLimitBar.setPreferredSize(new Dimension(180, 8));

        JLabel limitSubtitle = new JLabel("Max limit: $1,000.00 per calendar day");
        limitSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        limitSubtitle.setForeground(UITheme.TEXT_MUTED);

        limitCard.add(limitTitle);
        limitCard.add(Box.createVerticalStrut(4));
        limitCard.add(dailyLimitRemainingLabel);
        limitCard.add(Box.createVerticalStrut(8));
        limitCard.add(dailyLimitBar);
        limitCard.add(Box.createVerticalStrut(6));
        limitCard.add(limitSubtitle);

        matrixRow.add(checkingCard);
        matrixRow.add(savingsCard);
        matrixRow.add(limitCard);

        topContainer.add(matrixRow);
        add(topContainer, BorderLayout.NORTH);

        // --- CENTER SECTION: Grid of Action Buttons ---
        JPanel actionsGrid = new JPanel(new GridLayout(2, 4, 15, 15));
        actionsGrid.setOpaque(false);
        actionsGrid.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 1. Check Balance
        ModernButton balanceBtn = createActionButton(
                "Check Balance",
                "Instant multi-account overview",
                "🔍",
                ModernButton.Style.SECONDARY
        );
        balanceBtn.addActionListener(e -> showBalanceInquiryDialog());

        // 2. Deposit Funds
        ModernButton depositBtn = createActionButton(
                "Deposit Funds",
                "Credit Checking or Savings",
                "💰",
                ModernButton.Style.SUCCESS
        );
        depositBtn.addActionListener(e -> parentFrame.navigateToTransaction(TransactionPanel.Mode.DEPOSIT));

        // 3. Withdraw Cash
        ModernButton withdrawBtn = createActionButton(
                "Withdraw Cash",
                "Fast cash or custom dispensing",
                "💵",
                ModernButton.Style.PRIMARY
        );
        withdrawBtn.addActionListener(e -> parentFrame.navigateToTransaction(TransactionPanel.Mode.WITHDRAW));

        // 4. Transfer Allocations
        ModernButton transferBtn = createActionButton(
                "Transfer Funds",
                "Cross-account reallocation",
                "⇄",
                ModernButton.Style.PRIMARY
        );
        transferBtn.addActionListener(e -> parentFrame.navigateToTransaction(TransactionPanel.Mode.TRANSFER));

        // 5. Mini-Statement
        ModernButton miniStmtBtn = createActionButton(
                "Mini-Statement",
                "Last 5 real-time transactions",
                "📄",
                ModernButton.Style.SECONDARY
        );
        miniStmtBtn.addActionListener(e -> parentFrame.navigateToLedger(true));

        // 6. Full Ledger History
        ModernButton fullLedgerBtn = createActionButton(
                "Full Ledger",
                "Complete audit history & logs",
                "📜",
                ModernButton.Style.SECONDARY
        );
        fullLedgerBtn.addActionListener(e -> parentFrame.navigateToLedger(false));

        // 7. PIN Change
        ModernButton pinChangeBtn = createActionButton(
                "Change PIN",
                "Update 4-digit security code",
                "🔒",
                ModernButton.Style.WARNING
        );
        pinChangeBtn.addActionListener(e -> parentFrame.navigateToTransaction(TransactionPanel.Mode.PIN_CHANGE));

        // 8. Exit / Card Eject
        ModernButton exitBtn = createActionButton(
                "End Session / Exit",
                "Safely terminate session & eject card",
                "⏻",
                ModernButton.Style.DANGER
        );
        exitBtn.addActionListener(e -> handleLogout());

        actionsGrid.add(balanceBtn);
        actionsGrid.add(depositBtn);
        actionsGrid.add(withdrawBtn);
        actionsGrid.add(transferBtn);
        actionsGrid.add(miniStmtBtn);
        actionsGrid.add(fullLedgerBtn);
        actionsGrid.add(pinChangeBtn);
        actionsGrid.add(exitBtn);

        add(actionsGrid, BorderLayout.CENTER);
    }

    private RoundedPanel createAccountCard(String title, String accountNum, Color accentColor,
                                           JLabel balanceLabel, String subtitle) {
        RoundedPanel card = new RoundedPanel(12, UITheme.BG_PANEL, UITheme.BORDER_SUBTLE, 1);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(accentColor);

        JLabel acctNumLabel = new JLabel(accountNum);
        acctNumLabel.setFont(UITheme.FONT_MONOSPACE);
        acctNumLabel.setForeground(UITheme.TEXT_MUTED);

        balanceLabel.setFont(UITheme.FONT_DISPLAY_AMOUNT);
        balanceLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLabel.setForeground(UITheme.TEXT_MUTED);

        card.add(titleLabel);
        card.add(acctNumLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(balanceLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(subLabel);

        return card;
    }

    private ModernButton createActionButton(String title, String subtitle, String icon, ModernButton.Style style) {
        ModernButton btn = new ModernButton("", style);
        btn.setLayout(new BorderLayout(8, 0));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
        iconLabel.setForeground(UITheme.TEXT_PRIMARY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLabel.setForeground(new Color(0xCB, 0xD5, 0xE1));

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(subLabel);

        btn.add(iconLabel, BorderLayout.WEST);
        btn.add(textPanel, BorderLayout.CENTER);
        return btn;
    }

    public void refreshBalances() {
        if (!bankingService.isAuthenticated()) {
            return;
        }

        Customer cust = bankingService.getCurrentCustomer();
        if (cust != null) {
            welcomeLabel.setText("Welcome back, " + cust.getFullName());
            cardDetailsLabel.setText("Card: " + cust.getMaskedCardNumber() + " | Status: Active");

            BigDecimal chkBal = bankingService.getBalance(AccountType.CHECKING);
            BigDecimal savBal = bankingService.getBalance(AccountType.SAVINGS);
            BigDecimal remainingLimit = bankingService.getRemainingDailyWithdrawalLimit(AccountType.CHECKING);

            checkingBalanceLabel.setText(String.format("$%,.2f", chkBal));
            savingsBalanceLabel.setText(String.format("$%,.2f", savBal));
            dailyLimitRemainingLabel.setText(String.format("$%,.2f Remaining", remainingLimit));

            int limitInt = remainingLimit.intValue();
            dailyLimitBar.setValue(Math.min(1000, Math.max(0, limitInt)));

            if (limitInt < 250) {
                dailyLimitBar.setForeground(UITheme.ACCENT_RED);
            } else if (limitInt < 500) {
                dailyLimitBar.setForeground(UITheme.ACCENT_AMBER);
            } else {
                dailyLimitBar.setForeground(UITheme.ACCENT_GREEN);
            }
        }
    }

    private void showBalanceInquiryDialog() {
        BigDecimal chkBal = bankingService.getBalance(AccountType.CHECKING);
        BigDecimal savBal = bankingService.getBalance(AccountType.SAVINGS);
        BigDecimal remainingLimit = bankingService.getRemainingDailyWithdrawalLimit(AccountType.CHECKING);
        BigDecimal totalWealth = chkBal.add(savBal);

        String message = String.format(
                "===============================================\n" +
                        "           ACCOUNT BALANCE INQUIRY SUMMARY      \n" +
                        "===============================================\n" +
                        "Checking Account (CHK-89210-01):   $%,.2f\n" +
                        "Savings Account  (SAV-89210-02):   $%,.2f\n" +
                        "-----------------------------------------------\n" +
                        "Combined Total Liquid Assets:       $%,.2f\n" +
                        "-----------------------------------------------\n" +
                        "Today's Remaining Cash Limit:       $%,.2f\n" +
                        "===============================================",
                chkBal, savBal, totalWealth, remainingLimit
        );

        JTextArea textArea = new JTextArea(message);
        textArea.setFont(UITheme.FONT_MONOSPACE_BOLD);
        textArea.setEditable(false);
        textArea.setBackground(UITheme.BG_DARK);
        textArea.setForeground(UITheme.TEXT_PRIMARY);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JOptionPane.showMessageDialog(this,
                textArea,
                "Account Balance Inquiry",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you wish to end your session?\nYour ATM card will be safely ejected.",
                "Confirm Session Termination",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            bankingService.logout();
            JOptionPane.showMessageDialog(this,
                    "Session closed. Please take your card and receipt.\nThank you for banking with Metro Apex Bank!",
                    "Card Ejected",
                    JOptionPane.INFORMATION_MESSAGE);
            parentFrame.navigateToAuth();
        }
    }

    public void onPanelShown() {
        refreshBalances();
    }
}
