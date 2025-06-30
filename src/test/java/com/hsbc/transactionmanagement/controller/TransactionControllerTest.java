package com.hsbc.transactionmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.transactionmanagement.exceptions.TransactionNotFoundException;
import com.hsbc.transactionmanagement.model.Transaction;
import com.hsbc.transactionmanagement.model.TransactionCreateRequest;
import com.hsbc.transactionmanagement.model.TransactionUpdateRequest;
import com.hsbc.transactionmanagement.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Transaction sampleTransaction;
    private TransactionCreateRequest createRequest;
    private TransactionUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // 设置测试数据
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

        updateRequest = new TransactionUpdateRequest("CANCELLED");
    }

    @Test
    @DisplayName("Should create a new transaction")
    void shouldCreateTransaction() throws Exception {
        when(transactionService.createTransaction(any(TransactionCreateRequest.class)))
                .thenReturn(sampleTransaction);

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId", is("1")))
                .andExpect(jsonPath("$.amount", is(100.00)))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(transactionService).createTransaction(any(TransactionCreateRequest.class));
    }

    @Test
    @DisplayName("Should get transaction by ID")
    void shouldGetTransactionById() throws Exception {
        when(transactionService.getTransactionById("1")).thenReturn(sampleTransaction);

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId", is("1")))
                .andExpect(jsonPath("$.amount", is(100.00)))
                .andExpect(jsonPath("$.currency", is("USD")));

        verify(transactionService).getTransactionById("1");
    }

    @Test
    @DisplayName("Should return 404 when transaction not found")
    void shouldReturn404WhenTransactionNotFound() throws Exception {
        when(transactionService.getTransactionById("999"))
                .thenThrow(new TransactionNotFoundException("999"));

        mockMvc.perform(get("/api/transactions/999"))
                .andExpect(status().isNotFound());

        verify(transactionService).getTransactionById("999");
    }

    @Test
    @DisplayName("Should update transaction status")
    void shouldUpdateTransactionStatus() throws Exception {
        Transaction updatedTransaction = Transaction.builder()
                .transactionId("1")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .timestamp(LocalDateTime.now())
                .description("Test transaction")
                .status("CANCELLED")
                .senderAccountId(1001L)
                .receiverAccountId(2001L)
                .referenceNumber("REF123456")
                .build();

        when(transactionService.updateTransaction(eq("1"), any(TransactionUpdateRequest.class)))
                .thenReturn(updatedTransaction);

        mockMvc.perform(put("/api/transactions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId", is("1")))
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        verify(transactionService).updateTransaction(eq("1"), any(TransactionUpdateRequest.class));
    }

    @Test
    @DisplayName("Should delete transaction")
    void shouldDeleteTransaction() throws Exception {
        doNothing().when(transactionService).deleteTransactionById("1");

        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isNoContent());

        verify(transactionService).deleteTransactionById("1");
    }

    @Test
    @DisplayName("Should get all transactions")
    void shouldGetAllTransactions() throws Exception {
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
        when(transactionService.getAllTransactions()).thenReturn(transactions);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].transactionId", is("1")))
                .andExpect(jsonPath("$[1].transactionId", is("2")));

        verify(transactionService).getAllTransactions();
    }

    @Test
    @DisplayName("Should get paged transactions")
    void shouldGetPagedTransactions() throws Exception {
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
        PageImpl<Transaction> pagedTransactions = new PageImpl<>(
                transactions, PageRequest.of(0, 10), 2);

        when(transactionService.getAllTransactionsPaged(any())).thenReturn(pagedTransactions);

        mockMvc.perform(get("/api/transactions/paged")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.content[0].transactionId", is("1")))
                .andExpect(jsonPath("$.content[1].transactionId", is("2")));

        verify(transactionService).getAllTransactionsPaged(any());
    }

    @Test
    @DisplayName("Should validate transaction create request")
    void shouldValidateTransactionCreateRequest() throws Exception {
        // 创建一个无效的请求（金额为负）
        TransactionCreateRequest invalidRequest = new TransactionCreateRequest(
                "1",
                new BigDecimal("-100.00"),
                "USD",
                LocalDateTime.now(),
                "Test transaction",
                "PENDING",
                1001L,
                2001L,
                "REF123456"
        );

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).createTransaction(any());
    }
}