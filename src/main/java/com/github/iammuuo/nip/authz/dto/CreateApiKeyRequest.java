package com.github.iammuuo.nip.authz.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateApiKeyRequest(
    String label,
    @NotBlank(message = "Owner cannot be empty") UUID owner) {
}
