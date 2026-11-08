package com.github.iammuuo.nip.authz.dto;

import java.time.Instant;
import java.util.UUID;
import java.util.Set;

import lombok.Builder;

@Builder
public record ApiKeyResponse(
    UUID id,
    String label,
    UUID owner,
    Set<String> scopes,
    Instant lastUsedAt,
    Instant expiresAt,
    Instant createdAt,
    Instant updatedAt

) {
}
