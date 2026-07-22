package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.AddressTypeEn;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * A customer's address (maps to the {@code customer_addresses} table).
 * A customer may have multiple addresses of different types
 * (PHYSICAL, POSTAL, BILLING, SHIPPING).
 */
@Getter
@Setter
@Entity
@Table(name = "customer_addresses")
public class CustomerAddressEntity extends PanacheEntityBase
{

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private CustomerEntity customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", length = 20)
    private AddressTypeEn addressType;

    @Column(name = "address_line_1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    private String suburb;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String province;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "is_default")
    private boolean isDefault = false;
}

