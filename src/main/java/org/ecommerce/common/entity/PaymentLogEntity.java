package org.ecommerce.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payment_gateway_logs")
public class PaymentLogEntity
{
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity orderEntity;

    @Column(name = "gateway_name")
    private String gatewayName = "PAYFAST"; // Default for now

    @Column(name = "external_reference")
    private String externalReference; // pf_payment_id from PayFast

    @Column(name = "internal_reference")
    private String internalReference; // Your m_payment_id

    @Column(name = "amount_gross")
    private BigDecimal amountGross;

    @Column(name = "amount_fee")
    private BigDecimal amountFee;

    @Column(name = "amount_net")
    private BigDecimal amountNet;

    private String status; // COMPLETE, FAILED, PENDING

    @Column(name = "raw_response")
    private String rawResponse; // The full POST body for auditing

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
