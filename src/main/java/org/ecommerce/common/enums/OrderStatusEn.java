package org.ecommerce.common.enums;

import java.util.Set;

public enum OrderStatusEn {
    CREATED,
    PENDING,
    PAID,
    IN_STORE_PAYMENT,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED,
    FAILED,
    SYSTEM_CANCELED,
    REFUNDED;

    /**
     * The statuses staff may move an order in this status to by hand.
     * <p>
     * This governs the staff-driven transition only. The automated writers —
     * checkout (→ CREATED), the PayFast ITN handler (→ PAID) and the
     * abandoned-order release job (→ SYSTEM_CANCELED) — own their own
     * transitions and are deliberately not bound by this map. The admin UI
     * mirrors it exactly so the actions it offers are the actions the server
     * will accept.
     * <p>
     * CANCELLED restores the order's stock. It is deliberately reachable only
     * from pre-dispatch statuses — IN_TRANSIT leads to DELIVERED alone — so a
     * cancelled order's goods have never left, and the stock consumed at
     * CREATED must go back. Admitting CANCELLED from a post-dispatch status
     * would break that, since the goods would already be gone.
     * <p>
     * REFUNDED moves no money — there is no gateway refund integration, so it
     * records a refund made outside the system. It is reachable from both
     * sides of dispatch, so unlike CANCELLED it cannot infer what became of
     * the goods: whether its items return to stock is supplied by the staff
     * member issuing it, defaulted from {@link #isPreDispatch()} and never
     * decided from status alone.
     * <p>
     * CREATED accepts IN_STORE_PAYMENT because a staff member marking an
     * unpaid order as payable in store is the only thing that ever sets that
     * status, and it sends the shopper their confirmation email.
     */
    public Set<OrderStatusEn> allowedTransitions()
    {
        return switch (this) {
            case CREATED, PENDING -> Set.of(IN_STORE_PAYMENT, CANCELLED);
            case PAID, IN_STORE_PAYMENT -> Set.of(IN_TRANSIT, CANCELLED, REFUNDED);
            case IN_TRANSIT -> Set.of(DELIVERED);
            case DELIVERED -> Set.of(REFUNDED);
            case CANCELLED, FAILED, SYSTEM_CANCELED, REFUNDED -> Set.of();
        };
    }

    public boolean canTransitionTo(OrderStatusEn target)
    {
        return target != null && allowedTransitions().contains(target);
    }

    /**
     * Whether an order in this status still holds goods that have not left.
     * <p>
     * This is the <em>default</em> offered to a staff member deciding whether a
     * refund returns its items to stock — never the server's authority. Status
     * lags physical reality: an order dispatched without being moved to
     * IN_TRANSIT still reads PAID, and inferring from that would return stock
     * for goods already gone, overstating what is available to sell. Only the
     * person issuing the refund knows, so they are asked and their answer is
     * obeyed. Do not collapse this into an inference at the transition.
     * <p>
     * Exhaustive by constant rather than by a default branch, so adding a status
     * forces this question to be answered for it.
     */
    public boolean isPreDispatch()
    {
        return switch (this) {
            case CREATED, PENDING, PAID, IN_STORE_PAYMENT -> true;
            case IN_TRANSIT, DELIVERED, CANCELLED, FAILED, SYSTEM_CANCELED, REFUNDED -> false;
        };
    }
}
