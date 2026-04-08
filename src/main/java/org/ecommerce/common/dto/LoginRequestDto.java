package org.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

// The data coming IN from React-Vite
public record LoginRequestDto(@NotBlank @JsonAlias("username") String email, @NotBlank String password)
{
}
