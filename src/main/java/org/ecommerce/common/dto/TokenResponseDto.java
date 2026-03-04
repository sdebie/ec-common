package org.ecommerce.common.dto;

public record TokenResponseDto(
        String token,
        String username,
        String role
) {}
