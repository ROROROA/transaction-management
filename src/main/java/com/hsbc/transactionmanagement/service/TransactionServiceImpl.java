package com.hsbc.transactionmanagement.service;

import com.hsbc.transactionmanagement.exceptions.DuplicateTransactionException;
import com.hsbc.transactionmanagement.exceptions.TransactionNotFoundException;
import com.hsbc.transactionmanagement.model.Transaction;
import com.hsbc.transactionmanagement.model.TransactionCreateRequest;
import com.hsbc.transactionmanagement.model.TransactionUpdateRequest;
import com.hsbc.transactionmanagement.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }


    @Override
    @CachePut(value = "transactions", key = "#result.transactionId")
    public Transaction createTransaction(TransactionCreateRequest transactionCreateRequest){

        // DTO to Domain entity and validate
        Transaction initTransaction = transactionCreateRequest.toEntity();

        initTransaction.validateBusinessRules();

        if (initTransaction.getTransactionId() != null && transactionRepository.existsById(initTransaction.getTransactionId())) {
            throw new DuplicateTransactionException(initTransaction.getTransactionId());
        }
        return transactionRepository.save(initTransaction);

    }



    @Override
    @CacheEvict(value = "transactions", key = "#id")
    public void deleteTransactionById(String id){
        if (!transactionRepository.existsById(id)) {
            throw new TransactionNotFoundException(id);
        }
        transactionRepository.deleteById(id);
    }

    @Override
    @CachePut(value = "transactions", key = "#id")
    public Transaction updateTransaction(String id, TransactionUpdateRequest transactionUpdateRequest){

        Transaction currentTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        currentTransaction.updateStatus(transactionUpdateRequest.status());

        // Update currentTransaction by  transactionUpdateRequest
        return transactionRepository.save(currentTransaction);
    }

    @Override
    @Cacheable(value = "transactions", key = "#id")
    public Transaction getTransactionById(String id){
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public Page<Transaction> getAllTransactionsPaged(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }
}
