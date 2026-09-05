package org.ecommerce.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequestDto
{
    @NotBlank
    private String currentPassword;

    // 8/72 mirrors backend.utils.PasswordStrengthValidator's own limits (NIST 800-63B
    // minimum, and BCrypt's 72-byte truncation) — ec-common can't reference that class
    // (ec-backend depends on ec-common, not the other way around), so the two must be
    // kept in sync by hand.
    @NotBlank
    @Size(min = 8, max = 72)
    private String newPassword;
}
