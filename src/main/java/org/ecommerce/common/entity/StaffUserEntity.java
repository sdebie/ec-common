package org.ecommerce.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.StaffRoleEn;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "staff_users")
public class StaffUserEntity
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "password_reset_code_hash")
    private String passwordResetCodeHash;

    @Column(name = "password_reset_code_expiry")
    private Instant passwordResetCodeExpiry;

    @Column(name = "password_reset_code_attempts")
    private int passwordResetCodeAttempts = 0;

    @Column(name = "password_reset_code_locked_until")
    private Instant passwordResetCodeLockedUntil;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING) // Stores the name (e.g., 'CATALOG_MANAGER') in DB
    @Column(nullable = false)
    private StaffRoleEn role;

    @Column(name = "reset_password")
    private boolean resetPassword = false;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}