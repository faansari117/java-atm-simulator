package com.atm.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Immutable log record capturing an individual financial or state transaction
 * with audit trail timestamps, accounts involved, and post-transaction balances.
 */
public class TransactionRecord {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SHORT_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd HH:mm");

    private final String referenceId;
    private final LocalDateTime timestamp;
    private final TransactionType type;
    private final AccountType primaryAccount;
    private final AccountType targetAccount;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final String description;
    private final boolean success;

    public TransactionRecord(TransactionType type, AccountType primaryAccount, AccountType targetAccount,
                             BigDecimal amount, BigDecimal balanceAfter, String description, boolean success) {
        this.referenceId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.primaryAccount = primaryAccount;
        this.targetAccount = targetAccount;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.balanceAfter = balanceAfter != null ? balanceAfter : BigDecimal.ZERO;
        this.description = description != null ? description : type.getDescription();
        this.success = success;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DATE_FORMATTER);
    }

    public String getShortTimestamp() {
        return timestamp.format(SHORT_TIME_FORMATTER);
    }

    public TransactionType getType() {
        return type;
    }

    public AccountType getPrimaryAccount() {
        return primaryAccount;
    }

    public AccountType getTargetAccount() {
        return targetAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSuccess() {
        return success;
    }

    /**
     * Formats the record into a clean fixed-width tabular log line.
     */
    public String toFormattedLedgerLine() {
        String acctStr = primaryAccount != null ? primaryAccount.getCodePrefix() : "N/A";
        if (targetAccount != null) {
            acctStr += "->" + targetAccount.getCodePrefix();
        }

        String amtStr = amount.compareTo(BigDecimal.ZERO) > 0 ?
                String.format("%s$%,.2f", type.getSymbol(), amount) : "-";

        String balStr = primaryAccount != null ? String.format("$%,.2f", balanceAfter) : "-";

        return String.format("%-19s | %-12s | %-14s | %-8s | %11s | %11s | %s",
                getFormattedTimestamp(),
                referenceId,
                type.getLabel(),
                acctStr,
                amtStr,
                balStr,
                success ? "CONFIRMED" : "FAILED"
        );
    }

    /**
     * Formats the record for compact receipt / mini-statement printing.
     */
    public String toMiniStatementLine() {
        String amtStr = amount.compareTo(BigDecimal.ZERO) > 0 ?
                String.format("%s$%,.2f", type.getSymbol(), amount) : "";
        return String.format("[%s] %-13s %-4s %10s -> Bal: $%,.2f",
                getShortTimestamp(),
                type.getLabel(),
                primaryAccount != null ? primaryAccount.getCodePrefix() : "---",
                amtStr,
                balanceAfter
        );
    }

    @Override
    public String toString() {
        return toFormattedLedgerLine();
    }
}
