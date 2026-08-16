package org.ecommerce.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The order workflow, written as the specification rather than derived from the
 * code. Every status and every transition is pinned here; nothing about the
 * lifecycle is allowed to be true only in an implementation.
 * <p>
 * Two rules drive most of this suite:
 * <ol>
 *   <li><b>No step may be skipped.</b> An order reaches DELIVERED by going
 *       through PROCESSING, READY_TO_SHIP and IN_TRANSIT, and COLLECTED by
 *       going through PROCESSING and READY_FOR_COLLECTION. Any shortcut is a
 *       fulfilment step nobody performed.</li>
 *   <li><b>A terminal status is terminal.</b> An order that has been cancelled,
 *       refunded or failed cannot be moved anywhere, by anyone, ever.</li>
 * </ol>
 */
class OrderWorkflowTest
{
    /** The delivery path, in order. Each entry may reach only the next one. */
    private static final List<OrderStatusEn> ONLINE_PATH = List.of(
            OrderStatusEn.CREATED,
            OrderStatusEn.PENDING_PAYMENT,
            OrderStatusEn.PAID,
            OrderStatusEn.PROCESSING,
            OrderStatusEn.READY_TO_SHIP,
            OrderStatusEn.IN_TRANSIT,
            OrderStatusEn.DELIVERED);

    /** The collection path, in order. */
    private static final List<OrderStatusEn> IN_STORE_PATH = List.of(
            OrderStatusEn.CREATED,
            OrderStatusEn.IN_STORE_PAYMENT,
            OrderStatusEn.PAID,
            OrderStatusEn.PROCESSING,
            OrderStatusEn.READY_FOR_COLLECTION,
            OrderStatusEn.COLLECTED);

    /** Nothing leaves these, by staff action or otherwise. */
    private static final Set<OrderStatusEn> TERMINAL = EnumSet.of(
            OrderStatusEn.REFUNDED,
            OrderStatusEn.USER_CANCELED,
            OrderStatusEn.ADMIN_CANCELED,
            OrderStatusEn.SYSTEM_CANCELED,
            OrderStatusEn.FAILED,
            // Legacy values kept only so historic rows still parse.
            OrderStatusEn.PENDING,
            OrderStatusEn.CANCELLED);

    /**
     * An order in one of these still holds goods that have not left the shop, so
     * cancelling it must return their stock.
     * <p>
     * PENDING is deliberately absent. It is the entity's field default rather than
     * a state any order is put into, and no transition reaches it — calling it
     * pre-dispatch would make it a terminal status holding goods forever, which is
     * what {@code noTerminalStatusStrandsStock} exists to forbid.
     */
    private static final Set<OrderStatusEn> PRE_DISPATCH = EnumSet.of(
            OrderStatusEn.CREATED,
            OrderStatusEn.PENDING_PAYMENT,
            OrderStatusEn.PAYMENT_FAILED,
            OrderStatusEn.IN_STORE_PAYMENT,
            OrderStatusEn.PAID,
            OrderStatusEn.PROCESSING,
            OrderStatusEn.READY_TO_SHIP,
            OrderStatusEn.READY_FOR_COLLECTION);

    /** Staff may cancel an order right up until its goods leave. */
    private static final Set<OrderStatusEn> CANCELLABLE = PRE_DISPATCH;

    private static final Map<OrderStatusEn, Set<OrderStatusEn>> STAFF = new EnumMap<>(OrderStatusEn.class);
    private static final Map<OrderStatusEn, Set<OrderStatusEn>> SYSTEM = new EnumMap<>(OrderStatusEn.class);

