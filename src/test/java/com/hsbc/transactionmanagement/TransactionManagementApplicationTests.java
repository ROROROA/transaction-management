package com.hsbc.transactionmanagement;

import com.hsbc.transactionmanagement.controller.TransactionController;
import com.hsbc.transactionmanagement.repository.TransactionRepository;
import com.hsbc.transactionmanagement.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TransactionManagementApplicationTests {

    @Autowired
    private TransactionController transactionController;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        // This test will fail if the application context cannot start
    }

    @Test
    @DisplayName("All required beans are created")
    void allBeansAreCreated() {
        assertNotNull(transactionController, "TransactionController should be created");
        assertNotNull(transactionService, "TransactionService should be created");
        assertNotNull(transactionRepository, "TransactionRepository should be created");
    }

}
