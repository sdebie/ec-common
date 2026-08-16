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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Which statuses email the shopper, pinned one by one.
 * <p>
 * Both mistakes are expensive and neither is loud. A missing email leaves someone
 * wondering where their order went — and for the statuses that are waiting on them, it
 * loses the sale outright. A spurious one trains them to ignore the whole channel, so the
 * important emails stop being read either.
 * <p>
 * Silence is a legitimate answer for the internal steps and is asserted just as
 * deliberately as the rest.
 */
class OrderStatusEnNotificationTest
{
    private static final Map<OrderStatusEn, CustomerNotification> EXPECTED =
            new EnumMap<>(OrderStatusEn.class);

    static {
        // Waiting on the shopper — the ones that lose the sale if they go unsent.
        EXPECTED.put(OrderStatusEn.PENDING_PAYMENT, CustomerNotification.ACTION_REQUIRED);
        EXPECTED.put(OrderStatusEn.IN_STORE_PAYMENT, CustomerNotification.ACTION_REQUIRED);
        EXPECTED.put(OrderStatusEn.PAYMENT_FAILED, CustomerNotification.ACTION_REQUIRED);

        EXPECTED.put(OrderStatusEn.PAID, CustomerNotification.CONFIRMED);
        EXPECTED.put(OrderStatusEn.READY_FOR_COLLECTION, CustomerNotification.READY_FOR_COLLECTION);
        EXPECTED.put(OrderStatusEn.IN_TRANSIT, CustomerNotification.IN_TRANSIT);
        EXPECTED.put(OrderStatusEn.DELIVERED, CustomerNotification.COMPLETED);
        EXPECTED.put(OrderStatusEn.COLLECTED, CustomerNotification.COMPLETED);
        EXPECTED.put(OrderStatusEn.DELIVERY_FAILED, CustomerNotification.DELIVERY_PROBLEM);
        EXPECTED.put(OrderStatusEn.RETURNED_TO_ORIGIN, CustomerNotification.DELIVERY_PROBLEM);

        EXPECTED.put(OrderStatusEn.USER_CANCELED, CustomerNotification.ENDED);
        EXPECTED.put(OrderStatusEn.ADMIN_CANCELED, CustomerNotification.ENDED);
        EXPECTED.put(OrderStatusEn.SYSTEM_CANCELED, CustomerNotification.ENDED);
        EXPECTED.put(OrderStatusEn.CANCELLED, CustomerNotification.ENDED);
        EXPECTED.put(OrderStatusEn.FAILED, CustomerNotification.ENDED);

        EXPECTED.put(OrderStatusEn.REFUNDED, CustomerNotification.REFUNDED);
        EXPECTED.put(OrderStatusEn.PARTIALLY_REFUNDED, CustomerNotification.REFUNDED);

        // Deliberately silent, each for its own reason.
        EXPECTED.put(OrderStatusEn.CREATED, CustomerNotification.NONE);
        EXPECTED.put(OrderStatusEn.PROCESSING, CustomerNotification.NONE);
        EXPECTED.put(OrderStatusEn.READY_TO_SHIP, CustomerNotification.NONE);
        EXPECTED.put(OrderStatusEn.PENDING, CustomerNotification.NONE);
    }

    @ParameterizedTest
    @EnumSource(OrderStatusEn.class)
    @DisplayName("every status states which email it sends, or that it sends none")
    void customerNotification_isPinnedForEveryConstant(OrderStatusEn status)
    {
        assertEquals(EXPECTED.get(status), status.customerNotification(),
                "changing what " + status + " sends changes what a shopper hears from this store");
    }

    /**
     * CREATED comes before payment, so emailing there confirms orders that go on to fail
     * or time out. PROCESSING and READY_TO_SHIP are warehouse steps a shopper already
     * assumes are happening. FAILED is deliberately NOT here: the word is too vague to put
     * in front of a shopper, but an order that ends in it has still ended, so it borrows
     * the ordinary cancellation email rather than saying nothing.
     */
    @Test
    @DisplayName("the silent statuses are exactly the three internal ones, plus the legacy default")
    void silenceIsDeliberate()
    {
        Set<OrderStatusEn> silent = EnumSet.noneOf(OrderStatusEn.class);
        for (OrderStatusEn status : OrderStatusEn.values()) {
            if (status.customerNotification() == CustomerNotification.NONE) {
                silent.add(status);
            }
        }

        assertEquals(EnumSet.of(OrderStatusEn.CREATED, OrderStatusEn.PROCESSING,
                        OrderStatusEn.READY_TO_SHIP, OrderStatusEn.PENDING),
                silent);
    }

    /**
     * The rule that matters most: if an order stops moving and nobody tells the shopper,
     * they are left waiting for something that will never happen. Every terminal status a
     * real order can reach has to say something.
     */
    @Test
    @DisplayName("no order ends in silence")
    void everyReachableTerminalStatusNotifies()
    {
        for (OrderStatusEn status : OrderStatusEn.values()) {
            boolean terminal = status.allowedTransitions().isEmpty()
                    && status.systemTransitions().isEmpty();
            boolean reachable = isReachable(status);
            if (!terminal || !reachable) {
                continue;
            }

            assertNotEquals(CustomerNotification.NONE, status.customerNotification(),
                    status + " ends an order without telling the shopper anything; they are left "
                            + "waiting for an order that is never coming");
        }
    }

    /**
     * The counterpart: a status that is waiting on the shopper must always reach them,
     * because the order cannot progress until they act.
     */
    @Test
    @DisplayName("every status that needs the shopper to act tells them so")
    void statusesAwaitingTheShopperAlwaysNotify()
    {
        for (OrderStatusEn status : Set.of(OrderStatusEn.PENDING_PAYMENT,
                OrderStatusEn.IN_STORE_PAYMENT, OrderStatusEn.PAYMENT_FAILED)) {
            assertEquals(CustomerNotification.ACTION_REQUIRED, status.customerNotification(),
                    status + " is waiting on the shopper but does not ask them for anything");
        }
    }

    @Test
    @DisplayName("the spec accounts for every constant, so adding one cannot go untested")
    void everyConstantIsSpecified()
    {
        for (OrderStatusEn status : OrderStatusEn.values()) {
            assertTrue(EXPECTED.containsKey(status),
                    status + " has no expected notification in this spec");
        }
    }

    private static boolean isReachable(OrderStatusEn target)
    {
        for (OrderStatusEn source : OrderStatusEn.values()) {
            if (source.canTransitionTo(target) || source.canSystemTransitionTo(target)) {
                return true;
            }
        }
        return false;
    }
}