    static {
        // Forward step, then the cancels every pre-dispatch status carries.
        staff(OrderStatusEn.CREATED, OrderStatusEn.IN_STORE_PAYMENT);
        staff(OrderStatusEn.PENDING_PAYMENT, OrderStatusEn.PAID);
        staff(OrderStatusEn.PAYMENT_FAILED);
        staff(OrderStatusEn.IN_STORE_PAYMENT, OrderStatusEn.PAID);
        staff(OrderStatusEn.PAID, OrderStatusEn.PROCESSING);
        staff(OrderStatusEn.PROCESSING, OrderStatusEn.READY_TO_SHIP, OrderStatusEn.READY_FOR_COLLECTION);
        staff(OrderStatusEn.READY_TO_SHIP, OrderStatusEn.IN_TRANSIT);
        staff(OrderStatusEn.READY_FOR_COLLECTION, OrderStatusEn.COLLECTED);

        // Past dispatch: no cancelling, because the goods are already out.
        STAFF.put(OrderStatusEn.IN_TRANSIT, Set.of(OrderStatusEn.DELIVERED, OrderStatusEn.DELIVERY_FAILED));
        STAFF.put(OrderStatusEn.DELIVERY_FAILED,
                Set.of(OrderStatusEn.IN_TRANSIT, OrderStatusEn.RETURNED_TO_ORIGIN));
        STAFF.put(OrderStatusEn.DELIVERED, refunds());
        STAFF.put(OrderStatusEn.COLLECTED, refunds());
        STAFF.put(OrderStatusEn.RETURNED_TO_ORIGIN, refunds());
        STAFF.put(OrderStatusEn.PARTIALLY_REFUNDED, Set.of(OrderStatusEn.REFUNDED));
        for (OrderStatusEn terminal : TERMINAL) {
            STAFF.put(terminal, Set.of());
        }

        SYSTEM.put(OrderStatusEn.CREATED, Set.of(
                OrderStatusEn.PENDING_PAYMENT,
                OrderStatusEn.IN_STORE_PAYMENT,
                OrderStatusEn.SYSTEM_CANCELED,
                OrderStatusEn.FAILED));
        SYSTEM.put(OrderStatusEn.PENDING_PAYMENT, Set.of(
                OrderStatusEn.PAID,
                OrderStatusEn.PAYMENT_FAILED,
                OrderStatusEn.SYSTEM_CANCELED,
                OrderStatusEn.FAILED));
        SYSTEM.put(OrderStatusEn.PAYMENT_FAILED, Set.of(
                OrderStatusEn.PENDING_PAYMENT,
                OrderStatusEn.SYSTEM_CANCELED));
        SYSTEM.put(OrderStatusEn.IN_TRANSIT, Set.of(
                OrderStatusEn.DELIVERED,
                OrderStatusEn.DELIVERY_FAILED));
        for (OrderStatusEn status : OrderStatusEn.values()) {
            SYSTEM.putIfAbsent(status, Set.of());
        }
    }

    private static Set<OrderStatusEn> refunds()
    {
        return Set.of(OrderStatusEn.REFUNDED, OrderStatusEn.PARTIALLY_REFUNDED);
    }

    private static void staff(OrderStatusEn from, OrderStatusEn... forward)
    {
        Set<OrderStatusEn> targets = new LinkedHashSet<>(List.of(forward));
        targets.add(OrderStatusEn.USER_CANCELED);
        targets.add(OrderStatusEn.ADMIN_CANCELED);
        STAFF.put(from, targets);
    }

    @Nested
    @DisplayName("the map is complete and pinned")
    class Completeness
    {
        @ParameterizedTest
        @EnumSource(OrderStatusEn.class)
        @DisplayName("every status offers exactly the staff transitions the workflow specifies")
        void staffTransitions_arePinned(OrderStatusEn status)
        {
            assertEquals(STAFF.get(status), status.allowedTransitions(),
                    "staff transitions for " + status + " drifted from the specified workflow");
        }

        @ParameterizedTest
        @EnumSource(OrderStatusEn.class)
        @DisplayName("every status offers exactly the automated transitions the workflow specifies")
        void systemTransitions_arePinned(OrderStatusEn status)
        {
            assertEquals(SYSTEM.get(status), status.systemTransitions(),
                    "automated transitions for " + status + " drifted from the specified workflow");
        }

        @Test
        @DisplayName("the spec accounts for every constant, so adding one cannot go untested")
        void everyConstantIsSpecified()
        {
            for (OrderStatusEn status : OrderStatusEn.values()) {
                assertTrue(STAFF.containsKey(status), status + " has no staff transitions in this spec");
                assertTrue(SYSTEM.containsKey(status), status + " has no system transitions in this spec");
            }
        }
    }

    @Nested
    @DisplayName("a terminal status is terminal")
    class Terminal
    {
        @Test
        @DisplayName("nothing leaves a terminal status, by staff action or by the platform")
        void terminalStatusesHaveNoExit()
        {
            for (OrderStatusEn status : TERMINAL) {
                assertTrue(status.allowedTransitions().isEmpty(),
                        status + " is terminal but staff can still move it");
                assertTrue(status.systemTransitions().isEmpty(),
                        status + " is terminal but the platform can still move it");
            }
        }

        @Test
        @DisplayName("no transition anywhere leads back out of a terminal status")
        void terminalStatusesAreNeverReopened()
        {
            for (OrderStatusEn source : TERMINAL) {
                for (OrderStatusEn target : OrderStatusEn.values()) {
                    assertFalse(source.canTransitionTo(target),
                            source + " → " + target + " would reopen a closed order");
                    assertFalse(source.canSystemTransitionTo(target),
                            source + " → " + target + " would reopen a closed order");
                }
            }
        }
    }

