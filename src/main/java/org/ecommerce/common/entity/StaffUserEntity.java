package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.StaffRoleEn;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "staff_users")
public class StaffUserEntity extends PanacheEntityBase
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
    private OffsetDateTime passwordResetCodeExpiry;

    @Column(name = "password_reset_code_attempts")
    private int passwordResetCodeAttempts = 0;

    @Column(name = "password_reset_code_locked_until")
    private OffsetDateTime passwordResetCodeLockedUntil;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING) // Stores the name (e.g., 'CATALOG_MANAGER') in DB
    @Column(nullable = false)
    private StaffRoleEn role;

    @Column(name = "reset_password")
    private boolean resetPassword = false;

    // staff_users.created_at is a plain TIMESTAMP (no timezone), unlike users.created_at
    // (TIMESTAMPTZ) — the two are typed differently for that reason (LocalDateTime here,
    // OffsetDateTime on UserEntity).
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}