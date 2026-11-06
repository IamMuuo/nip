package com.github.iammuuo.nip.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateAccountRequest(
    @NotBlank @Size(max = 256, min = 3, message = "Name cannot exceed 255 characters or be less than 3 characters") String name,
    String description) {
}