    @Nested
    @DisplayName("the workflow runs in order and no step can be skipped")
    class NoSkipping
    {
        @Test
        @DisplayName("the delivery path walks end to end, one step at a time")
        void onlinePathIsWalkable()
        {
            assertPathIsWalkable(ONLINE_PATH);
        }

        @Test
        @DisplayName("the collection path walks end to end, one step at a time")
        void inStorePathIsWalkable()
        {
            assertPathIsWalkable(IN_STORE_PATH);
        }

        /**
         * The heart of the suite. For each path, every non-adjacent forward pair is
         * checked to be unreachable — so PAID cannot jump to IN_TRANSIT, PROCESSING
         * cannot jump to DELIVERED, and CREATED cannot jump to PAID. Each of those
         * would record a fulfilment step that nobody actually performed.
         */
        @Test
        @DisplayName("no forward jump skips a step, on either path")
        void forwardJumpsAreRefused()
        {
            for (List<OrderStatusEn> path : List.of(ONLINE_PATH, IN_STORE_PATH)) {
                for (int from = 0; from < path.size(); from++) {
                    for (int to = from + 2; to < path.size(); to++) {
                        OrderStatusEn source = path.get(from);
                        OrderStatusEn target = path.get(to);
                        assertFalse(source.canTransitionTo(target) || source.canSystemTransitionTo(target),
                                source + " → " + target + " skips "
                                        + path.subList(from + 1, to)
                                        + "; every step between them would be recorded as done without being done");
                    }
                }
            }
        }

        @Test
        @DisplayName("the workflow never runs backwards")
        void backwardsJumpsAreRefused()
        {
            for (List<OrderStatusEn> path : List.of(ONLINE_PATH, IN_STORE_PATH)) {
                for (int from = 0; from < path.size(); from++) {
                    for (int to = 0; to < from; to++) {
                        OrderStatusEn source = path.get(from);
                        OrderStatusEn target = path.get(to);
                        assertFalse(source.canTransitionTo(target) || source.canSystemTransitionTo(target),
                                source + " → " + target + " moves the order backwards through the workflow");
                    }
                }
            }
        }

        /**
         * The two paths must not become interchangeable halfway through. An order
         * being couriered cannot become ready for collection, and one waiting at the
         * counter cannot be handed to a courier.
         */
        @Test
        @DisplayName("the delivery and collection paths do not cross after they fork")
        void pathsDoNotCross()
        {
            assertFalse(OrderStatusEn.READY_TO_SHIP.canTransitionTo(OrderStatusEn.COLLECTED));
            assertFalse(OrderStatusEn.READY_TO_SHIP.canTransitionTo(OrderStatusEn.READY_FOR_COLLECTION));
            assertFalse(OrderStatusEn.READY_FOR_COLLECTION.canTransitionTo(OrderStatusEn.IN_TRANSIT));
            assertFalse(OrderStatusEn.READY_FOR_COLLECTION.canTransitionTo(OrderStatusEn.READY_TO_SHIP));
            assertFalse(OrderStatusEn.IN_TRANSIT.canTransitionTo(OrderStatusEn.COLLECTED));
            assertFalse(OrderStatusEn.READY_FOR_COLLECTION.canTransitionTo(OrderStatusEn.DELIVERED));
        }

        @Test
        @DisplayName("payment cannot be skipped — nothing reaches fulfilment without going through PAID")
        void fulfilmentRequiresPayment()
        {
            for (OrderStatusEn source : OrderStatusEn.values()) {
                if (source == OrderStatusEn.PAID) {
                    continue;
                }
                assertFalse(source.canTransitionTo(OrderStatusEn.PROCESSING)
                                || source.canSystemTransitionTo(OrderStatusEn.PROCESSING),
                        source + " reaches PROCESSING without the order having been paid");
            }
        }

        private void assertPathIsWalkable(List<OrderStatusEn> path)
        {
            List<String> walked = new ArrayList<>();
            for (int i = 0; i < path.size() - 1; i++) {
                OrderStatusEn from = path.get(i);
                OrderStatusEn to = path.get(i + 1);
                assertTrue(from.canTransitionTo(to) || from.canSystemTransitionTo(to),
                        "the workflow is broken at " + from + " → " + to
                                + "; walked so far: " + walked);
                walked.add(from + " → " + to);
            }
        }
    }

    @Nested
    @DisplayName("cancellation")
    class Cancellation
    {
        @Test
        @DisplayName("an order can be cancelled at any point before its goods leave, and never after")
        void cancellableExactlyWhilePreDispatch()
        {
            for (OrderStatusEn status : OrderStatusEn.values()) {
                boolean offersCancel = status.canTransitionTo(OrderStatusEn.USER_CANCELED)
                        || status.canTransitionTo(OrderStatusEn.ADMIN_CANCELED);
                assertEquals(CANCELLABLE.contains(status), offersCancel,
                        status + " disagrees with the rule that cancellation is offered exactly while the "
                                + "goods are still in the shop");
            }
        }

