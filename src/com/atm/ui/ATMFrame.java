package com.atm.ui;

import com.atm.model.Customer;
import com.atm.service.BankingService;
import com.atm.ui.component.HeaderPanel;
import com.atm.ui.panels.AuthPanel;
import com.atm.ui.panels.DashboardPanel;
import com.atm.ui.panels.LedgerPanel;
import com.atm.ui.panels.TransactionPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame managing the dynamic CardLayout system,
 * navigating fluidly between Authentication, Dashboard, Transaction, and Ledger screens.
 */
public class ATMFrame extends JFrame {
    public static final String SCREEN_AUTH = "AUTH_SCREEN";
    public static final String SCREEN_DASHBOARD = "DASHBOARD_SCREEN";
    public static final String SCREEN_TRANSACTION = "TRANSACTION_SCREEN";
    public static final String SCREEN_LEDGER = "LEDGER_SCREEN";

    private final BankingService bankingService;

    private HeaderPanel headerPanel;
    private JPanel cardContainer;
    private CardLayout cardLayout;

    private AuthPanel authPanel;
    private DashboardPanel dashboardPanel;
    private TransactionPanel transactionPanel;
    private LedgerPanel ledgerPanel;

    public ATMFrame(BankingService bankingService) {
        this.bankingService = bankingService;

        initFrame();
        buildUI();
        navigateToAuth();
    }

    private void initFrame() {
        setTitle("METRO APEX BANK - Next-Gen Automated Teller Machine Terminal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 760);
        setMinimumSize(new Dimension(960, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // Header telemetry bar at North
        headerPanel = new HeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Dynamic Card Panel System at Center
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setOpaque(false);

        // Instantiate Screen 1, 2, 3, 4
        authPanel = new AuthPanel(this, bankingService);
        dashboardPanel = new DashboardPanel(this, bankingService);
        transactionPanel = new TransactionPanel(this, bankingService);
        ledgerPanel = new LedgerPanel(this, bankingService);

        cardContainer.add(authPanel, SCREEN_AUTH);
        cardContainer.add(dashboardPanel, SCREEN_DASHBOARD);
        cardContainer.add(transactionPanel, SCREEN_TRANSACTION);
        cardContainer.add(ledgerPanel, SCREEN_LEDGER);

        add(cardContainer, BorderLayout.CENTER);
    }

    public void navigateToAuth() {
        headerPanel.setSessionCustomer(null);
        cardLayout.show(cardContainer, SCREEN_AUTH);
        authPanel.onPanelShown();
    }

    public void onAuthenticationSuccess(Customer customer) {
        headerPanel.setSessionCustomer(customer);
        navigateToDashboard();
    }

    public void navigateToDashboard() {
        cardLayout.show(cardContainer, SCREEN_DASHBOARD);
        dashboardPanel.onPanelShown();
    }

    public void navigateToTransaction(TransactionPanel.Mode mode) {
        transactionPanel.setMode(mode);
        cardLayout.show(cardContainer, SCREEN_TRANSACTION);
        transactionPanel.onPanelShown();
    }

    public void navigateToLedger(boolean miniStatementOnly) {
        ledgerPanel.setMiniStatementMode(miniStatementOnly);
        cardLayout.show(cardContainer, SCREEN_LEDGER);
        ledgerPanel.onPanelShown();
    }

    public BankingService getBankingService() {
        return bankingService;
    }
}
