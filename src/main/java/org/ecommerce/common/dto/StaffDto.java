package org.ecommerce.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.ecommerce.common.enums.StaffRoleEn;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StaffDto {
    private UUID id;
    private String email;
    private String fullName;
    private StaffRoleEn role;
    private boolean isActive;
    private boolean resetPassword;
    private String temporaryPassword;
    private LocalDateTime createdAt;
}
