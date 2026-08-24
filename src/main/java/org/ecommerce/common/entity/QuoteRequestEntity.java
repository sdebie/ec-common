package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.ecommerce.common.enums.QuoteRequestStatusEn;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "quote_requests")
public class QuoteRequestEntity extends PanacheEntityBase
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", length = 120, nullable = false)
    private String name;

    @Column(name = "email", length = 254, nullable = false)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "company", length = 160)
    private String company;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private QuoteRequestStatusEn status = QuoteRequestStatusEn.NEW;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "status_changed_at")
    private Instant statusChangedAt;

    @Column(name = "quoted_amount", precision = 12, scale = 2)
    private BigDecimal quotedAmount;

    @Column(name = "quoted_notes", columnDefinition = "TEXT")
    private String quotedNotes;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "quoted_by", referencedColumnName = "id", nullable = true)
    private StaffUserEntity quotedBy;

    @OneToMany(mappedBy = "quoteRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteRequestItemEntity> items = new ArrayList<>();
}
