package com.hsbc.transactionmanagement.controller;

import com.hsbc.transactionmanagement.model.Transaction;
import com.hsbc.transactionmanagement.model.TransactionCreateRequest;
import com.hsbc.transactionmanagement.model.TransactionResponse;
import com.hsbc.transactionmanagement.model.TransactionUpdateRequest;
import com.hsbc.transactionmanagement.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Management", description = "Transaction Management API")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    private final AtomicLong idGenerator;

    @Autowired
    public TransactionController(TransactionService transactionService,AtomicLong idGenerator) {
        this.transactionService = transactionService;
        this.idGenerator = idGenerator;
        logger.info("TransactionController initialized");
    }

    @Operation(summary = "Create new transaction", description = "Create a new transaction record")
    @ApiResponse(responseCode = "201", description = "Transaction created successfully",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody @Valid TransactionCreateRequest transactionCreateRequest) {
        logger.info("Creating new transaction: {}", transactionCreateRequest);
        Transaction savedTransaction = this.transactionService.createTransaction(transactionCreateRequest);
        logger.info("Transaction created successfully with ID: {}", savedTransaction.getTransactionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.fromEntity(savedTransaction));
    }

    @Operation(summary = "Delete transaction", description = "Delete transaction by ID")
    @ApiResponse(responseCode = "204", description = "Transaction deleted successfully")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransactionById(
            @Parameter(description = "Transaction ID") @PathVariable String id) {
        logger.info("Deleting transaction with ID: {}", id);
        this.transactionService.deleteTransactionById(id);
        logger.info("Transaction deleted successfully: {}", id);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Update transaction", description = "Only allowed to update part of fields, like cancel a transaction")
    @ApiResponse(responseCode = "200", description = "Transaction updated successfully")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @Parameter(description = "Transaction ID") @PathVariable String id,
            @RequestBody @Valid TransactionUpdateRequest transactionUpdateRequest) {
        logger.info("Updating transaction with ID: {}, request: {}", id, transactionUpdateRequest);
        Transaction updatedTransaction = this.transactionService.updateTransaction(id, transactionUpdateRequest);
        logger.info("Transaction updated successfully: {}", id);
        return ResponseEntity.ok(TransactionResponse.fromEntity(updatedTransaction));
    }

    @Operation(summary = "Get single transaction", description = "Get transaction details by ID")
    @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "Transaction ID") @PathVariable String id) {
        logger.info("Fetching transaction with ID: {}", id);
        Transaction transaction = this.transactionService.getTransactionById(id);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transaction));
    }

    @Operation(summary = "Get all transactions", description = "Get all transaction records in the system")
    @ApiResponse(responseCode = "200", description = "Transaction list retrieved successfully")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        logger.info("Fetching all transactions");
        List<Transaction> transactions = this.transactionService.getAllTransactions();
        logger.info("Retrieved {} transactions", transactions.size());
        List<TransactionResponse> responses = transactions.stream()
                .map(TransactionResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get transactions with pagination", 
               description = "Get transaction records with pagination support")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    @GetMapping("/paged")
    public ResponseEntity<Page<TransactionResponse>> getTransactionsPaged(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int size) {
        logger.info("Fetching paged transactions - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionsPage = this.transactionService.getAllTransactionsPaged(pageable);
        logger.info("Retrieved page {} of {} with {} transactions", 
                transactionsPage.getNumber(), 
                transactionsPage.getTotalPages(),
                transactionsPage.getNumberOfElements());
        Page<TransactionResponse> responsePage = transactionsPage.map(TransactionResponse::fromEntity);
        return ResponseEntity.ok(responsePage);
    }



    @Operation(summary = "Generate next transaction ID", 
               description = "Generate a new unique transaction ID using Snowflake algorithm")
    @ApiResponse(responseCode = "200", description = "Transaction ID generated successfully")
    @GetMapping("/next-id")
    public ResponseEntity<Map<String, String>> generateNextTransactionId() {
        logger.info("Generating next transaction ID");
        String nextId = String.valueOf(this.idGenerator.getAndIncrement());
        logger.info("Generated transaction ID: {}", nextId);
        return ResponseEntity.ok(Map.of("transactionId", nextId));
    }
}
