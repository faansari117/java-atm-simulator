package com.atm.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the authenticated banking client, housing security credentials,
 * failure counters, and linked financial accounts.
 */
public class Customer {
    public static final int MAX_PIN_ATTEMPTS = 3;

    private final String customerId;
    private final String fullName;
    private final String cardNumber;
    private String pin;
    private final Map<AccountType, Account> accounts;
    private int failedPinAttempts;
    private boolean locked;

    public Customer(String customerId, String fullName, String cardNumber, String pin,
                    BigDecimal initialChecking, BigDecimal initialSavings) {
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.fullName = Objects.requireNonNull(fullName, "fullName must not be null");
        this.cardNumber = Objects.requireNonNull(cardNumber, "cardNumber must not be null");
        this.pin = Objects.requireNonNull(pin, "pin must not be null");
        this.failedPinAttempts = 0;
        this.locked = false;

        this.accounts = new EnumMap<>(AccountType.class);
        this.accounts.put(AccountType.CHECKING,
                new Account(AccountType.CHECKING, "CHK-" + customerId.substring(Math.max(0, customerId.length() - 4)) + "-01", initialChecking));
        this.accounts.put(AccountType.SAVINGS,
                new Account(AccountType.SAVINGS, "SAV-" + customerId.substring(Math.max(0, customerId.length() - 4)) + "-02", initialSavings));
    }

    public synchronized boolean verifyPin(String enteredPin) {
        if (locked) {
            return false;
        }
        if (this.pin.equals(enteredPin)) {
            resetFailedAttempts();
            return true;
        } else {
            failedPinAttempts++;
            if (failedPinAttempts >= MAX_PIN_ATTEMPTS) {
                locked = true;
            }
            return false;
        }
    }

    public synchronized void resetFailedAttempts() {
        this.failedPinAttempts = 0;
    }

    public synchronized void setPin(String newPin) {
        this.pin = Objects.requireNonNull(newPin, "New PIN cannot be null");
    }

    public synchronized void unlock() {
        this.locked = false;
        this.failedPinAttempts = 0;
    }

    public synchronized void lock() {
        this.locked = true;
    }

    public synchronized int getFailedPinAttempts() {
        return failedPinAttempts;
    }

    public synchronized int getRemainingAttempts() {
        return Math.max(0, MAX_PIN_ATTEMPTS - failedPinAttempts);
    }

    public synchronized boolean isLocked() {
        return locked;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getMaskedCardNumber() {
        if (cardNumber.length() < 8) return cardNumber;
        String clean = cardNumber.replaceAll("\\s+", "");
        String lastFour = clean.substring(clean.length() - 4);
        return "4532 •••• •••• " + lastFour;
    }

    public Account getAccount(AccountType type) {
        return accounts.get(type);
    }

    public Map<AccountType, Account> getAccounts() {
        return Collections.unmodifiableMap(accounts);
    }

    public String getPin() {
        return pin;
    }
}
