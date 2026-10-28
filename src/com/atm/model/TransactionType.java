package com.atm.model;

/**
 * Enumeration representing valid financial and administrative operations.
 */
public enum TransactionType {
    DEPOSIT("Cash Deposit", "+", "Funds credited to account"),
    WITHDRAWAL("Cash Withdrawal", "-", "Cash dispensed at ATM"),
    TRANSFER_OUT("Transfer Out", "-", "Funds transferred to another account"),
    TRANSFER_IN("Transfer In", "+", "Funds received from another account"),
    BALANCE_INQUIRY("Balance Inquiry", "ℹ", "Account balance check"),
    PIN_CHANGE("PIN Update", "🔒", "Authentication PIN successfully updated");

    private final String label;
    private final String symbol;
    private final String description;

    TransactionType(String label, String symbol, String description) {
        this.label = label;
        this.symbol = symbol;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return label;
    }
}
