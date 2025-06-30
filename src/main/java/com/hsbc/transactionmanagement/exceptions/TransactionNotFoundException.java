package com.hsbc.transactionmanagement.exceptions;

public class TransactionNotFoundException extends RuntimeException {
    private final String transactionId;

    public TransactionNotFoundException(String transactionId) {
        super("Transaction not found with ID: " + transactionId);
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }
}