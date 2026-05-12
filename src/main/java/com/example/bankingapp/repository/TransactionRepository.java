package com.example.bankingapp.repository;

import com.example.bankingapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // All transactions where the user's IBANs appear as sender or receiver, newest first
    List<Transaction> findByFromIbanInOrToIbanInOrderByTimestampDesc(
            List<String> fromIbans, List<String> toIbans);
}
