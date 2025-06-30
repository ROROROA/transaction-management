package com.hsbc.transactionmanagement.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;


// Most business scenarios only allowed to update part of fields for specific purpose, like cancel a transaction
public record TransactionUpdateRequest(

        @Schema(
                description = "Transaction status",
                allowableValues = {"PENDING", "COMPLETED", "FAILED", "CANCELLED"},
                example = "COMPLETED"
        )
        @NotBlank(message = "Status cannot be blank")
        @Pattern(regexp = "PENDING|COMPLETED|FAILED|CANCELLED",
                message = "Invalid transaction status")
        String status


) {}