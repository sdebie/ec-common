package org.ecommerce.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an authenticated user account (maps to the {@code users} table).
 * Credentials, security tokens, and roles live here.
 * Profile/personal data lives in the linked {@link CustomerEntity}.
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity
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
    
    @Column(name = "roles", columnDefinition = "text[]")
    private String[] roles = new String[]{"RETAIL"};

    @Column(name = "mfa_enabled")
    private boolean mfaEnabled = false;

    @Column(name = "last_login")
    private Instant lastLogin;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private CustomerEntity customer;

}

