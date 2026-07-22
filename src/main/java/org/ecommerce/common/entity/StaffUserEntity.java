package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.StaffRoleEn;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "staff_users")
public class StaffUserEntity extends PanacheEntityBase
{
    @Id
    @GeneratedValue
    private UUID id; // Using UUID as requested

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING) // Stores the name (e.g., 'CATALOG_MANAGER') in DB
    @Column(nullable = false)
    private StaffRoleEn role;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "reset_password")
    private boolean resetPassword = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Helper method for login and admin lookups
    public static StaffUserEntity findByEmail(String email)
    {
        return find("lower(email) = lower(?1)", email).firstResult();
    }
}