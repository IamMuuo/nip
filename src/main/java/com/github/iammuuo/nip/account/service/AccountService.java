package com.github.iammuuo.nip.account.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import com.github.iammuuo.nip.account.dto.AccountResponse;
import com.github.iammuuo.nip.account.dto.CreateAccountRequest;
import com.github.iammuuo.nip.account.entity.Account;
import com.github.iammuuo.nip.account.repository.AccountRepository;
import com.github.iammuuo.nip.account.service.exceptions.AccountNotFoundException;

/**
 * AccountService
 */
@Service
public class AccountService {

  private final AccountRepository repository;
  private final Logger logger;

  AccountService(AccountRepository repository) {
    this.repository = repository;
    this.logger = org.slf4j.LoggerFactory.getLogger(AccountRepository.class);
  }

  public AccountResponse createAccount(CreateAccountRequest request) {
    try {
      final Account account = Account.builder().name(request.name()).description(request.description()).build();
      final Account saved = repository.save(account);
      return toResponse(saved);

    } catch (Exception e) {
      logger.info("Failed to save new account | {}", e.getMessage());
    }
    return null;
  }

  public AccountResponse getAccountById(UUID id) {
    final Account account = repository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    return toResponse(account);
  }

  public AccountResponse toResponse(Account account) {
    return AccountResponse.builder()
        .id(account.getId())
        .name(account.getName())
        .description(account.getDescription())
        .active(account.isActive())
        .updatedAt(account.getUpdatedAt())
        .createdAt(account.getCreatedAt()).build();
  }

}
