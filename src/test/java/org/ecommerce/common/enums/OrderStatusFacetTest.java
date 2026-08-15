package org.ecommerce.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An order carries one status, but staff ask two different questions of it: <em>has the
 * money arrived?</em> and <em>where are the goods?</em> These two facets answer them
 * separately so the order list can be filtered on either.
 * <p>
 * Both are derived, not stored. That is a deliberate limitation with a known edge: once an
 * order ends, its single status stops carrying enough to answer both questions
 * independently — a cancelled order does not say whether it had been paid first. Those
 * cases are mapped explicitly below rather than guessed at, and the honest fix is two real
 * columns, which is a much larger change than a filter.
 */
class OrderStatusFacetTest
{
    private static final Map<OrderStatusEn, PaymentState> PAYMENT = new EnumMap<>(OrderStatusEn.class);
    private static final Map<OrderStatusEn, FulfilmentState> FULFILMENT = new EnumMap<>(OrderStatusEn.class);

    static {
        // Money not received yet.
        PAYMENT.put(OrderStatusEn.CREATED, PaymentState.AWAITING);
        PAYMENT.put(OrderStatusEn.PENDING_PAYMENT, PaymentState.AWAITING);
        PAYMENT.put(OrderStatusEn.IN_STORE_PAYMENT, PaymentState.AWAITING);
        PAYMENT.put(OrderStatusEn.PENDING, PaymentState.AWAITING);

        PAYMENT.put(OrderStatusEn.PAYMENT_FAILED, PaymentState.FAILED);

        // Money received and still held by the store.
        PAYMENT.put(OrderStatusEn.PAID, PaymentState.PAID);
        PAYMENT.put(OrderStatusEn.PROCESSING, PaymentState.PAID);
        PAYMENT.put(OrderStatusEn.READY_TO_SHIP, PaymentState.PAID);
        PAYMENT.put(OrderStatusEn.READY_FOR_COLLECTION, PaymentState.PAID);
        PAYMENT.put(OrderStatusEn.IN_TRANSIT, PaymentState.PAID);
        PAYMENT.put(OrderStatusEn.DELIVERY_FAILED, PaymentState.PAID);
        PAYMENT.put(OrderStatusEn.RETURNED_TO_ORIGIN, PaymentState.PAID);
        PAYMENT.put(OrderStatusEn.DELIVERED, PaymentState.PAID);
        PAYMENT.put(OrderStatusEn.COLLECTED, PaymentState.PAID);

        PAYMENT.put(OrderStatusEn.PARTIALLY_REFUNDED, PaymentState.PARTIALLY_REFUNDED);
        PAYMENT.put(OrderStatusEn.REFUNDED, PaymentState.REFUNDED);

        // Ended before the money question resolved. The status alone cannot say whether a
        // cancelled order had been paid first, so it says only that it ended.
        PAYMENT.put(OrderStatusEn.USER_CANCELED, PaymentState.CANCELLED);
        PAYMENT.put(OrderStatusEn.ADMIN_CANCELED, PaymentState.CANCELLED);
        PAYMENT.put(OrderStatusEn.SYSTEM_CANCELED, PaymentState.CANCELLED);
        PAYMENT.put(OrderStatusEn.FAILED, PaymentState.CANCELLED);
        PAYMENT.put(OrderStatusEn.CANCELLED, PaymentState.CANCELLED);

        // Nothing picked yet — including a paid order nobody has started.
        FULFILMENT.put(OrderStatusEn.CREATED, FulfilmentState.NOT_STARTED);
        FULFILMENT.put(OrderStatusEn.PENDING_PAYMENT, FulfilmentState.NOT_STARTED);
        FULFILMENT.put(OrderStatusEn.IN_STORE_PAYMENT, FulfilmentState.NOT_STARTED);
        FULFILMENT.put(OrderStatusEn.PAYMENT_FAILED, FulfilmentState.NOT_STARTED);
        FULFILMENT.put(OrderStatusEn.PAID, FulfilmentState.NOT_STARTED);
        FULFILMENT.put(OrderStatusEn.PENDING, FulfilmentState.NOT_STARTED);

        FULFILMENT.put(OrderStatusEn.PROCESSING, FulfilmentState.PROCESSING);
        FULFILMENT.put(OrderStatusEn.READY_TO_SHIP, FulfilmentState.READY);
        FULFILMENT.put(OrderStatusEn.READY_FOR_COLLECTION, FulfilmentState.READY);
        FULFILMENT.put(OrderStatusEn.IN_TRANSIT, FulfilmentState.IN_TRANSIT);

        FULFILMENT.put(OrderStatusEn.DELIVERY_FAILED, FulfilmentState.PROBLEM);
        FULFILMENT.put(OrderStatusEn.RETURNED_TO_ORIGIN, FulfilmentState.PROBLEM);

        // Finished, not necessarily successful: a refund is only reachable once fulfilment
        // is over, so the attempt is done either way.
        FULFILMENT.put(OrderStatusEn.DELIVERED, FulfilmentState.COMPLETED);
        FULFILMENT.put(OrderStatusEn.COLLECTED, FulfilmentState.COMPLETED);
        FULFILMENT.put(OrderStatusEn.PARTIALLY_REFUNDED, FulfilmentState.COMPLETED);
        FULFILMENT.put(OrderStatusEn.REFUNDED, FulfilmentState.COMPLETED);

        FULFILMENT.put(OrderStatusEn.USER_CANCELED, FulfilmentState.CANCELLED);
        FULFILMENT.put(OrderStatusEn.ADMIN_CANCELED, FulfilmentState.CANCELLED);
        FULFILMENT.put(OrderStatusEn.SYSTEM_CANCELED, FulfilmentState.CANCELLED);
        FULFILMENT.put(OrderStatusEn.FAILED, FulfilmentState.CANCELLED);
        FULFILMENT.put(OrderStatusEn.CANCELLED, FulfilmentState.CANCELLED);
    }

