package com.example.newproject.service;

import com.example.newproject.model.Account;
import com.example.newproject.repository.AccountRepository;
import com.example.newproject.util.PhoneNumberValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(Account account) {
        validateAccount(account);
        return accountRepository.save(account);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    public Account updateAccount(Long id, Account accountDetails) {
        validateAccount(accountDetails);
        Account account = getAccountById(id);
        account.setFirstName(accountDetails.getFirstName());
        account.setLastName(accountDetails.getLastName());
        account.setPhoneNumber(accountDetails.getPhoneNumber());
        account.setAddress(accountDetails.getAddress());
        return accountRepository.save(account);
    }

    private void validateAccount(Account account) {
        if (!PhoneNumberValidator.isValidPhoneNumber(account.getPhoneNumber())) {
            String errorMessage = PhoneNumberValidator.getValidationErrorMessage(account.getPhoneNumber());
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    public Flux<Account> streamAllAccounts() {
        return Flux.fromIterable(accountRepository.findAll())
            .delayElements(Duration.ofMillis(500));
    }
}
