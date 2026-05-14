package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import org.ecommerce.common.enums.CustomerStatusEn;
import org.ecommerce.common.enums.CustomerTypeEn;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "customers")
public class CustomerEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "shopper_type")
    public CustomerTypeEn shopperType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public CustomerStatusEn status = CustomerStatusEn.REGISTERING;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(name = "first_name")
    public String firstName;

    @Column(name = "last_name")
    public String lastName;

    public String phone;

    @Column(name = "physical_address_line1")
    public String physicalAddressLine1;

    @Column(name = "physical_address_line2")
    public String physicalAddressLine2;

    @Column(name = "physical_suburb")
    public String physicalSuburb;

    @Column(name = "physical_city")
    public String physicalCity;

    @Column(name = "physical_province")
    public String physicalProvince;

    @Column(name = "physical_postal_code")
    public String physicalPostalCode;

    @Column(name = "postal_address_line1")
    public String postalAddressLine1;

    @Column(name = "postal_address_line2")
    public String postalAddressLine2;

    @Column(name = "postal_suburb")
    public String postalSuburb;

    @Column(name = "postal_city")
    public String postalCity;

    @Column(name = "postal_province")
    public String postalProvince;

    @Column(name = "postal_postal_code")
    public String postalPostalCode;

    @Column(name = "additional_info", length = 1025, nullable = false)
    public String additionalInfo = "{}";

    @OneToMany(mappedBy = "customerEntity", cascade = CascadeType.ALL)
    public List<OrderEntity> orderEntities;

    @Column(name = "password_hash")
    public String passwordHash;

    @Column(name = "last_login")
    public LocalDateTime passwordUpdatedAt;

    @Column(name = "reset_token")
    public String resetToken;

    @Column(name = "reset_token_expiry")
    public LocalDateTime resetTokenExpiry;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Helper to find a customer by email (useful for logins/lookups)
     */
    public static CustomerEntity findByEmail(String email) {
        return find("email", email).firstResult();
    }

}
