package com.github.iammuuo.nip.authz.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

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
  private final PasswordEncoder passwordEncoder;

  ApiKeyService(ApiKeyRepository repository, AccountRepository accountRepository,
      PasswordEncoder passwordEncoder) {
    this.apiKeyRepository = repository;
    this.accountRepository = accountRepository;
    this.logger = LoggerFactory.getLogger(ApiKeyService.class);
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public ApiKeyCreatedResponse createApiKey(CreateApiKeyRequest createApiKeyRequest) {
    final String key = ApiKeyGenerator.generateKey();
    final int separator = key.indexOf('.');
    final String keyId = key.substring(0, separator);
    final String secret = key.substring(separator + 1);

    final Set<String> scopes = createApiKeyRequest.scopes() == null
        ? Set.of("account:read", "api-key:manage")
        : Set.copyOf(createApiKeyRequest.scopes());
    if (!scopes.stream().allMatch(scope -> scope.matches("[a-z-]+:[a-z-]+"))) {
      throw new IllegalArgumentException("Invalid API key scope");
    }
    final Account owner = accountRepository.findById(currentAccountId())
        .orElseThrow(() -> new AccountNotFoundException(currentAccountId()));

    final ApiKey apiKey = ApiKey.builder()
        .label(createApiKeyRequest.label())
        .keyId(keyId)
        .keyHash(hashKey(secret))
        .scopes(String.join(",", scopes))
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
    return passwordEncoder.encode(raw);
  }

  /*
   * compareHash
   *
   * Compares the raw hash and the hashed key effectively
   * returning true if the hashes match otherwise false
   */
  public boolean compareHash(String raw, String hash) {
    return passwordEncoder.matches(raw, hash);
  }

  ApiKeyResponse toResponse(ApiKey apiKey) {
    return ApiKeyResponse.builder()
        .id(apiKey.getId())
        .owner(apiKey.getAccount().getId())
        .label(apiKey.getLabel())
        .scopes(apiKey.getScopes() == null || apiKey.getScopes().isBlank()
            ? Set.of() : Set.of(apiKey.getScopes().split(",")))
        .lastUsedAt(apiKey.getLastUsedAt())
        .expiresAt(apiKey.getExpiresAt())
        .createdAt(apiKey.getCreatedAt())
        .updatedAt(apiKey.getUpdatedAt())
        .build();
  }

  @Transactional
  public void revokeApiKey(UUID id) {
    ApiKey apiKey = ownedKey(id);
    apiKey.setActive(false);
    apiKeyRepository.save(apiKey);
  }

  @Transactional
  public ApiKeyCreatedResponse rotateApiKey(UUID id) {
    ApiKey oldKey = ownedKey(id);
    oldKey.setActive(false);
    apiKeyRepository.save(oldKey);
    return createApiKey(new CreateApiKeyRequest(oldKey.getLabel(),
        oldKey.getScopes() == null || oldKey.getScopes().isBlank()
            ? Set.of("account:read", "api-key:manage")
            : Set.of(oldKey.getScopes().split(","))));
  }

  private ApiKey ownedKey(UUID id) {
    UUID accountId = currentAccountId();
    return apiKeyRepository.findById(id)
        .filter(key -> key.getAccount() != null && accountId.equals(key.getAccount().getId()))
        .orElseThrow(() -> new SecurityException("API key not found"));
  }

  private UUID currentAccountId() {
    String principal = org.springframework.security.core.context.SecurityContextHolder
        .getContext().getAuthentication().getName();
    return UUID.fromString(principal);
  }

}
