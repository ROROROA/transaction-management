package com.hsbc.transactionmanagement.model;

import com.hsbc.transactionmanagement.exceptions.TransactionValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class  TransactionTest {

    @Test
    @DisplayName("Should create valid transaction")
    void shouldCreateValidTransaction() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        // When
        Transaction transaction = Transaction.builder()
                .transactionId("1")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .timestamp(now)
                .description("Test transaction")
                .status("PENDING")
                .senderAccountId(1001L)
                .receiverAccountId(2001L)
                .referenceNumber("REF123456")
                .build();
        
        // Then
        assertNotNull(transaction);
        assertEquals("1", transaction.getTransactionId());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals("USD", transaction.getCurrency());
        assertEquals(now, transaction.getTimestamp());
        assertEquals("Test transaction", transaction.getDescription());
        assertEquals("PENDING", transaction.getStatus());
        assertEquals(1001L, transaction.getSenderAccountId());
        assertEquals(2001L, transaction.getReceiverAccountId());
        assertEquals("REF123456", transaction.getReferenceNumber());
    }

    @Test
    @DisplayName("Should validate business rules successfully")
    void shouldValidateBusinessRulesSuccessfully() {
        // Given
        Transaction transaction = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .timestamp(LocalDateTime.now())
                .description("Test transaction")
                .status("PENDING")
                .senderAccountId(1001L)
                .receiverAccountId(2001L)
                .referenceNumber("REF123456")
                .build();
        
        // When & Then
        assertDoesNotThrow(() -> transaction.validateBusinessRules());
    }

    @Test
    @DisplayName("Should fail validation when amount is negative")
    void shouldFailValidationWhenAmountIsNegative() {
        // Given
        Transaction transaction = Transaction.builder()
                .amount(new BigDecimal("-100.00"))
                .currency("USD")
                .timestamp(LocalDateTime.now())
                .description("Test transaction")
                .status("PENDING")
                .senderAccountId(1001L)
                .receiverAccountId(2001L)
                .referenceNumber("REF123456")
                .build();
        
        // When & Then
        TransactionValidationException exception = assertThrows(TransactionValidationException.class, 
                () -> transaction.validateBusinessRules());
        assertTrue(exception.getMessage().contains("Amount must be positive"));
        assertEquals("INVALID_AMOUNT", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should fail validation when sender and receiver are the same")
    void shouldFailValidationWhenSenderAndReceiverAreSame() {
        // Given
        Transaction transaction = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .timestamp(LocalDateTime.now())
                .description("Test transaction")
                .status("PENDING")
                .senderAccountId(1001L)
                .receiverAccountId(1001L)
                .referenceNumber("REF123456")
                .build();
        
        // When & Then
        TransactionValidationException exception = assertThrows(TransactionValidationException.class, 
                () -> transaction.validateBusinessRules());
        assertTrue(exception.getMessage().contains("Sender and receiver accounts cannot be the same"));
        assertEquals("INVALID_ACCOUNT_PAIR", exception.getErrorCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "COMPLETED", "CANCELLED", "FAILED"})
    @DisplayName("Should accept valid status values")
    void shouldAcceptValidStatusValues(String status) {
        // Given
        Transaction transaction = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .timestamp(LocalDateTime.now())
                .description("Test transaction")
                .status(status)
                .senderAccountId(1001L)
                .receiverAccountId(2001L)
                .referenceNumber("REF123456")
                .build();
        
        // When & Then
        assertDoesNotThrow(() -> transaction.validateBusinessRules());
    }

    @Test
    @DisplayName("Should fail validation with invalid status")
    void shouldFailValidationWithInvalidStatus() {
        // Given
        Transaction transaction = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .timestamp(LocalDateTime.now())
                .description("Test transaction")
                .status("INVALID_STATUS")
                .senderAccountId(1001L)
                .receiverAccountId(2001L)
                .referenceNumber("REF123456")
                .build();
        
        // When & Then
        TransactionValidationException exception = assertThrows(TransactionValidationException.class, 
                () -> transaction.validateBusinessRules());
        assertTrue(exception.getMessage().contains("Invalid status"));
        assertEquals("INVALID_STATUS", exception.getErrorCode());
    }

    @Test
    @DisplayName("Should convert TransactionCreateRequest to Transaction entity")
    void shouldConvertTransactionCreateRequestToEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        TransactionCreateRequest request = new TransactionCreateRequest(
                "1",
                new BigDecimal("100.00"),
                "USD",
                now,
                "Test transaction",
                "PENDING",
                1001L,
                2001L,
                "REF123456"
        );
        
        // When
        Transaction transaction = request.toEntity();
        
        // Then
        assertNotNull(transaction);
        assertNotNull(transaction.getTransactionId());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals("USD", transaction.getCurrency());
        assertEquals(now, transaction.getTimestamp());
        assertEquals("Test transaction", transaction.getDescription());
        assertEquals("PENDING", transaction.getStatus());
        assertEquals(1001L, transaction.getSenderAccountId());
        assertEquals(2001L, transaction.getReceiverAccountId());
        assertEquals("REF123456", transaction.getReferenceNumber());
    }
}