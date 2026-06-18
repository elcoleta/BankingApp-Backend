package com.example.bankingapp.controller;

import com.example.bankingapp.dto.CustomerAccountDTO;
import com.example.bankingapp.dto.CustomerDTO;
import com.example.bankingapp.dto.TransactionDTO;
import com.example.bankingapp.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/accounts")
    public Page<CustomerAccountDTO> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return employeeService.getAllAccounts(
                PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    @PostMapping("/customers/{id}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveCustomer(@PathVariable Long id) {
        employeeService.approveCustomer(id);
    }

    @GetMapping("/customers/pending")
    public Page<CustomerDTO> getPendingCustomersWithoutAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return employeeService.getPendingCustomersWithoutAccounts(
                PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    @GetMapping("/transactions")
    public Page<TransactionDTO> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return employeeService.getAllTransactions(
                PageRequest.of(page, size, Sort.by("timestamp").descending()));
    }
}
