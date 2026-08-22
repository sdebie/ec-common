package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payment_gateway_logs")
public class PaymentLogEntity extends PanacheEntityBase
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

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    private void setUpdatedAt()
    {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Records a payment-gateway callback. {@code order} may be null when the
     * callback's own order reference can't be resolved to a real order — the row
     * is still kept for the raw audit trail, just unlinked.
     * <p>
     * Caller must already hold a transaction; the row is persisted immediately.
     */
    public static PaymentLogEntity record(OrderEntity order, String gatewayName, String internalReference,
                                           String externalReference, BigDecimal amountGross, String status,
                                           String rawResponse)
    {
        PaymentLogEntity log = new PaymentLogEntity();
        log.setOrderEntity(order);
        log.setGatewayName(gatewayName);
        log.setInternalReference(internalReference);
        log.setExternalReference(externalReference);
        log.setAmountGross(amountGross);
        log.setStatus(status);
        log.setRawResponse(rawResponse);
        log.persist();
        return log;
    }
}
