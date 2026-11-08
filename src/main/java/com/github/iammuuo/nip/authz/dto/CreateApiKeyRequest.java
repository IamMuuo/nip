package com.github.iammuuo.nip.authz.dto;

import java.util.Set;
import lombok.Builder;

@Builder
public record CreateApiKeyRequest(
    String label,
    Set<String> scopes) {
}
