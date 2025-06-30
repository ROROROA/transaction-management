package com.hsbc.transactionmanagement.service;

import com.hsbc.transactionmanagement.exceptions.DuplicateTransactionException;
import com.hsbc.transactionmanagement.exceptions.TransactionNotFoundException;
import com.hsbc.transactionmanagement.model.Transaction;
import com.hsbc.transactionmanagement.model.TransactionCreateRequest;
import com.hsbc.transactionmanagement.model.TransactionUpdateRequest;
import com.hsbc.transactionmanagement.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction sampleTransaction;
    private TransactionCreateRequest createRequest;
    private TransactionUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
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

        createRequest = new TransactionCreateRequest(
                "1",
                new BigDecimal("100.00"),
                "USD",
                LocalDateTime.now(),
                "Test transaction",
                "PENDING",
                1001L,
                2001L,
                "REF123456"
        );

        updateRequest = new TransactionUpdateRequest("COMPLETED");
    }

    @Test
    @DisplayName("Should create a new transaction successfully")
    void shouldCreateTransaction() {
        // Given
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);
        when(transactionRepository.existsById(any())).thenReturn(false);

        // When
        Transaction result = transactionService.createTransaction(createRequest);

        // Then
        assertNotNull(result);
        assertEquals("1", result.getTransactionId());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals("USD", result.getCurrency());
        assertEquals("PENDING", result.getStatus());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception when creating transaction with existing ID")
    void shouldThrowExceptionWhenCreatingDuplicateTransaction() {
        // Given
        Transaction transactionWithId = createRequest.toEntity();
        transactionWithId.setTransactionId("1");
        when(transactionRepository.existsById("1")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateTransactionException.class, () -> {
            transactionService.createTransaction(createRequest);
        });
    }

    @Test
    @DisplayName("Should get transaction by ID")
    void shouldGetTransactionById() {
        // Given
        when(transactionRepository.findById("1")).thenReturn(Optional.of(sampleTransaction));

        // When
        Transaction result = transactionService.getTransactionById("1");

        // Then
        assertNotNull(result);
        assertEquals("1", result.getTransactionId());
        verify(transactionRepository).findById("1");
    }

    @Test
    @DisplayName("Should throw exception when transaction not found")
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Given
        when(transactionRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.getTransactionById("999");
        });
        verify(transactionRepository).findById("999");
    }

    @Test
    @DisplayName("Should update transaction status")
    void shouldUpdateTransactionStatus() {
        // Given
        Transaction updatedTransaction = Transaction.builder()
                .transactionId("1")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .timestamp(sampleTransaction.getTimestamp())
                .description("Test transaction")
                .status("COMPLETED")
                .senderAccountId(1001L)
                .receiverAccountId(2001L)
                .referenceNumber("REF123456")
                .build();

        when(transactionRepository.findById("1")).thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransaction);

        // When
        Transaction result = transactionService.updateTransaction("1", updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        verify(transactionRepository).findById("1");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should delete transaction by ID")
    void shouldDeleteTransactionById() {
        // Given
        when(transactionRepository.existsById("1")).thenReturn(true);
        doNothing().when(transactionRepository).deleteById("1");

        // When
        transactionService.deleteTransactionById("1");

        // Then
        verify(transactionRepository).existsById("1");
        verify(transactionRepository).deleteById("1");
    }

    @Test
    @DisplayName("Should get all transactions")
    void shouldGetAllTransactions() {
        // Given
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

        List<Transaction> transactions = Arrays.asList(sampleTransaction, transaction2);
        when(transactionRepository.findAll()).thenReturn(transactions);

        // When
        List<Transaction> result = transactionService.getAllTransactions();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getTransactionId());
        assertEquals("2", result.get(1).getTransactionId());
        verify(transactionRepository).findAll();
    }

    @Test
    @DisplayName("Should get paged transactions")
    void shouldGetPagedTransactions() {
        // Given
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

        List<Transaction> transactions = Arrays.asList(sampleTransaction, transaction2);
        Page<Transaction> pagedTransactions = new PageImpl<>(
                transactions, PageRequest.of(0, 10), 2);
        
        Pageable pageable = PageRequest.of(0, 10);
        when(transactionRepository.findAll(pageable)).thenReturn(pagedTransactions);

        // When
        Page<Transaction> result = transactionService.getAllTransactionsPaged(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());
        verify(transactionRepository).findAll(pageable);
    }
}