package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
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
public class UserEntity extends PanacheEntityBase
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

    /**
     * PostgreSQL TEXT[] column — roles such as 'RETAIL', 'WHOLESALE'.
     * Default mirrors the DB default of '{RETAIL}'.
     */
    @Column(name = "roles", columnDefinition = "text[]")
    private String[] roles = new String[]{"RETAIL"};

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "mfa_enabled")
    private boolean mfaEnabled = false;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // ── Security tokens ────────────────────────────────────────────────────
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private OffsetDateTime resetTokenExpiry;

    @Column(name = "password_reset_code_hash")
    private String passwordResetCodeHash;

    @Column(name = "password_reset_code_expiry")
    private OffsetDateTime passwordResetCodeExpiry;

    @Column(name = "password_reset_code_attempts")
    private int passwordResetCodeAttempts = 0;

    @Column(name = "password_reset_code_locked_until")
    private OffsetDateTime passwordResetCodeLockedUntil;

    // ── Relationships ───────────────────────────────────────────────────────
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerEntity customer;

    // ── Helpers ─────────────────────────────────────────────────────────────
    public static UserEntity findByEmail(String email)
    {
        return find("lower(email) = lower(?1)", email).firstResult();
    }

    public static UserEntity findByResetToken(String token)
    {
        return find("resetToken", token).firstResult();
    }
}

