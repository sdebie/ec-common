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
     * REFUNDED is a bookkeeping marker: it records that a refund was made
     * outside the system. It moves no money — there is no gateway refund
     * integration — and does not restore stock.
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
}
