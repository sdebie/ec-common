package org.ecommerce.common.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Where an order's money is — one of the two questions staff ask of a status.
 * <p>
 * Derived from {@link OrderStatusEn}, never stored. That is a deliberate limitation with a
 * known edge: once an order ends, one status stops carrying enough to answer the money and
 * the goods questions independently, which is why {@link #CANCELLED} says only that the
 * order ended. Answering it properly wants a second column on the order.
 *
 * @see OrderStatusEn#paymentState()
 */
public enum PaymentState
{
    /** Nothing received yet — the order is waiting on the shopper. */
    AWAITING,

    /** The gateway declined. The reservation is still held for a retry. */
    FAILED,

    /** Received and still held by the store. */
    PAID,

    /** Some of it has gone back. */
    PARTIALLY_REFUNDED,

    /** All of it has gone back. */
    REFUNDED,

    /**
     * The order ended before the money question resolved. Whether it had been paid first
     * is not knowable from the status alone — a cancellation records that the order is
     * over, not what state it was in when it ended.
     */
    CANCELLED;

    private static final Map<PaymentState, Set<OrderStatusEn>> BY_STATE = index();

    /**
     * The statuses reporting this state, for turning a filter back into a query.
     * <p>
     * Built once by asking every constant rather than listed by hand: a second list would
     * be a second place to forget a new status, and the two would drift silently.
     */
    public static Set<OrderStatusEn> statusesFor(PaymentState state)
    {
        return BY_STATE.getOrDefault(state, Set.of());
    }

    private static Map<PaymentState, Set<OrderStatusEn>> index()
    {
        Map<PaymentState, Set<OrderStatusEn>> byState = new EnumMap<>(PaymentState.class);
        for (PaymentState state : values()) {
            byState.put(state, Collections.unmodifiableSet(
                    Arrays.stream(OrderStatusEn.values())
                            .filter(status -> status.paymentState() == state)
                            .collect(() -> EnumSet.noneOf(OrderStatusEn.class), Set::add, Set::addAll)));
        }
        return Collections.unmodifiableMap(byState);
    }
}
