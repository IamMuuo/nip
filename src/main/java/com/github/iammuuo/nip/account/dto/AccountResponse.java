package com.github.iammuuo.nip.account.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record AccountResponse(
    UUID id,
    String name,
    String description,
    Boolean active,
    Instant createdAt,
    Instant updatedAt) {
}