    @ParameterizedTest
    @EnumSource(OrderStatusEn.class)
    @DisplayName("every status states where its money is")
    void paymentState_isPinnedForEveryConstant(OrderStatusEn status)
    {
        assertEquals(PAYMENT.get(status), status.paymentState(),
                "changing the payment facet of " + status + " changes which orders staff can find");
    }

    @ParameterizedTest
    @EnumSource(OrderStatusEn.class)
    @DisplayName("every status states where its goods are")
    void fulfilmentState_isPinnedForEveryConstant(OrderStatusEn status)
    {
        assertEquals(FULFILMENT.get(status), status.fulfilmentState(),
                "changing the fulfilment facet of " + status + " changes which orders staff can find");
    }

    /**
     * The filter resolves a facet back to the statuses that carry it, so the two have to
     * agree in both directions — a status missing from its own facet's set is a status the
     * filter can never return.
     */
    @Test
    @DisplayName("a facet's status set is exactly the statuses that report it")
    void statusSetsRoundTrip()
    {
        for (PaymentState state : PaymentState.values()) {
            Set<OrderStatusEn> expected = EnumSet.noneOf(OrderStatusEn.class);
            for (OrderStatusEn status : OrderStatusEn.values()) {
                if (status.paymentState() == state) {
                    expected.add(status);
                }
            }
            assertEquals(expected, PaymentState.statusesFor(state), state + " round trip");
        }

        for (FulfilmentState state : FulfilmentState.values()) {
            Set<OrderStatusEn> expected = EnumSet.noneOf(OrderStatusEn.class);
            for (OrderStatusEn status : OrderStatusEn.values()) {
                if (status.fulfilmentState() == state) {
                    expected.add(status);
                }
            }
            assertEquals(expected, FulfilmentState.statusesFor(state), state + " round trip");
        }
    }

    @Test
    @DisplayName("no facet value is unreachable — every one has at least one status")
    void everyFacetValueIsReachable()
    {
        for (PaymentState state : PaymentState.values()) {
            assertFalse(PaymentState.statusesFor(state).isEmpty(),
                    state + " is offered as a filter but no status ever reports it");
        }
        for (FulfilmentState state : FulfilmentState.values()) {
            assertFalse(FulfilmentState.statusesFor(state).isEmpty(),
                    state + " is offered as a filter but no status ever reports it");
        }
    }

    /**
     * The two facets are the point: filtering on both has to be able to return something.
     * "Paid but nothing picked yet" is the queue staff work from every morning.
     */
    @Test
    @DisplayName("the facets are genuinely independent — paid orders exist at every fulfilment stage")
    void facetsCombineMeaningfully()
    {
        Set<OrderStatusEn> paid = PaymentState.statusesFor(PaymentState.PAID);

        for (FulfilmentState stage : Set.of(FulfilmentState.NOT_STARTED, FulfilmentState.PROCESSING,
                FulfilmentState.READY, FulfilmentState.IN_TRANSIT, FulfilmentState.COMPLETED,
                FulfilmentState.PROBLEM)) {
            Set<OrderStatusEn> both = EnumSet.copyOf(paid);
            both.retainAll(FulfilmentState.statusesFor(stage));
            assertFalse(both.isEmpty(),
                    "no status is both PAID and " + stage + ", so that filter pair returns nothing");
        }
    }

    @Test
    @DisplayName("an order awaiting payment has never been picked")
    void awaitingPaymentIsNeverInFulfilment()
    {
        for (OrderStatusEn status : PaymentState.statusesFor(PaymentState.AWAITING)) {
            assertTrue(status.fulfilmentState() == FulfilmentState.NOT_STARTED,
                    status + " is awaiting payment but reports fulfilment progress");
        }
    }
}
