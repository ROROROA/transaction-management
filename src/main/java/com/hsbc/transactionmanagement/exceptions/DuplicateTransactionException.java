package com.hsbc.transactionmanagement.exceptions;

public class DuplicateTransactionException extends RuntimeException {
    private final String transactionId;

    public DuplicateTransactionException(String transactionId) {
        super("Duplicate transaction detected: " + transactionId);
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }
}