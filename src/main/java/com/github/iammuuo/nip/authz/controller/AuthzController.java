package com.github.iammuuo.nip.authz.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.iammuuo.nip.authz.dto.ApiKeyCreatedResponse;
import com.github.iammuuo.nip.authz.dto.CreateApiKeyRequest;
import com.github.iammuuo.nip.authz.service.ApiKeyService;

@RestController
@RequestMapping("api/api-key")
public class AuthzController {

  private final ApiKeyService apiKeyService;

  AuthzController(ApiKeyService apiKeyService) {
    this.apiKeyService = apiKeyService;
  }

  @PostMapping
  public ResponseEntity<?> createApiKey(@RequestBody CreateApiKeyRequest createApiKeyRequest) {
    final ApiKeyCreatedResponse created = apiKeyService.createApiKey(createApiKeyRequest);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(created);
  }

}
