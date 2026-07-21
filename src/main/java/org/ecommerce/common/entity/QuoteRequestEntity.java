package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;
import org.ecommerce.common.enums.QuoteRequestStatusEn;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quote_requests")
public class QuoteRequestEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "name", length = 120, nullable = false)
    public String name;

    @Column(name = "email", length = 254, nullable = false)
    public String email;

    @Column(name = "phone", length = 40)
    public String phone;

    @Column(name = "company", length = 160)
    public String company;

    @Column(name = "message", columnDefinition = "TEXT")
    public String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    public QuoteRequestStatusEn status = QuoteRequestStatusEn.NEW;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "status_changed_at")
    public Instant statusChangedAt;

    @OneToMany(mappedBy = "quoteRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<QuoteRequestItemEntity> items = new ArrayList<>();
}
