package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.ecommerce.common.enums.StaffRoleEn;

import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_users")
public class StaffUserEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id; // Using UUID as requested

    @Column(unique = true, nullable = false)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Column(name = "full_name")
    public String fullName;

    @Enumerated(EnumType.STRING) // Stores the name (e.g., 'CATALOG_MANAGER') in DB
    @Column(nullable = false)
    public StaffRoleEn role;

    @Column(name = "is_active")
    public boolean isActive = true;

    @Column(name = "reset_password")
    public boolean resetPassword = false;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();

    // Helper method for login and admin lookups
    public static StaffUserEntity findByEmail(String email) {
        return find("lower(email) = lower(?1)", email).firstResult();
    }
}