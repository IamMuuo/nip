package com.github.iammuuo.nip.authz.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ApiKeyResponse(
    UUID id,
    String label,
    UUID owner,
    Instant lastUsedAt,
    Instant expiresAt,
    Instant createdAt,
    Instant updatedAt

) {
}
