package com.example.bankingapp.service;

import com.example.bankingapp.dto.CustomerAccountDTO;
import com.example.bankingapp.dto.CustomerDTO;
import com.example.bankingapp.exception.ResourceNotFoundException;
import com.example.bankingapp.model.Account;
import com.example.bankingapp.model.AppUser;
import com.example.bankingapp.repository.AccountRepository;
import com.example.bankingapp.repository.AppUserRepository;
import com.example.bankingapp.util.IbanGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final AccountRepository accountRepository;
    private final AppUserRepository userRepository;

    public EmployeeService(AccountRepository accountRepository, AppUserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Page<CustomerAccountDTO> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<CustomerDTO> getPendingCustomersWithoutAccounts(Pageable pageable) {
        return userRepository.findPendingCustomersWithoutAccounts(pageable).map(this::toCustomerDTO);
    }

    @Transactional
    public void approveCustomer(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + userId));

        user.setStatus(AppUser.Status.APPROVED);
        userRepository.save(user);

        accountRepository.save(new Account(uniqueIban(), "CHECKING", 0.0, 1000.0, 5000.0, true, user));
        accountRepository.save(new Account(uniqueIban(), "SAVINGS",  0.0, 1000.0, 5000.0, true, user));
    }

    private String uniqueIban() {
        String iban;
        do {
            iban = IbanGenerator.generate();
        } while (accountRepository.existsByIban(iban));
        return iban;
    }

    private CustomerDTO toCustomerDTO(AppUser user) {
        return new CustomerDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getBsn(),
                user.getPhoneNumber(),
                user.getStatus().name()
        );
    }

    private CustomerAccountDTO toDTO(Account account) {
        var user = account.getUser();
        return new CustomerAccountDTO(
                account.getId(),
                account.getIban(),
                account.getAccountType(),
                account.getBalance(),
                account.getAbsoluteTransferLimit(),
                account.getDailyTransferLimit(),
                account.isActive(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getStatus().name()
        );
    }
}
