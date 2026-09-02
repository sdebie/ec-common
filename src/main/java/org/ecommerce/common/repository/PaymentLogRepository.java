package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.OrderEntity;
import org.ecommerce.common.entity.PaymentLogEntity;

import java.math.BigDecimal;
import java.util.UUID;

@ApplicationScoped
public class PaymentLogRepository extends BaseRepository<PaymentLogEntity, UUID>
{
    @Override
    protected Class<PaymentLogEntity> getEntityClass()
    {
        return PaymentLogEntity.class;
    }

    /** The most recent payment-gateway log row for an order, or null if it has none. */
    public PaymentLogEntity findLatestByOrderId(UUID orderId)
    {
        return find("select l from PaymentLogEntity l where l.orderEntity.id = ?1 order by l.createdAt desc", orderId).firstResult();
    }

    public PaymentLogEntity record(OrderEntity order, String gatewayName, String internalReference, String externalReference, BigDecimal amountGross, String status, String rawResponse)
    {
        PaymentLogEntity log = new PaymentLogEntity();
        log.setOrderEntity(order);
        log.setGatewayName(gatewayName);
        log.setInternalReference(internalReference);
        log.setExternalReference(externalReference);
        log.setAmountGross(amountGross);
        log.setStatus(status);
        log.setRawResponse(rawResponse);
        persist(log);
        return log;
    }
}
