package com.atm.exception;

/**
 * Checked exception thrown when inputs violate fundamental transactional rules,
 * such as zero/negative amounts, transferring to identical accounts, or invalid PIN formatting.
 */
public class InvalidTransactionException extends ATMException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
