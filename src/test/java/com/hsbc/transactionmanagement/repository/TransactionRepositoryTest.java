package com.hsbc.transactionmanagement.repository;

import com.hsbc.transactionmanagement.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionRepositoryTest {

    private InMemoryTransactionRepository repository;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
        
        // Setup test data
        sampleTransaction = Transaction.builder()
                .transactionId("1")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .timestamp(LocalDateTime.now())
                .description("Test transaction")
                .status("PENDING")
                .senderAccountId(1001L)
                .receiverAccountId(2001L)
                .referenceNumber("REF123456")
                .build();
    }

    @Test
    @DisplayName("Should save transaction and generate ID")
    void shouldSaveTransactionAndGenerateId() {
        // When
        Transaction savedTransaction = repository.save(sampleTransaction);
        
        // Then
        assertNotNull(savedTransaction);
        assertNotNull(savedTransaction.getTransactionId());
        assertEquals("1", savedTransaction.getTransactionId());
    }

    @Test
    @DisplayName("Should save transaction with existing ID")
    void shouldSaveTransactionWithExistingId() {
        // Given
        sampleTransaction.setTransactionId("custom-id");
        
        // When
        Transaction savedTransaction = repository.save(sampleTransaction);
        
        // Then
        assertNotNull(savedTransaction);
        assertEquals("custom-id", savedTransaction.getTransactionId());
    }

    @Test
    @DisplayName("Should find transaction by ID")
    void shouldFindTransactionById() {
        // Given
        Transaction savedTransaction = repository.save(sampleTransaction);
        
        // When
        Optional<Transaction> foundTransaction = repository.findById(savedTransaction.getTransactionId());
        
        // Then
        assertTrue(foundTransaction.isPresent());
        assertEquals(savedTransaction.getTransactionId(), foundTransaction.get().getTransactionId());
    }

    @Test
    @DisplayName("Should return empty when transaction not found")
    void shouldReturnEmptyWhenTransactionNotFound() {
        // When
        Optional<Transaction> foundTransaction = repository.findById("non-existent-id");
        
        // Then
        assertFalse(foundTransaction.isPresent());
    }

    @Test
    @DisplayName("Should delete transaction by ID")
    void shouldDeleteTransactionById() {
        // Given
        Transaction savedTransaction = repository.save(sampleTransaction);
        
        // When
        repository.deleteById(savedTransaction.getTransactionId());
        Optional<Transaction> foundTransaction = repository.findById(savedTransaction.getTransactionId());
        
        // Then
        assertFalse(foundTransaction.isPresent());
    }

    @Test
    @DisplayName("Should find all transactions")
    void shouldFindAllTransactions() {
        // Given
        repository.save(sampleTransaction);
        
        Transaction transaction2 = Transaction.builder()
                .transactionId("2")
                .amount(new BigDecimal("200.00"))
                .currency("EUR")
                .timestamp(LocalDateTime.now())
                .description("Another transaction")
                .status("COMPLETED")
                .senderAccountId(1002L)
                .receiverAccountId(2002L)
                .referenceNumber("REF789012")
                .build();
        repository.save(transaction2);
        
        // When
        List<Transaction> transactions = repository.findAll();
        
        // Then
        assertNotNull(transactions);
        assertEquals(2, transactions.size());
    }

    @Test
    @DisplayName("Should check if transaction exists by ID")
    void shouldCheckIfTransactionExistsById() {
        // Given
        Transaction savedTransaction = repository.save(sampleTransaction);
        
        // When
        boolean exists = repository.existsById(savedTransaction.getTransactionId());
        boolean nonExists = repository.existsById("non-existent-id");
        
        // Then
        assertTrue(exists);
        assertFalse(nonExists);
    }

    @Test
    @DisplayName("Should find all transactions with pagination")
    void shouldFindAllTransactionsWithPagination() {
        // Given
        for (int i = 0; i < 20; i++) {
            Transaction transaction = Transaction.builder()
                    .transactionId(String.valueOf(i))
                    .amount(new BigDecimal("100.00"))
                    .currency("USD")
                    .timestamp(LocalDateTime.now())
                    .description("Transaction " + i)
                    .status("PENDING")
                    .senderAccountId(1001L)
                    .receiverAccountId(2001L)
                    .referenceNumber("REF" + i)
                    .build();
            repository.save(transaction);
        }
        
        // When
        Pageable pageable = PageRequest.of(0, 5);
        Page<Transaction> page = repository.findAll(pageable);
        
        // Then
        assertNotNull(page);
        assertEquals(5, page.getContent().size());
        assertEquals(20, page.getTotalElements());
        assertEquals(4, page.getTotalPages());
        
        // Test second page
        pageable = PageRequest.of(1, 5);
        page = repository.findAll(pageable);
        assertEquals(5, page.getContent().size());
        assertEquals(1, page.getNumber());
    }
}