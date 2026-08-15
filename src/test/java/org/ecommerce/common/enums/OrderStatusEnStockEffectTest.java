package org.ecommerce.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins {@link OrderStatusEn#stockEffect()} for every constant, and guards the
 * invariant that lets it be keyed on the destination status alone.
 * <p>
 * Stock is consumed once, at CREATED, before payment. Getting this wrong is
 * expensive in both directions and neither is loud: a missing RESTORE strands goods
 * in an order that will never ship, and a spurious one inflates stock and stays
 * invisible until an oversell.
 * <p>
 * {@code OrderWorkflowTest} owns the transition rules these depend on.
 */
class OrderStatusEnStockEffectTest
{
    private static final Map<OrderStatusEn, StockEffect> EXPECTED = new EnumMap<>(OrderStatusEn.class);

    static {
        // Live: the order still holds what it reserved.
        EXPECTED.put(OrderStatusEn.CREATED, StockEffect.NONE);
        EXPECTED.put(OrderStatusEn.PENDING_PAYMENT, StockEffect.NONE);
        EXPECTED.put(OrderStatusEn.IN_STORE_PAYMENT, StockEffect.NONE);
        EXPECTED.put(OrderStatusEn.PAID, StockEffect.NONE);
        EXPECTED.put(OrderStatusEn.PROCESSING, StockEffect.NONE);
        EXPECTED.put(OrderStatusEn.READY_TO_SHIP, StockEffect.NONE);
        EXPECTED.put(OrderStatusEn.READY_FOR_COLLECTION, StockEffect.NONE);
        EXPECTED.put(OrderStatusEn.IN_TRANSIT, StockEffect.NONE);
        EXPECTED.put(OrderStatusEn.DELIVERY_FAILED, StockEffect.NONE);

        // Held deliberately, so the shopper can retry the payment.
        EXPECTED.put(OrderStatusEn.PAYMENT_FAILED, StockEffect.NONE);

        // Gone: the goods left the shop.
        EXPECTED.put(OrderStatusEn.DELIVERED, StockEffect.NONE);
        EXPECTED.put(OrderStatusEn.COLLECTED, StockEffect.NONE);

        // Bookkeeping only — putting returned goods back on sale is the returns feature.
        EXPECTED.put(OrderStatusEn.RETURNED_TO_ORIGIN, StockEffect.NONE);
        EXPECTED.put(OrderStatusEn.REFUNDED, StockEffect.NONE);
        EXPECTED.put(OrderStatusEn.PARTIALLY_REFUNDED, StockEffect.NONE);

        // Ended before the goods left: every one of these gives the stock back.
        EXPECTED.put(OrderStatusEn.USER_CANCELED, StockEffect.RESTORE);
        EXPECTED.put(OrderStatusEn.ADMIN_CANCELED, StockEffect.RESTORE);
        EXPECTED.put(OrderStatusEn.SYSTEM_CANCELED, StockEffect.RESTORE);
        EXPECTED.put(OrderStatusEn.FAILED, StockEffect.RESTORE);

        // Legacy, unreachable. CANCELLED keeps the meaning it had when rows were written.
        EXPECTED.put(OrderStatusEn.CANCELLED, StockEffect.RESTORE);
        EXPECTED.put(OrderStatusEn.PENDING, StockEffect.NONE);
    }

    @ParameterizedTest
    @EnumSource(OrderStatusEn.class)
    @DisplayName("every status states what reaching it does to stock, and states it explicitly")
    void stockEffect_isPinnedForEveryConstant(OrderStatusEn status)
    {
        assertEquals(EXPECTED.get(status), status.stockEffect(),
                "changing the stock effect of " + status + " changes how much of this product is sellable");
    }

    /**
     * The invariant {@code stockEffect()}'s javadoc depends on. Keying the answer on
     * the destination alone is only sound while a status that returns stock cannot be
     * reached from one whose goods have already left — otherwise the same destination
     * would need two different answers, and the one it gives would silently inflate
     * stock for goods that are gone.
     */
    @Test
    @DisplayName("a status that restores stock is reachable only from statuses whose goods never left")
    void restoringStatusesAreReachableOnlyFromPreDispatch()
    {
        for (OrderStatusEn source : OrderStatusEn.values()) {
            for (OrderStatusEn target : source.allowedTransitions()) {
                if (target.stockEffect() != StockEffect.RESTORE) {
                    continue;
                }
                assertTrue(source.isPreDispatch(),
                        source + " → " + target + " returns stock unconditionally, but " + source
                                + " is post-dispatch; stockEffect() would have to take the source status too");
            }
            for (OrderStatusEn target : source.systemTransitions()) {
                if (target.stockEffect() != StockEffect.RESTORE) {
                    continue;
                }
                assertTrue(source.isPreDispatch(),
                        source + " → " + target + " (system) returns stock unconditionally, but " + source
                                + " is post-dispatch");
            }
        }
    }

    /**
     * The other half of the same guarantee: an order that keeps its reservation and
     * is not going anywhere on its own has to be reclaimable, or those goods are held
     * forever by a shopper who walked away.
     */
    @Test
    @DisplayName("an unpaid status that holds stock is either reclaimable by the sweep or a real commitment")
    void unpaidHoldingStatusesAreEitherReclaimableOrCommitted()
    {
        for (OrderStatusEn status : OrderStatusEn.values()) {
            boolean holdsStock = status.stockEffect() == StockEffect.NONE && status.isPreDispatch();
            boolean paid = status == OrderStatusEn.PAID
                    || status == OrderStatusEn.PROCESSING
                    || status == OrderStatusEn.READY_TO_SHIP
                    || status == OrderStatusEn.READY_FOR_COLLECTION;
            if (!holdsStock || paid) {
                continue;
            }

            assertTrue(status.isReclaimableByStockRecovery() || status == OrderStatusEn.IN_STORE_PAYMENT,
                    status + " is unpaid and holding stock, but nothing will ever reclaim it — "
                            + "either the sweep must cover it or it must be a deliberate commitment "
                            + "like IN_STORE_PAYMENT");
        }
    }
}
