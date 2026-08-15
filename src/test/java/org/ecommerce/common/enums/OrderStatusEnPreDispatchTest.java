package org.ecommerce.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins {@link OrderStatusEn#isPreDispatch()} for every constant, so adding a status
 * cannot quietly inherit an answer. It drives the default offered when a staff member
 * refunds an order, and a wrong default there overstates sellable stock.
 */
class OrderStatusEnPreDispatchTest
{
    private static final Set<OrderStatusEn> PRE_DISPATCH = EnumSet.of(
            OrderStatusEn.CREATED,
            OrderStatusEn.PENDING,
            OrderStatusEn.PAID,
            OrderStatusEn.IN_STORE_PAYMENT);

    @ParameterizedTest
    @EnumSource(OrderStatusEn.class)
    @DisplayName("every status is classified explicitly, and only the four holding undispatched goods are pre-dispatch")
    void isPreDispatch_isPinnedForEveryConstant(OrderStatusEn status)
    {
        assertEquals(PRE_DISPATCH.contains(status), status.isPreDispatch(),
                "reclassifying " + status + " changes the stock default staff are offered on a refund");
    }

    @Test
    @DisplayName("every status REFUNDED is reachable from is classified — the refund default depends on it")
    void everyRefundSourceIsClassified()
    {
        for (OrderStatusEn source : OrderStatusEn.values()) {
            if (!source.canTransitionTo(OrderStatusEn.REFUNDED)) {
                continue;
            }
            // Reaching here at all is the assertion: isPreDispatch() is an exhaustive
            // switch, so a new refund source that nobody classified fails to compile
            // rather than silently defaulting.
            assertTrue(source.isPreDispatch() || source == OrderStatusEn.DELIVERED,
                    source + " can be refunded but is neither pre-dispatch nor the known post-dispatch case");
        }
    }

    @Test
    @DisplayName("a post-dispatch status is never a source for CANCELLED — cancellation restocks unconditionally")
    void cancellationRemainsReachableOnlyFromPreDispatch()
    {
        for (OrderStatusEn source : OrderStatusEn.values()) {
            if (source.canTransitionTo(OrderStatusEn.CANCELLED)) {
                assertTrue(source.isPreDispatch(),
                        source + " admits CANCELLED but is post-dispatch; cancellation restocks without asking, "
                                + "so this would return stock for goods that already shipped");
            }
        }
    }
}
