package com.hsbc.transactionmanagement.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCreateRequest(

        @NotNull(message = "Transaction ID cannot be null")
        String transactionId,

        @NotNull(message = "Amount cannot be null")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "Currency cannot be blank")
        @Size(min = 3, max = 3, message = "Currency must be 3-letter code")
        String currency,

        @NotNull(message = "Timestamp cannot be null")
        @PastOrPresent(message = "Timestamp cannot be in the future")
        LocalDateTime timestamp,

        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,

        @Schema(
                description = "Transaction status",
                allowableValues = {"PENDING", "COMPLETED", "FAILED", "CANCELLED"},
                example = "COMPLETED"
        )
        @NotBlank(message = "Status cannot be blank")
        @Pattern(regexp = "PENDING|COMPLETED|FAILED|CANCELLED",
                message = "Invalid transaction status")
        String status,

        @NotNull(message = "Sender account ID cannot be null")
        @Positive(message = "Sender account ID must be positive")
        Long senderAccountId,

        @NotNull(message = "Receiver account ID cannot be null")
        @Positive(message = "Receiver account ID must be positive")
        Long receiverAccountId,

        @Size(max = 64, message = "Reference number cannot exceed 64 characters")
        String referenceNumber
) {


    /**
     * Converts this request to a Transaction domain object
     */
    public Transaction toEntity() {
        return Transaction.builder()
                .transactionId(this.transactionId())
                .amount(this.amount())
                .currency(this.currency())
                .timestamp(this.timestamp())
                .description(this.description())
                .status(this.status())
                .senderAccountId(this.senderAccountId())
                .receiverAccountId(this.receiverAccountId())
                .referenceNumber(this.referenceNumber())
                .build();
    }


}