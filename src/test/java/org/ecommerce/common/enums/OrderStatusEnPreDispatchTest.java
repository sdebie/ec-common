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

    /**
     * The other half of the drift tripwire for a rule that exists in two languages.
     * <p>
     * The frontend's {@code defaultRestockForStatus} asserts this identical equivalence
     * against {@code getAvailableTransitions}, which mirrors {@link
     * OrderStatusEn#allowedTransitions()}. Pinning each side to its own transition map
     * means the pre-dispatch rule cannot be changed on one side alone without a test
     * failing somewhere.
     * <p>
     * If pre-dispatch and cancellable ever genuinely need to differ, this failing is the
     * signal to build a real cross-language sync for the refund default — not to relax
     * the assertion.
     */
    @Test
    @DisplayName("pre-dispatch is exactly the set of statuses that admit CANCELLED, in both directions")
    void isPreDispatch_matchesCancellability()
    {
        for (OrderStatusEn status : OrderStatusEn.values()) {
            assertEquals(status.canTransitionTo(OrderStatusEn.CANCELLED), status.isPreDispatch(),
                    status + " disagrees between isPreDispatch() and its CANCELLED transition; the frontend "
                            + "refund default is pinned to the same equivalence and would now diverge");
        }
    }
}
