package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.ContactRoleEn;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * A contact person associated with a customer account
 * (maps to the {@code customer_contacts} table).
 * Primarily used for wholesale accounts to track finance, buyer, and manager contacts.
 */
@Getter
@Setter
@Entity
@Table(name = "customer_contacts")
public class CustomerContactEntity extends PanacheEntityBase
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
    @Column(name = "contact_role", length = 50)
    private ContactRoleEn contactRole;

    @Column(name = "full_name")
    private String fullName;

    private String email;

    private String phone;
}

