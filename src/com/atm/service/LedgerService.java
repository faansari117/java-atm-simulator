package com.atm.service;

import com.atm.model.AccountType;
import com.atm.model.TransactionRecord;
import com.atm.model.TransactionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sequential real-time collection capturing all balance adjustments and audit events.
 * Provides mini-statement formatting (last 5 records) and full ledger logs.
 */
public class LedgerService {
    private final List<TransactionRecord> records = Collections.synchronizedList(new ArrayList<>());

    public void log(TransactionRecord record) {
        if (record != null) {
            records.add(record);
        }
    }

    /**
     * Retrieves up to the last 5 transaction records for the mini-statement.
     */
    public List<TransactionRecord> getMiniStatement() {
        synchronized (records) {
            int size = records.size();
            if (size <= 5) {
                return new ArrayList<>(records);
            }
            return new ArrayList<>(records.subList(size - 5, size));
        }
    }

    /**
     * Retrieves all recorded transactions in chronological order.
     */
    public List<TransactionRecord> getAllRecords() {
        synchronized (records) {
            return new ArrayList<>(records);
        }
    }

    /**
     * Filters ledger entries by specific account allocation.
     */
    public List<TransactionRecord> getRecordsByAccount(AccountType accountType) {
        synchronized (records) {
            return records.stream()
                    .filter(r -> r.getPrimaryAccount() == accountType || r.getTargetAccount() == accountType)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Computes the cumulative total deposited across all recorded events.
     */
    public BigDecimal calculateTotalDeposits() {
        synchronized (records) {
            return records.stream()
                    .filter(r -> r.isSuccess() && (r.getType() == TransactionType.DEPOSIT || r.getType() == TransactionType.TRANSFER_IN))
                    .map(TransactionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /**
     * Computes the cumulative total withdrawn across all recorded events.
     */
    public BigDecimal calculateTotalWithdrawals() {
        synchronized (records) {
            return records.stream()
                    .filter(r -> r.isSuccess() && (r.getType() == TransactionType.WITHDRAWAL || r.getType() == TransactionType.TRANSFER_OUT))
                    .map(TransactionRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    /**
     * Formats the mini-statement into a display-ready thermal receipt view.
     */
    public String formatMiniStatementText(String customerName, String cardNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("                     METRO APEX BANK - MINI STATEMENT                   \n");
        sb.append("                       ATM TERMINAL ID: APX-7809-B2                     \n");
        sb.append("========================================================================\n");
        sb.append(String.format("Cardholder : %s\n", customerName));
        sb.append(String.format("Card Number: %s\n", cardNumber));
        sb.append("------------------------------------------------------------------------\n");
        sb.append(String.format("%-19s | %-12s | %-14s | %-8s | %11s | %11s | %s\n",
                "Date & Time", "Ref ID", "Type", "Acct", "Amount", "Balance", "Status"));
        sb.append("------------------------------------------------------------------------\n");

        List<TransactionRecord> mini = getMiniStatement();
        if (mini.isEmpty()) {
            sb.append("                       [ No recent transactions recorded ]              \n");
        } else {
            for (TransactionRecord record : mini) {
                sb.append(record.toFormattedLedgerLine()).append("\n");
            }
        }
        sb.append("========================================================================\n");
        sb.append("Note: Displaying last 5 real-time transactional records.\n");
        return sb.toString();
    }

    /**
     * Formats the entire ledger into a complete audit document.
     */
    public String formatFullLedgerText(String customerName, String cardNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("                 METRO APEX BANK - COMPLETE ACCOUNT AUDIT LEDGER        \n");
        sb.append("                       ATM TERMINAL ID: APX-7809-B2                     \n");
        sb.append("========================================================================\n");
        sb.append(String.format("Cardholder : %s\n", customerName));
        sb.append(String.format("Card Number: %s\n", cardNumber));
        sb.append("------------------------------------------------------------------------\n");
        sb.append(String.format("%-19s | %-12s | %-14s | %-8s | %11s | %11s | %s\n",
                "Date & Time", "Ref ID", "Type", "Acct", "Amount", "Balance", "Status"));
        sb.append("------------------------------------------------------------------------\n");

        List<TransactionRecord> all = getAllRecords();
        if (all.isEmpty()) {
            sb.append("                       [ No audit logs available ]                      \n");
        } else {
            for (TransactionRecord record : all) {
                sb.append(record.toFormattedLedgerLine()).append("\n");
            }
        }
        sb.append("========================================================================\n");
        sb.append(String.format("Total Deposits Processed : $%,.2f\n", calculateTotalDeposits()));
        sb.append(String.format("Total Withdrawals Dispensed: $%,.2f\n", calculateTotalWithdrawals()));
        sb.append(String.format("Total Audit Entries Logged : %d\n", all.size()));
        sb.append("========================================================================\n");
        return sb.toString();
    }
}
