package com.hsbc.transactionmanagement.model;

import com.hsbc.transactionmanagement.exceptions.TransactionValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {


    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime timestamp;
    private String description;
    private String status;
    private Long senderAccountId;
    private Long receiverAccountId;
    private String referenceNumber;


    private static final Map<String, Set<String>> STATUS_TRANSITION_RULES = Map.of(

            "PENDING",   Set.of("CANCELLED","COMPLETED"),
            "COMPLETED", Set.of(),
            "FAILED",    Set.of("CANCELLED"),
            "CANCELLED", Set.of()
    );


    public void validateBusinessRules(){
        // 验证金额必须为正数
        if (this.getAmount() != null && this.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransactionValidationException(
                    "Amount must be positive",
                    "INVALID_AMOUNT"
            );
        }
        
        // 验证发送方和接收方账户不能相同
        if (this.getSenderAccountId() != null && this.getReceiverAccountId() != null 
                && this.getSenderAccountId().equals(this.getReceiverAccountId())) {
            throw new TransactionValidationException(
                    "Sender and receiver accounts cannot be the same",
                    "INVALID_ACCOUNT_PAIR"
            );
        }
        
        // 验证状态必须是有效的
        if (this.getStatus() != null && !Set.of("PENDING", "COMPLETED", "CANCELLED", "FAILED")
                .contains(this.getStatus())) {
            throw new TransactionValidationException(
                    "Invalid status: " + this.getStatus(),
                    "INVALID_STATUS"
            );
        }
    }

    public void updateStatus(@NotBlank @Pattern(regexp = "PENDING|COMPLETED|FAILED|CANCELLED") String newStatus) {


        String currentStatus = this.status != null ? this.status : "PENDING";


        Set<String> allowedStatuses = STATUS_TRANSITION_RULES.get(currentStatus);


        if (allowedStatuses == null) {
            throw new TransactionValidationException(
                    "Unknown current status: " + currentStatus,
                    "INVALID_CURRENT_STATUS"
            );
        }

        if (!allowedStatuses.contains(newStatus)) {
            throw new TransactionValidationException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus),
                    "INVALID_STATUS_TRANSITION"
            );
        }


        this.status = newStatus;
    }
}
