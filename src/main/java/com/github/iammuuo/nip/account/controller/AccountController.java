package com.github.iammuuo.nip.account.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.iammuuo.nip.account.dto.AccountResponse;
import com.github.iammuuo.nip.account.dto.CreateAccountRequest;
import com.github.iammuuo.nip.account.service.AccountService;

import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;

/**
 * AccountController
 */
@RestController
@RequestMapping("api/accounts")
public class AccountController {
  private final AccountService accountService;
  private final Logger logger;

  AccountController(AccountService service) {
    this.accountService = service;
    this.logger = org.slf4j.LoggerFactory.getLogger(AccountController.class);
  }

  @PostMapping
  public ResponseEntity<?> createAccount(
      @Valid @RequestBody CreateAccountRequest accountRequest) {
    final AccountResponse response = accountService.createAccount(accountRequest);

    if (response == null) {
      logger.error("Failed to save account see previous log.");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "we couldn't create your account at the moment"));
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getAccountById(@PathVariable UUID id) {
    final AccountResponse response = accountService.getAccountById(id);
    return ResponseEntity.ok(response);
  }
}
