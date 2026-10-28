package com.atm.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Encapsulates an individual customer account (e.g. Checking or Savings)
 * tracking available balance and daily withdrawal metrics.
 */
public class Account {
    public static final BigDecimal DEFAULT_DAILY_LIMIT = new BigDecimal("1000.00");

    private final AccountType accountType;
    private final String accountNumber;
    private BigDecimal balance;
    private BigDecimal dailyWithdrawn;
    private BigDecimal dailyLimit;
    private LocalDate lastResetDate;

    public Account(AccountType accountType, String accountNumber, BigDecimal initialBalance) {
        this.accountType = Objects.requireNonNull(accountType, "AccountType must not be null");
        this.accountNumber = Objects.requireNonNull(accountNumber, "AccountNumber must not be null");
        this.balance = initialBalance.setScale(2, RoundingMode.HALF_UP);
        this.dailyWithdrawn = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.dailyLimit = DEFAULT_DAILY_LIMIT.setScale(2, RoundingMode.HALF_UP);
        this.lastResetDate = LocalDate.now();
    }

    /**
     * Checks if a new calendar day has started. If so, resets the accumulated
     * daily withdrawal total back to zero.
     */
    public synchronized void checkAndResetDailyLimit() {
        LocalDate today = LocalDate.now();
        if (today.isAfter(lastResetDate)) {
            dailyWithdrawn = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            lastResetDate = today;
        }
    }

    /**
     * Calculates the remaining withdrawal headroom for today.
     */
    public synchronized BigDecimal getRemainingDailyLimit() {
        checkAndResetDailyLimit();
        BigDecimal remaining = dailyLimit.subtract(dailyWithdrawn);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    public synchronized BigDecimal getBalance() {
        return balance;
    }

    public synchronized void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly positive");
        }
        balance = balance.add(amount.setScale(2, RoundingMode.HALF_UP));
    }

    public synchronized void debit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be strictly positive");
        }
        balance = balance.subtract(amount.setScale(2, RoundingMode.HALF_UP));
    }

    public synchronized void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be strictly positive");
        }
        checkAndResetDailyLimit();
        BigDecimal formattedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        balance = balance.subtract(formattedAmount);
        dailyWithdrawn = dailyWithdrawn.add(formattedAmount);
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public synchronized BigDecimal getDailyWithdrawn() {
        checkAndResetDailyLimit();
        return dailyWithdrawn;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public synchronized void setDailyLimit(BigDecimal newLimit) {
        this.dailyLimit = newLimit.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Balance: $%,.2f", accountType.getDisplayName(), accountNumber, balance);
    }
}
