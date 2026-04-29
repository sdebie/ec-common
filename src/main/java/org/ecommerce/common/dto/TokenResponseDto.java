package org.ecommerce.common.dto;

public record TokenResponseDto(
        String token,
        String email,
        String role,
        boolean resetPassword
) {}
