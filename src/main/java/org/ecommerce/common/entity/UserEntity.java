package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents an authenticated user account (maps to the {@code users} table).
 * Credentials, security tokens, and roles live here.
 * Profile/personal data lives in the linked {@link CustomerEntity}.
 */
@Entity
@Table(name = "users")
public class UserEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    /**
     * PostgreSQL TEXT[] column — roles such as 'RETAIL', 'WHOLESALE'.
     * Default mirrors the DB default of '{RETAIL}'.
     */
    @Column(name = "roles", columnDefinition = "text[]")
    public String[] roles = new String[]{"RETAIL"};

    @Column(name = "is_active")
    public boolean isActive = true;

    @Column(name = "mfa_enabled")
    public boolean mfaEnabled = false;

    @Column(name = "last_login")
    public OffsetDateTime lastLogin;

    @Column(name = "created_at")
    public OffsetDateTime createdAt = OffsetDateTime.now();

    // ── Security tokens ────────────────────────────────────────────────────
    @Column(name = "reset_token")
    public String resetToken;

    @Column(name = "reset_token_expiry")
    public OffsetDateTime resetTokenExpiry;

    // ── Relationships ───────────────────────────────────────────────────────
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public CustomerEntity customer;

    // ── Helpers ─────────────────────────────────────────────────────────────

    public static UserEntity findByEmail(String email) {
        return find("lower(email) = lower(?1)", email).firstResult();
    }

    public static UserEntity findByResetToken(String token) {
        return find("resetToken", token).firstResult();
    }
}

