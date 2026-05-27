package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.ecommerce.common.enums.ContactRoleEn;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * A contact person associated with a customer account
 * (maps to the {@code customer_contacts} table).
 * Primarily used for wholesale accounts to track finance, buyer, and manager contacts.
 */
@Entity
@Table(name = "customer_contacts")
public class CustomerContactEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    public CustomerEntity customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_role", length = 50)
    public ContactRoleEn contactRole;

    @Column(name = "full_name")
    public String fullName;

    public String email;

    public String phone;
}

