package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.ecommerce.common.enums.AddressTypeEn;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * A customer's address (maps to the {@code customer_addresses} table).
 * A customer may have multiple addresses of different types
 * (PHYSICAL, POSTAL, BILLING, SHIPPING).
 */
@Entity
@Table(name = "customer_addresses")
public class CustomerAddressEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    public CustomerEntity customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", length = 20)
    public AddressTypeEn addressType;

    @Column(name = "address_line_1", nullable = false)
    public String addressLine1;

    @Column(name = "address_line_2")
    public String addressLine2;

    public String suburb;

    @Column(nullable = false)
    public String city;

    @Column(nullable = false)
    public String province;

    @Column(name = "postal_code", nullable = false)
    public String postalCode;

    @Column(name = "is_default")
    public boolean isDefault = false;
}

