package com.atm.exception;

/**
 * Base checked exception class for all ATM domain operations and business rule violations.
 */
public class ATMException extends Exception {
    public ATMException(String message) {
        super(message);
    }

    public ATMException(String message, Throwable cause) {
        super(message, cause);
    }
}
