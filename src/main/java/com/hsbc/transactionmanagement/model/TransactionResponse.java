package com.hsbc.transactionmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record TransactionResponse(
        String transactionId,
        BigDecimal amount,
        String currency,
        LocalDateTime timestamp,
        String description,
        String status,
        Long senderAccountId,
        Long receiverAccountId,
        String referenceNumber
) {

    public static TransactionResponse fromEntity(Transaction transaction) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTimestamp(),
                transaction.getDescription(),
                transaction.getStatus(),
                transaction.getSenderAccountId(),
                transaction.getReceiverAccountId(),
                transaction.getReferenceNumber()
        );
    }

}