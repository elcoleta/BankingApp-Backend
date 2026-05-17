package com.example.bankingapp.repository;

import com.example.bankingapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // All transactions where the user's IBANs appear as sender or receiver, newest first
    List<Transaction> findByFromIbanInOrToIbanInOrderByTimestampDesc(
            List<String> fromIbans, List<String> toIbans);

    /**
     * Sum of all amounts sent from a given IBAN within a specific time window.
     * Used to enforce the daily transfer limit.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.fromIban = :iban AND t.timestamp >= :startOfDay")
    double sumAmountSentFromIbanSince(@Param("iban") String iban,
                                      @Param("startOfDay") LocalDateTime startOfDay);
}
