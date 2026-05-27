package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Wholesale-specific profile data (maps to the {@code wholesale_profiles} table).
 * Uses a shared primary key with {@link CustomerEntity} — the PK is also the FK.
 */
@Entity
@Table(name = "wholesale_profiles")
public class WholesaleProfileEntity extends PanacheEntityBase {

    /**
     * Shared primary key — same value as the linked customer's id.
     */
    @Id
    @Column(name = "customer_id", updatable = false, nullable = false)
    public UUID customerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "customer_id")
    public CustomerEntity customer;

    @Column(name = "company_name", nullable = false)
    public String companyName;

    @Column(name = "vat_number", length = 50)
    public String vatNumber;

    /** CIPC Registration number. */
    @Column(name = "reg_number", length = 100)
    public String regNumber;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    public BigDecimal creditLimit = BigDecimal.ZERO;

    /** Net payment terms, e.g. 30 for "Net 30". */
    @Column(name = "payment_terms_days")
    public int paymentTermsDays = 0;

}

