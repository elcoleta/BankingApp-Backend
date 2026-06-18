package com.example.bankingapp.controller;

import com.example.bankingapp.dto.CustomerSearchDTO;
import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.AppUserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final AppUserRepository userRepository;
    private final AccountRepository accountRepository;

    public CustomerController(AppUserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    // Accessible to any authenticated user — used by customers to find transfer recipients
    @GetMapping("/search")
    public List<CustomerSearchDTO> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String iban
    ) {
        if (iban != null && !iban.isBlank()) {
            return accountRepository.findByIban(iban.trim().toUpperCase())
                    .filter(a -> a.getUser().getRole() == AppUser.Role.CUSTOMER)
                    .map(a -> toDTO(a.getUser()))
                    .map(List::of)
                    .orElse(List.of());
        }
        if (name != null && !name.isBlank()) {
            return userRepository.searchApprovedCustomersByName(name.trim()).stream()
                    .map(this::toDTO)
                    .toList();
        }
        return List.of();
    }

    private CustomerSearchDTO toDTO(AppUser user) {
        List<String> ibans = accountRepository.findByUser(user).stream()
                .map(Account::getIban)
                .toList();
        return new CustomerSearchDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), ibans);
    }
}