        @Test
        @DisplayName("both manual cancellations are offered together — who cancelled is not the order's business")
        void userAndAdminCancelTravelTogether()
        {
            for (OrderStatusEn status : OrderStatusEn.values()) {
                assertEquals(status.canTransitionTo(OrderStatusEn.USER_CANCELED),
                        status.canTransitionTo(OrderStatusEn.ADMIN_CANCELED),
                        status + " offers one manual cancellation but not the other");
            }
        }

        @Test
        @DisplayName("the platform never cancels an order the shopper has committed to paying for in store")
        void sweepNeverTouchesInStorePayment()
        {
            assertFalse(OrderStatusEn.IN_STORE_PAYMENT.canSystemTransitionTo(OrderStatusEn.SYSTEM_CANCELED),
                    "a shopper coming to the shop to pay has not abandoned anything");
        }
    }

    @Nested
    @DisplayName("stock")
    class Stock
    {
        @ParameterizedTest
        @EnumSource(OrderStatusEn.class)
        @DisplayName("goods are counted as still in the shop for exactly the pre-dispatch statuses")
        void isPreDispatch_isPinned(OrderStatusEn status)
        {
            assertEquals(PRE_DISPATCH.contains(status), status.isPreDispatch(),
                    "reclassifying " + status + " changes whether cancelling it returns stock");
        }

        /**
         * Reversing a payment is bookkeeping. Whether the goods came back is a
         * separate physical fact the system does not know, and returning them to
         * sale is a future returns feature — so no refund status moves stock.
         */
        @Test
        @DisplayName("no refund status touches stock")
        void refundsDoNotMoveStock()
        {
            assertEquals(StockEffect.NONE, OrderStatusEn.REFUNDED.stockEffect());
            assertEquals(StockEffect.NONE, OrderStatusEn.PARTIALLY_REFUNDED.stockEffect());
        }

        /**
         * The shopper is told to retry with another payment method. If the failure
         * released their items, there would be nothing left to retry against.
         */
        @Test
        @DisplayName("a failed payment keeps the reservation so the shopper can retry")
        void paymentFailedHoldsStockForRetry()
        {
            assertEquals(StockEffect.NONE, OrderStatusEn.PAYMENT_FAILED.stockEffect());
            assertTrue(OrderStatusEn.PAYMENT_FAILED.canSystemTransitionTo(OrderStatusEn.PENDING_PAYMENT),
                    "a shopper must be able to retry a failed payment");
        }

        /**
         * The counterpart of holding stock through a retry: if the shopper never
         * comes back, something has to reclaim it. Any unpaid status that keeps its
         * reservation and is not a terminal state must be reachable by the sweep,
         * or its stock is held forever.
         */
        @Test
        @DisplayName("every unpaid status that holds stock can be reclaimed by the sweep")
        void unpaidHoldingStatusesAreReclaimable()
        {
            Set<OrderStatusEn> expected = EnumSet.of(
                    OrderStatusEn.CREATED,
                    OrderStatusEn.PENDING_PAYMENT,
                    OrderStatusEn.PAYMENT_FAILED);

            for (OrderStatusEn status : OrderStatusEn.values()) {
                assertEquals(expected.contains(status), status.isReclaimableByStockRecovery(),
                        status + " disagrees with the set of statuses the abandoned-order sweep reclaims");
            }
        }

        @Test
        @DisplayName("a reclaimable status can actually be swept, and lands on SYSTEM_CANCELED")
        void reclaimableStatusesCanReachSystemCancelled()
        {
            for (OrderStatusEn status : OrderStatusEn.values()) {
                if (!status.isReclaimableByStockRecovery()) {
                    continue;
                }
                assertTrue(status.canSystemTransitionTo(OrderStatusEn.SYSTEM_CANCELED),
                        status + " is marked reclaimable but the sweep cannot legally move it");
            }
            assertEquals(StockEffect.RESTORE, OrderStatusEn.SYSTEM_CANCELED.stockEffect());
        }

        @Test
        @DisplayName("no status ends an order holding goods that never left")
        void noTerminalStatusStrandsStock()
        {
            for (OrderStatusEn status : OrderStatusEn.values()) {
                boolean terminal = status.allowedTransitions().isEmpty()
                        && status.systemTransitions().isEmpty();
                if (!terminal || status.stockEffect() != StockEffect.NONE) {
                    continue;
                }
                assertFalse(status.isPreDispatch(),
                        status + " ends the order without returning stock, but its goods never left — "
                                + "that stock can never be sold again");
            }
        }
    }
}
