package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.AddressTypeEn;
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
@Getter
@Setter
@Entity
@Table(name = "customers")
public class CustomerEntity extends PanacheEntityBase
{

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserEntity user;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "shopper_type")
    private CustomerTypeEn shopperType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatusEn status = CustomerStatusEn.PENDING;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerAddressEntity> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerContactEntity> contacts = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WholesaleProfileEntity wholesaleProfile;

    @OneToMany(mappedBy = "customerEntity", cascade = CascadeType.ALL)
    private List<OrderEntity> orderEntities;

    /**
     * Finds a customer by the email stored on the linked {@link UserEntity}.
     */
    public static CustomerEntity findByEmail(String email)
    {
        return find("lower(user.email) = lower(?1)", email).firstResult();
    }


    /**
     * The customer's physical address, or null when none has been captured.
     * A customer holds typed address rows; flat views read through these.
     */
    public CustomerAddressEntity getPhysicalAddress()
    {
        return addressOfType(AddressTypeEn.PHYSICAL);
    }

    /**
     * The customer's postal address, or null when none has been captured.
     */
    public CustomerAddressEntity getPostalAddress()
    {
        return addressOfType(AddressTypeEn.POSTAL);
    }

    private CustomerAddressEntity addressOfType(AddressTypeEn type)
    {
        if (addresses == null) {
            return null;
        }
        return addresses.stream()
                .filter(a -> a != null && a.getAddressType() == type)
                .findFirst()
                .orElse(null);
    }
}
