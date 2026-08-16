package org.ecommerce.common.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Where an order's goods are — the other of the two questions staff ask of a status.
 * <p>
 * Derived from {@link OrderStatusEn}, never stored. See {@link PaymentState} for the
 * limitation the two share once an order ends.
 *
 * @see OrderStatusEn#fulfilmentState()
 */
public enum FulfilmentState
{
    /** Nobody has started picking it — including a paid order still in the queue. */
    NOT_STARTED,

    /** Being picked and packed. */
    PROCESSING,

    /** Packed and waiting, for a courier or for the shopper. */
    READY,

    /** With the courier. */
    IN_TRANSIT,

    /** The courier could not deliver it, or brought it back. Needs somebody. */
    PROBLEM,

    /**
     * Finished — not necessarily successfully. A refund is only reachable once fulfilment
     * is over, so a refunded order reports this whether its goods were delivered,
     * collected, or returned.
     */
    COMPLETED,

    /** The order ended before its goods went anywhere. */
    CANCELLED;

    private static final Map<FulfilmentState, Set<OrderStatusEn>> BY_STATE = index();

    /**
     * The statuses reporting this state, for turning a filter back into a query.
     * <p>
     * Built once by asking every constant rather than listed by hand: a second list would
     * be a second place to forget a new status, and the two would drift silently.
     */
    public static Set<OrderStatusEn> statusesFor(FulfilmentState state)
    {
        return BY_STATE.getOrDefault(state, Set.of());
    }

    private static Map<FulfilmentState, Set<OrderStatusEn>> index()
    {
        Map<FulfilmentState, Set<OrderStatusEn>> byState = new EnumMap<>(FulfilmentState.class);
        for (FulfilmentState state : values()) {
            byState.put(state, Collections.unmodifiableSet(
                    Arrays.stream(OrderStatusEn.values())
                            .filter(status -> status.fulfilmentState() == state)
                            .collect(() -> EnumSet.noneOf(OrderStatusEn.class), Set::add, Set::addAll)));
        }
        return Collections.unmodifiableMap(byState);
    }
}
