package com.example.bankingapp.repository;

import com.example.bankingapp.model.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmail(String email);

    boolean existsByBsn(String bsn);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByToken(String token);

    @Query("SELECT u FROM AppUser u WHERE u.role = com.example.bankingapp.model.AppUser.Role.CUSTOMER AND u.status = com.example.bankingapp.model.AppUser.Status.PENDING AND NOT EXISTS (SELECT a FROM Account a WHERE a.user = u)")
    Page<AppUser> findPendingCustomersWithoutAccounts(Pageable pageable);

    @Query("SELECT u FROM AppUser u WHERE u.role = com.example.bankingapp.model.AppUser.Role.CUSTOMER AND u.status = com.example.bankingapp.model.AppUser.Status.APPROVED AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<AppUser> searchApprovedCustomersByName(@Param("name") String name);
}
