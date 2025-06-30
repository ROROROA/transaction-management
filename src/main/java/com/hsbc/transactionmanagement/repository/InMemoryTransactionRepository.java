package com.hsbc.transactionmanagement.repository;

import com.hsbc.transactionmanagement.model.Transaction;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryTransactionRepository implements TransactionRepository{

    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();


    public Transaction save(Transaction transaction) {
        transactions.put(transaction.getTransactionId(),transaction);
        return transaction;
    }

    public void deleteById(String id) {
        transactions.remove(id);
    }

    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(transactions.get(id));
    }

    public List<Transaction> findAll() {
        return new ArrayList<>(transactions.values());
    }

    public boolean existsById(String id) {
        return transactions.containsKey(id);
    }

    @Override
    public Page<Transaction> findAll(Pageable pageable) {
        List<Transaction> allTransactions = new ArrayList<>(transactions.values());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allTransactions.size());
        
        // 处理边界情况
        if (start > allTransactions.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, allTransactions.size());
        }
        
        List<Transaction> pageContent = allTransactions.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allTransactions.size());
    }
}
