package com.atm.ui.panels;

import com.atm.model.AccountType;
import com.atm.model.Customer;
import com.atm.model.TransactionRecord;
import com.atm.service.BankingService;
import com.atm.service.LedgerService;
import com.atm.ui.ATMFrame;
import com.atm.ui.UITheme;
import com.atm.ui.component.ModernButton;
import com.atm.ui.component.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Screen 4: Ledger Display Panel.
 * Displays formatted real-time transaction logs and mini-statements inside
 * a high-contrast scrollable text view with receipt printing and filtering.
 */
public class LedgerPanel extends JPanel {
    private final ATMFrame parentFrame;
    private final BankingService bankingService;

    private JTextArea ledgerTextArea;
    private JLabel viewModeTitleLabel;
    private JLabel totalDepositsBadge;
    private JLabel totalWithdrawalsBadge;
    private JLabel recordCountBadge;

    private JRadioButton miniStatementRadio;
    private JRadioButton fullLedgerRadio;
    private JRadioButton checkingOnlyRadio;
    private JRadioButton savingsOnlyRadio;

    public LedgerPanel(ATMFrame parentFrame, BankingService bankingService) {
        this.parentFrame = parentFrame;
        this.bankingService = bankingService;

        setLayout(new BorderLayout(0, 15));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 30, 20, 30));

        buildUI();
    }

    private void buildUI() {
        // --- TOP: Header & Action Bar ---
        RoundedPanel topBar = new RoundedPanel(12, UITheme.BG_PANEL, UITheme.BORDER_SUBTLE, 1);
        topBar.setLayout(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        viewModeTitleLabel = new JLabel("TRANSACTION AUDIT LEDGER");
        viewModeTitleLabel.setFont(UITheme.FONT_TITLE);
        viewModeTitleLabel.setForeground(UITheme.ACCENT_CYAN);

        JLabel subLabel = new JLabel("Sequential real-time balance adjustment logs & mini-statements.");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);

        titlePanel.add(viewModeTitleLabel);
        titlePanel.add(Box.createVerticalStrut(2));
        titlePanel.add(subLabel);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActions.setOpaque(false);

        ModernButton printReceiptBtn = new ModernButton("🖨 Print Receipt", ModernButton.Style.SUCCESS);
        printReceiptBtn.setPreferredSize(new Dimension(140, 35));
        printReceiptBtn.addActionListener(e -> printThermalReceipt());

        ModernButton backBtn = new ModernButton("← Return to Dashboard", ModernButton.Style.SECONDARY);
        backBtn.setPreferredSize(new Dimension(180, 35));
        backBtn.addActionListener(e -> parentFrame.navigateToDashboard());

        topActions.add(printReceiptBtn);
        topActions.add(backBtn);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(topActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // --- CENTER: Filter Toolbar + Scrollable Monospace Text Area ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        // Filter Radios
        RoundedPanel filterBar = new RoundedPanel(10, UITheme.BG_CARD, UITheme.BORDER_SUBTLE, 1);
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

        JLabel filterLabel = new JLabel("Display Mode:");
        filterLabel.setFont(UITheme.FONT_BODY_BOLD);
        filterLabel.setForeground(UITheme.TEXT_PRIMARY);
        filterBar.add(filterLabel);

        ButtonGroup filterGroup = new ButtonGroup();
        miniStatementRadio = createFilterRadio("Mini-Statement (Last 5 Records)");
        fullLedgerRadio = createFilterRadio("Full Transaction Ledger");
        checkingOnlyRadio = createFilterRadio("Checking Only");
        savingsOnlyRadio = createFilterRadio("Savings Only");

        filterGroup.add(miniStatementRadio);
        filterGroup.add(fullLedgerRadio);
        filterGroup.add(checkingOnlyRadio);
        filterGroup.add(savingsOnlyRadio);

        miniStatementRadio.setSelected(true);

        miniStatementRadio.addActionListener(e -> refreshLedgerDisplay());
        fullLedgerRadio.addActionListener(e -> refreshLedgerDisplay());
        checkingOnlyRadio.addActionListener(e -> refreshLedgerDisplay());
        savingsOnlyRadio.addActionListener(e -> refreshLedgerDisplay());

        filterBar.add(miniStatementRadio);
        filterBar.add(fullLedgerRadio);
        filterBar.add(checkingOnlyRadio);
        filterBar.add(savingsOnlyRadio);

        centerPanel.add(filterBar, BorderLayout.NORTH);

        // Scrollable Ledger Text Area
        ledgerTextArea = new JTextArea();
        ledgerTextArea.setEditable(false);
        ledgerTextArea.setFont(UITheme.FONT_MONOSPACE);
        ledgerTextArea.setBackground(UITheme.BG_INPUT);
        ledgerTextArea.setForeground(new Color(0xE2, 0xE8, 0xF0));
        ledgerTextArea.setCaretColor(UITheme.ACCENT_CYAN);
        ledgerTextArea.setBorder(BorderFactory.createEmptyBorder(15, 18, 15, 18));

        JScrollPane scrollPane = new JScrollPane(ledgerTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_SUBTLE, 1));
        scrollPane.getViewport().setBackground(UITheme.BG_INPUT);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- BOTTOM: Summary Statistics Card ---
        RoundedPanel summaryBar = new RoundedPanel(12, UITheme.BG_PANEL, UITheme.BORDER_SUBTLE, 1);
        summaryBar.setLayout(new GridLayout(1, 4, 15, 0));
        summaryBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        totalDepositsBadge = createSummaryBadge("TOTAL DEPOSITS:", "$0.00", UITheme.ACCENT_GREEN);
        totalWithdrawalsBadge = createSummaryBadge("TOTAL WITHDRAWALS:", "$0.00", UITheme.ACCENT_RED);
        recordCountBadge = createSummaryBadge("ENTRIES LOGGED:", "0 Records", UITheme.ACCENT_CYAN);

        JLabel encryptionBadge = new JLabel("● 256-Bit Ledger Verification Active");
        encryptionBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        encryptionBadge.setForeground(UITheme.ACCENT_GREEN);
        encryptionBadge.setHorizontalAlignment(SwingConstants.RIGHT);

        summaryBar.add(totalDepositsBadge);
        summaryBar.add(totalWithdrawalsBadge);
        summaryBar.add(recordCountBadge);
        summaryBar.add(encryptionBadge);

        add(summaryBar, BorderLayout.SOUTH);
    }

    private JRadioButton createFilterRadio(String label) {
        JRadioButton radio = new JRadioButton(label);
        radio.setFont(UITheme.FONT_BODY);
        radio.setForeground(UITheme.TEXT_SECONDARY);
        radio.setOpaque(false);
        radio.setFocusPainted(false);
        return radio;
    }

    private JLabel createSummaryBadge(String title, String val, Color color) {
        JLabel label = new JLabel(String.format("<html><span style='color:#94a3b8; font-size:10px;'>%s</span> <b style='color:%s; font-size:12px;'>%s</b></html>",
                title, toHex(color), val));
        label.setFont(UITheme.FONT_BODY);
        return label;
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public void setMiniStatementMode(boolean miniOnly) {
        if (miniOnly) {
            miniStatementRadio.setSelected(true);
        } else {
            fullLedgerRadio.setSelected(true);
        }
        refreshLedgerDisplay();
    }

    public void refreshLedgerDisplay() {
        if (!bankingService.isAuthenticated()) return;

        Customer cust = bankingService.getCurrentCustomer();
        LedgerService ledger = bankingService.getLedgerService();

        String content;
        if (miniStatementRadio.isSelected()) {
            viewModeTitleLabel.setText("ATM MINI-STATEMENT (LAST 5 TRANSACTIONS)");
            content = ledger.formatMiniStatementText(cust.getFullName(), cust.getMaskedCardNumber());
        } else if (checkingOnlyRadio.isSelected()) {
            viewModeTitleLabel.setText("CHECKING ACCOUNT TRANSACTION LEDGER");
            content = formatFilteredRecords(ledger.getRecordsByAccount(AccountType.CHECKING), AccountType.CHECKING);
        } else if (savingsOnlyRadio.isSelected()) {
            viewModeTitleLabel.setText("SAVINGS ACCOUNT TRANSACTION LEDGER");
            content = formatFilteredRecords(ledger.getRecordsByAccount(AccountType.SAVINGS), AccountType.SAVINGS);
        } else {
            viewModeTitleLabel.setText("FULL ACCOUNT AUDIT LEDGER");
            content = ledger.formatFullLedgerText(cust.getFullName(), cust.getMaskedCardNumber());
        }

        ledgerTextArea.setText(content);
        ledgerTextArea.setCaretPosition(0);

        // Update Summary Badges
        BigDecimal deposits = ledger.calculateTotalDeposits();
        BigDecimal withdrawals = ledger.calculateTotalWithdrawals();
        int totalCount = ledger.getAllRecords().size();

        totalDepositsBadge.setText(String.format("<html><span style='color:#94a3b8; font-size:10px;'>TOTAL DEPOSITS:</span> <b style='color:%s; font-size:12px;'>$%,.2f</b></html>",
                toHex(UITheme.ACCENT_GREEN), deposits));
        totalWithdrawalsBadge.setText(String.format("<html><span style='color:#94a3b8; font-size:10px;'>TOTAL WITHDRAWALS:</span> <b style='color:%s; font-size:12px;'>$%,.2f</b></html>",
                toHex(UITheme.ACCENT_RED), withdrawals));
        recordCountBadge.setText(String.format("<html><span style='color:#94a3b8; font-size:10px;'>ENTRIES LOGGED:</span> <b style='color:%s; font-size:12px;'>%d Records</b></html>",
                toHex(UITheme.ACCENT_CYAN), totalCount));
    }

    private String formatFilteredRecords(List<TransactionRecord> list, AccountType acct) {
        Customer cust = bankingService.getCurrentCustomer();
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append(String.format("          METRO APEX BANK - %s LEDGER\n", acct.getDisplayName().toUpperCase()));
        sb.append("                       ATM TERMINAL ID: APX-7809-B2                     \n");
        sb.append("========================================================================\n");
        sb.append(String.format("Cardholder : %s\n", cust.getFullName()));
        sb.append(String.format("Card Number: %s\n", cust.getMaskedCardNumber()));
        sb.append("------------------------------------------------------------------------\n");
        sb.append(String.format("%-19s | %-12s | %-14s | %-8s | %11s | %11s | %s\n",
                "Date & Time", "Ref ID", "Type", "Acct", "Amount", "Balance", "Status"));
        sb.append("------------------------------------------------------------------------\n");

        if (list.isEmpty()) {
            sb.append(String.format("               [ No transaction activity for %s ]\n", acct.getDisplayName()));
        } else {
            for (TransactionRecord r : list) {
                sb.append(r.toFormattedLedgerLine()).append("\n");
            }
        }
        sb.append("========================================================================\n");
        return sb.toString();
    }

    /**
     * Simulates thermal receipt printing via an authentic receipt dialog modal.
     */
    private void printThermalReceipt() {
        Customer cust = bankingService.getCurrentCustomer();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String nowStr = LocalDateTime.now().format(dtf);

        BigDecimal chkBal = bankingService.getBalance(AccountType.CHECKING);
        BigDecimal savBal = bankingService.getBalance(AccountType.SAVINGS);

        StringBuilder receipt = new StringBuilder();
        receipt.append("****************************************\n");
        receipt.append("           METRO APEX BANK              \n");
        receipt.append("         ATM TERMINAL #7809             \n");
        receipt.append("      WALL STREET FINANCIAL CENTER      \n");
        receipt.append("****************************************\n");
        receipt.append(String.format("DATE/TIME: %s\n", nowStr));
        receipt.append(String.format("CARD:      %s\n", cust.getMaskedCardNumber()));
        receipt.append(String.format("CLIENT:    %s\n", cust.getFullName()));
        receipt.append("----------------------------------------\n");
        receipt.append("RECENT TRANSACTION ACTIVITY:\n");

        List<TransactionRecord> mini = bankingService.getMiniStatement();
        for (TransactionRecord r : mini) {
            receipt.append(r.toMiniStatementLine()).append("\n");
        }

        receipt.append("----------------------------------------\n");
        receipt.append(String.format("CHECKING BALANCE:      $%,.2f\n", chkBal));
        receipt.append(String.format("SAVINGS BALANCE:       $%,.2f\n", savBal));
        receipt.append(String.format("TOTAL ASSET VALUE:     $%,.2f\n", chkBal.add(savBal)));
        receipt.append("****************************************\n");
        receipt.append("    THANK YOU FOR BANKING WITH US!      \n");
        receipt.append("      PLEASE RETAIN FOR YOUR RECORDS    \n");
        receipt.append("****************************************\n");

        JTextArea receiptArea = new JTextArea(receipt.toString());
        receiptArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        receiptArea.setEditable(false);
        receiptArea.setBackground(new Color(0xFF, 0xFE, 0xF0)); // Thermal paper off-white
        receiptArea.setForeground(new Color(0x11, 0x18, 0x27));
        receiptArea.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JScrollPane scroll = new JScrollPane(receiptArea);
        scroll.setPreferredSize(new Dimension(380, 420));

        JOptionPane.showMessageDialog(this,
                scroll,
                "🖨 ATM Thermal Receipt Dispensed",
                JOptionPane.PLAIN_MESSAGE);
    }

    public void onPanelShown() {
        refreshLedgerDisplay();
    }
}
