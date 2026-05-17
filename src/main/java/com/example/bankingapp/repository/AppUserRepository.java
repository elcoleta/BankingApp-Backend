package com.example.bankingapp.repository;

import java.util.Optional;

import com.example.bankingapp.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByUsername(String username);

    Optional<AppUser> findByUsername(String username);
}
