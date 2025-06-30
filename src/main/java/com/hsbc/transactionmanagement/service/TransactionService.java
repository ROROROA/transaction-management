package com.hsbc.transactionmanagement.service;

import com.hsbc.transactionmanagement.model.Transaction;
import com.hsbc.transactionmanagement.model.TransactionCreateRequest;
import com.hsbc.transactionmanagement.model.TransactionUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {

    Transaction createTransaction(TransactionCreateRequest transactionCreateRequest) ;
    void deleteTransactionById(String id);
    Transaction updateTransaction(String id, TransactionUpdateRequest transactionUpdateRequest);
    Transaction getTransactionById(String id);
    List<Transaction> getAllTransactions();
    Page<Transaction> getAllTransactionsPaged(Pageable pageable);

}
