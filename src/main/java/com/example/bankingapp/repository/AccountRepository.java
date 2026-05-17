package com.example.bankingapp.repository;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // Get all accounts belonging to a user
    List<Account> findByUser(AppUser user);

    // Find account by IBAN
    Optional<Account> findByIban(String iban);
}
