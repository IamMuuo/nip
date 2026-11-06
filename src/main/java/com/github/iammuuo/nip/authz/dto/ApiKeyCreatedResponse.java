package com.github.iammuuo.nip.authz.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Builder;

@Builder
public record ApiKeyCreatedResponse(
    String key,
    @JsonUnwrapped ApiKeyResponse details) {
}
