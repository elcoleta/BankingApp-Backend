package com.example.bankingapp.repository;

import com.example.bankingapp.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromIbanInOrToIbanInOrderByTimestampDesc(
            List<String> fromIbans, List<String> toIbans);

    Page<Transaction> findByFromIbanInOrToIbanInOrderByTimestampDesc(
            List<String> fromIbans, List<String> toIbans, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.fromIban IN :ibans OR t.toIban IN :ibans) AND t.timestamp BETWEEN :from AND :to ORDER BY t.timestamp DESC")
    List<Transaction> findByIbansAndDateRange(
            @Param("ibans") List<String> ibans,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.fromIban = :iban AND t.timestamp >= :startOfDay")
    double sumTodayOutgoing(@Param("iban") String iban, @Param("startOfDay") LocalDateTime startOfDay);

    Page<Transaction> findAllByOrderByTimestampDesc(Pageable pageable);
}
