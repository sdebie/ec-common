package org.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.StaffRoleEn;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StaffDto
{
    private UUID id;
    private String email;
    private String fullName;
    private StaffRoleEn role;
    private boolean isActive;
    private boolean resetPassword;
    private String temporaryPassword;
    private LocalDateTime createdAt;
}
