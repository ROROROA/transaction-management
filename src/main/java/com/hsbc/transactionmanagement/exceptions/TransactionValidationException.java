package com.hsbc.transactionmanagement.exceptions;

public class TransactionValidationException extends RuntimeException {
    private final String errorCode;

    public TransactionValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}