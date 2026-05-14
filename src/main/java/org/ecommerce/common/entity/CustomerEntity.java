package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import org.ecommerce.common.enums.CustomerStatusEn;
import org.ecommerce.common.enums.CustomerTypeEn;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Customer profile (maps to the {@code customers} table).
 * Authentication credentials and security tokens live in the linked {@link UserEntity}.
 * Addresses live in {@link CustomerAddressEntity}.
 * Contacts live in {@link CustomerContactEntity}.
 * Wholesale-specific data lives in {@link WholesaleProfileEntity}.
 */
@Entity
@Getter
@Table(name = "customers")
public class CustomerEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    // ── Link to user account ────────────────────────────────────────────────
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    public UserEntity user;

    // ── Profile ─────────────────────────────────────────────────────────────
    @Column(name = "first_name")
    public String firstName;

    @Column(name = "last_name")
    public String lastName;

    public String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "shopper_type")
    public CustomerTypeEn shopperType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public CustomerStatusEn status = CustomerStatusEn.PENDING;

    // ── Relationships ────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CustomerAddressEntity> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CustomerContactEntity> contacts = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public WholesaleProfileEntity wholesaleProfile;

    @OneToMany(mappedBy = "customerEntity", cascade = CascadeType.ALL)
    public List<OrderEntity> orderEntities;

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Finds a customer by the email stored on the linked {@link UserEntity}.
     */
    public static CustomerEntity findByEmail(String email) {
        return find("lower(user.email) = lower(?1)", email).firstResult();
    }

}
