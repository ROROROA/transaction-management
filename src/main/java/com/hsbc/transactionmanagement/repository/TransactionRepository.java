package com.hsbc.transactionmanagement.repository;

import com.hsbc.transactionmanagement.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);
    void deleteById(String id);
    Optional<Transaction> findById(String id);
    List<Transaction> findAll();
    boolean existsById(String id);
    Page<Transaction> findAll(Pageable pageable);
}
