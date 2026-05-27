package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;
import org.ecommerce.common.enums.WholesaleApplicationStatusEn;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wholesale_applications")
public class WholesaleApplicationEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "account_email", nullable = false, unique = true)
    public String accountEmail;

    @Column(name = "first_name", nullable = false)
    public String firstName;

    @Column(name = "last_name")
    public String lastName;

    @Column(name = "phone")
    public String phone;

    @Column(name = "company_name", nullable = false)
    public String companyName;

    @Column(name = "vat_number")
    public String vatNumber;

    @Column(name = "reg_number")
    public String regNumber;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    public WholesaleApplicationStatusEn status = WholesaleApplicationStatusEn.PENDING;

    @Column(name = "notes")
    public String notes;

    @Column(name = "created_at")
    public OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "processed_at")
    public OffsetDateTime processedAt;

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

    @Column(name = "postal_province")
    public String postalProvince;

    @Column(name = "postal_city")
    public String postalCity;

    @Column(name = "postal_postal_code")
    public String postalPostalCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", unique = true)
    public CustomerEntity customer;
}

