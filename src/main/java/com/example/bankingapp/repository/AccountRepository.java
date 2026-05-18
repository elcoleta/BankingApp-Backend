package com.example.bankingapp.repository;

import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUser(AppUser user);

    boolean existsByIban(String iban);
}
