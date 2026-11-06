package com.github.iammuuo.nip.authz.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.github.iammuuo.nip.account.entity.Account;
import com.github.iammuuo.nip.account.repository.AccountRepository;
import com.github.iammuuo.nip.account.service.exceptions.AccountNotFoundException;
import com.github.iammuuo.nip.authz.dto.ApiKeyCreatedResponse;
import com.github.iammuuo.nip.authz.dto.ApiKeyResponse;
import com.github.iammuuo.nip.authz.dto.CreateApiKeyRequest;
import com.github.iammuuo.nip.authz.entity.ApiKey;
import com.github.iammuuo.nip.authz.repository.ApiKeyRepository;
import com.github.iammuuo.nip.authz.service.Generator.ApiKeyGenerator;

import jakarta.transaction.Transactional;

/**
 * ApiService
 */
@Service
public class ApiKeyService {

  private final ApiKeyRepository apiKeyRepository;
  private final AccountRepository accountRepository;
  private final Logger logger;

  ApiKeyService(ApiKeyRepository repository, AccountRepository accountRepository) {
    this.apiKeyRepository = repository;
    this.accountRepository = accountRepository;
    this.logger = LoggerFactory.getLogger(ApiKeyService.class);
  }

  @Transactional
  public ApiKeyCreatedResponse createApiKey(CreateApiKeyRequest createApiKeyRequest) {
    final String key = ApiKeyGenerator.generateKey();

    final Account owner = accountRepository.findById(createApiKeyRequest.owner())
        .orElseThrow(() -> new AccountNotFoundException(createApiKeyRequest.owner()));

    final ApiKey apiKey = ApiKey.builder()
        .label(createApiKeyRequest.label())
        .keyHash(hashKey(key))
        .account(owner)
        .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
        .build();

    final ApiKey created = apiKeyRepository.save(apiKey);

    logger.info("Api key generated successfully!");

    ApiKeyCreatedResponse apiKeyCreatedResponse = ApiKeyCreatedResponse
        .builder()
        .details(toResponse(created))
        .key(key)
        .build();

    return apiKeyCreatedResponse;
  }

  /*
   * hashKey
   *
   * Hashes the string parameter raw and
   * returns a hashed version of the string
   */
  public String hashKey(String raw) {
    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    return bCryptPasswordEncoder.encode(raw);
  }

  /*
   * compareHash
   *
   * Compares the raw hash and the hashed key effectively
   * returning true if the hashes match otherwise false
   */
  public boolean compareHash(String raw, String hash) {
    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    return bCryptPasswordEncoder.matches(raw, hash);
  }

  ApiKeyResponse toResponse(ApiKey apiKey) {
    return ApiKeyResponse.builder()
        .owner(apiKey.getAccount().getId())
        .label(apiKey.getLabel())
        .lastUsedAt(apiKey.getLastUsedAt())
        .expiresAt(apiKey.getExpiresAt())
        .createdAt(apiKey.getCreatedAt())
        .updatedAt(apiKey.getUpdatedAt())
        .build();
  }

}
