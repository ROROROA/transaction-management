package com.hsbc.transactionmanagement.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.transactionmanagement.model.Transaction;
import com.hsbc.transactionmanagement.model.TransactionCreateRequest;
import com.hsbc.transactionmanagement.model.TransactionUpdateRequest;
import com.hsbc.transactionmanagement.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    private TransactionCreateRequest createRequest;
    private TransactionUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Clear repository before each test
        transactionRepository.findAll().forEach(transaction -> 
                transactionRepository.deleteById(transaction.getTransactionId()));
        
        // Setup test data
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
    @DisplayName("Should create, retrieve, update and delete transaction - full lifecycle")
    void shouldPerformFullTransactionLifecycle() throws Exception {
        // 1. Create transaction
        String createResponse = mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId", notNullValue()))
                .andExpect(jsonPath("$.amount", is(100.00)))
                .andExpect(jsonPath("$.currency", is("USD")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andReturn().getResponse().getContentAsString();
        
        // Extract transaction ID from response
        String transactionId = objectMapper.readTree(createResponse).get("transactionId").asText();
        
        // 2. Get transaction by ID
        mockMvc.perform(get("/api/transactions/" + transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId", is(transactionId)))
                .andExpect(jsonPath("$.amount", is(100.00)))
                .andExpect(jsonPath("$.status", is("PENDING")));
        
        // 3. Update transaction
        mockMvc.perform(put("/api/transactions/" + transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId", is(transactionId)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
        
        // 4. Verify update
        mockMvc.perform(get("/api/transactions/" + transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
        
        // 5. Delete transaction
        mockMvc.perform(delete("/api/transactions/" + transactionId))
                .andExpect(status().isNoContent());
        
        // 6. Verify deletion
        mockMvc.perform(get("/api/transactions/" + transactionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get all transactions")
    void shouldGetAllTransactions() throws Exception {
        // Create multiple transactions
        for (int i = 0; i < 3; i++) {
            TransactionCreateRequest request = new TransactionCreateRequest(
                    String.valueOf(i),
                    new BigDecimal("100.00").add(new BigDecimal(i * 50)),
                    "USD",
                    LocalDateTime.now(),
                    "Transaction " + i,
                    "PENDING",
                    1001L,
                    2001L,
                    "REF" + i
            );
            
            mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
        
        // Get all transactions
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].amount", notNullValue()))
                .andExpect(jsonPath("$[1].amount", notNullValue()))
                .andExpect(jsonPath("$[2].amount", notNullValue()));
    }

    @Test
    @DisplayName("Should get paged transactions")
    void shouldGetPagedTransactions() throws Exception {
        // Create multiple transactions
        for (int i = 0; i < 5; i++) {
            TransactionCreateRequest request = new TransactionCreateRequest(
                    String.valueOf(i),
                    new BigDecimal("100.00").add(new BigDecimal(i * 50)),
                    "USD",
                    LocalDateTime.now(),
                    "Transaction " + i,
                    "PENDING",
                    1001L,
                    2001L,
                    "REF" + i
            );
            
            mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
        
        // Get first page with 2 items
        mockMvc.perform(get("/api/transactions/paged")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.number", is(0)));
        
        // Get second page with 2 items
        mockMvc.perform(get("/api/transactions/paged")
                .param("page", "1")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.number", is(1)));
    }

    @Test
    @DisplayName("Should validate transaction create request")
    void shouldValidateTransactionCreateRequest() throws Exception {
        // Invalid request with negative amount
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
        
        // Invalid request with same sender and receiver
        invalidRequest = new TransactionCreateRequest(
                "1",
                new BigDecimal("100.00"),
                "USD",
                LocalDateTime.now(),
                "Test transaction",
                "PENDING",
                1001L,
                1001L,
                "REF123456"
        );
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity());
    }
}