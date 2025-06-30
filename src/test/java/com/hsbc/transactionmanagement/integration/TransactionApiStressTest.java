package com.hsbc.transactionmanagement.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.transactionmanagement.model.Transaction;
import com.hsbc.transactionmanagement.model.TransactionCreateRequest;
import com.hsbc.transactionmanagement.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionApiStressTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        // Clear repository before each test
        transactionRepository.findAll().forEach(transaction -> 
                transactionRepository.deleteById(transaction.getTransactionId()));
    }

    @Test
    @DisplayName("Should handle multiple concurrent transaction creation requests")
    void shouldHandleConcurrentTransactionCreations() throws Exception {


        // Number of concurrent requests
        int concurrentRequests = 50;
        
        // Create a thread pool and countdown latch
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch latch = new CountDownLatch(concurrentRequests);
        
        List<Exception> exceptions = new ArrayList<>();
        
        // Submit concurrent tasks
        for (int i = 0; i < concurrentRequests; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    // Create a unique transaction request
                    TransactionCreateRequest request = new TransactionCreateRequest(
                            UUID.randomUUID().toString(),
                            new BigDecimal("100.00").add(new BigDecimal(index)),
                            "USD",
                            LocalDateTime.now(),
                            "Stress test transaction " + index,
                            "PENDING",
                            1000L + index,
                            2000L + index,
                            "REF-STRESS-" + index
                    );
                    
                    // Send the request
                    mockMvc.perform(post("/api/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.transactionId", notNullValue()));
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all requests to complete or timeout after 30 seconds
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        
        // Shutdown the executor
        executorService.shutdown();
        
        // Assert that all requests completed and no exceptions occurred
        assertTrue(completed, "Not all requests completed within the timeout period");
        assertTrue(exceptions.isEmpty(), "Exceptions occurred during concurrent requests: " + exceptions);
        
        // Verify that all transactions were created
        List<Transaction> allTransactions = transactionRepository.findAll();
        int count = allTransactions.size();



        assertTrue(count == concurrentRequests,
                "Expected " + concurrentRequests + " transactions, but found " + count);
    }
}