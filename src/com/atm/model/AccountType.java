package com.atm.model;

/**
 * Enumeration representing distinct banking account allocation types.
 */
public enum AccountType {
    CHECKING("Checking Account", "CHK"),
    SAVINGS("Savings Account", "SAV");

    private final String displayName;
    private final String codePrefix;

    AccountType(String displayName, String codePrefix) {
        this.displayName = displayName;
        this.codePrefix = codePrefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCodePrefix() {
        return codePrefix;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
