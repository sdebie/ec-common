package org.ecommerce.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequestDto
{
    private String currentPassword;
    private String newPassword;
}
