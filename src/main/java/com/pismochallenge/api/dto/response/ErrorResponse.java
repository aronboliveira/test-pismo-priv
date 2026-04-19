package com.pismochallenge.api.dto.response;

public record ErrorResponse(
    int status,
    String error,
    String message
) {}
