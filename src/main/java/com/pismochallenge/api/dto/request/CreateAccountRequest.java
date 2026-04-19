package com.pismochallenge.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAccountRequest(
    @NotBlank(message = "document_number is required")
    String documentNumber
) {}
