package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.WholesaleApplicationStatusEn;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "wholesale_applications")
public class WholesaleApplicationEntity extends PanacheEntityBase
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "applicant_email", nullable = false)
    private String applicantEmail;

    @Column(name = "account_email")
    private String accountEmail;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "trading_name")
    private String tradingName;

    @Column(name = "company_phone")
    private String companyPhone;

    @Column(name = "company_email")
    private String companyEmail;

    @Column(name = "vat_number")
    private String vatNumber;

    @Column(name = "reg_number")
    private String regNumber;

    @Column(name = "finance_contact_name")
    private String financeContactName;

    @Column(name = "finance_contact_email")
    private String financeContactEmail;

    @Column(name = "finance_contact_phone")
    private String financeContactPhone;

    @Column(name = "purchase_order_required")
    private Boolean purchaseOrderRequired = false;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private WholesaleApplicationStatusEn status = WholesaleApplicationStatusEn.PENDING;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "physical_address_line1")
    private String physicalAddressLine1;

    @Column(name = "physical_address_line2")
    private String physicalAddressLine2;

    @Column(name = "physical_suburb")
    private String physicalSuburb;

    @Column(name = "physical_city")
    private String physicalCity;

    @Column(name = "physical_province")
    private String physicalProvince;

    @Column(name = "physical_postal_code")
    private String physicalPostalCode;

    @Column(name = "postal_address_line1")
    private String postalAddressLine1;

    @Column(name = "postal_address_line2")
    private String postalAddressLine2;

    @Column(name = "postal_suburb")
    private String postalSuburb;

    @Column(name = "postal_province")
    private String postalProvince;

    @Column(name = "postal_city")
    private String postalCity;

    @Column(name = "postal_postal_code")
    private String postalPostalCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", unique = true)
    private CustomerEntity customer;
}

