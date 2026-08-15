package org.ecommerce.common.enums;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The order lifecycle. Two paths, and an order walks one of them a step at a time.
 *
 * <pre>
 * delivery:   CREATED → PENDING_PAYMENT  → PAID → PROCESSING → READY_TO_SHIP        → IN_TRANSIT → DELIVERED
 * collection: CREATED → IN_STORE_PAYMENT → PAID → PROCESSING → READY_FOR_COLLECTION → COLLECTED
 * </pre>
 *
 * <b>No step may be skipped.</b> Every jump past a step would record fulfilment work
 * nobody performed — an order marked IN_TRANSIT that was never picked, or DELIVERED
 * that was never sent. The maps below are exact rather than permissive, and
 * {@code OrderWorkflowTest} pins every pair in both directions.
 * <p>
 * <b>A terminal status is terminal.</b> Once an order is cancelled, refunded or
 * failed, nothing moves it — there is no reopening.
 * <p>
 * Stock is consumed once, at CREATED, before payment. Which statuses give it back is
 * {@link #stockEffect()}; which unpaid ones the sweep may reclaim is
 * {@link #isReclaimableByStockRecovery()}. Neither is decided at a call site.
 */
public enum OrderStatusEn {
    CREATED,
    PENDING_PAYMENT,
    IN_STORE_PAYMENT,
    PAYMENT_FAILED,
    PAID,
    PROCESSING,
    READY_TO_SHIP,
    READY_FOR_COLLECTION,
    IN_TRANSIT,
    DELIVERY_FAILED,
    RETURNED_TO_ORIGIN,
    DELIVERED,
    COLLECTED,
    USER_CANCELED,
    ADMIN_CANCELED,
    SYSTEM_CANCELED,
    FAILED,
    PARTIALLY_REFUNDED,
    REFUNDED,

    /**
     * Legacy. No transition reaches either and neither may be used for new work —
     * they exist so historic rows still parse. PENDING is also the entity's field
     * default; CANCELLED was superseded by {@link #USER_CANCELED} and
     * {@link #ADMIN_CANCELED}, which record who ended the order.
     */
    PENDING,
    CANCELLED;

    /** The manual cancellations, offered together wherever cancelling is possible. */
    private static final Set<OrderStatusEn> CANCELS = Set.of(USER_CANCELED, ADMIN_CANCELED);

    /** Reversing a payment, in full or in part. Neither moves stock. */
    private static final Set<OrderStatusEn> REFUNDS = Set.of(REFUNDED, PARTIALLY_REFUNDED);

    /**
     * The statuses staff may move an order in this status to by hand. The admin UI
     * mirrors this exactly, so the actions it offers are the actions the server
     * will accept.
     * <p>
     * Each entry is one forward step plus, while the goods are still in the shop, the
     * two cancellations. PROCESSING is the only fork: it chooses which fulfilment path
     * the order takes, and afterwards the paths never cross — an order being couriered
     * cannot become ready for collection.
     * <p>
     * PENDING_PAYMENT and IN_STORE_PAYMENT both mean the money is still owed, which is
     * why each leads to PAID and no further, and why neither can be refunded: there is
     * nothing to refund. PAID is the single post-payment state, whichever way the money
     * arrived.
     */
    public Set<OrderStatusEn> allowedTransitions()
    {
        return switch (this) {
            case CREATED -> withCancels(IN_STORE_PAYMENT);
            case PENDING_PAYMENT -> withCancels(PAID);
            case IN_STORE_PAYMENT -> withCancels(PAID);
            case PAYMENT_FAILED -> withCancels();
            case PAID -> withCancels(PROCESSING);
            case PROCESSING -> withCancels(READY_TO_SHIP, READY_FOR_COLLECTION);
            case READY_TO_SHIP -> withCancels(IN_TRANSIT);
            case READY_FOR_COLLECTION -> withCancels(COLLECTED);

            // Past dispatch there is nothing to cancel — the goods have gone out.
            case IN_TRANSIT -> Set.of(DELIVERED, DELIVERY_FAILED);
            case DELIVERY_FAILED -> Set.of(IN_TRANSIT, RETURNED_TO_ORIGIN);
            case DELIVERED, COLLECTED, RETURNED_TO_ORIGIN -> REFUNDS;
            case PARTIALLY_REFUNDED -> Set.of(REFUNDED);

            case REFUNDED, USER_CANCELED, ADMIN_CANCELED, SYSTEM_CANCELED, FAILED, PENDING, CANCELLED -> Set.of();
        };
    }

    /**
     * The transitions the platform makes for itself, with no staff member deciding.
     * Kept apart from {@link #allowedTransitions()} because the admin UI mirrors that
     * map, and an automated move offered as a button is a move staff can make by hand
     * — marking an order PAID without a payment, say.
     * <p>
     * Both maps are enforced; neither is advisory.
     * <ul>
     *   <li>PENDING_PAYMENT — checkout, when the payment gateway is invoked.</li>
     *   <li>IN_STORE_PAYMENT — checkout, when the shopper chooses to pay at
     *       collection. Staff reach it by hand too, so it is in both maps.</li>
     *   <li>PAID / PAYMENT_FAILED — the gateway's callback.</li>
     *   <li>PAYMENT_FAILED → PENDING_PAYMENT — the shopper retrying with another
     *       method. The reservation is deliberately still theirs at that point.</li>
     *   <li>SYSTEM_CANCELED — the abandoned-order sweep.</li>
     *   <li>DELIVERED / DELIVERY_FAILED from IN_TRANSIT — the courier integration when
     *       there is one; staff can record both by hand meanwhile.</li>
     * </ul>
     */
    public Set<OrderStatusEn> systemTransitions()
    {
        return switch (this) {
            case CREATED -> Set.of(PENDING_PAYMENT, IN_STORE_PAYMENT, SYSTEM_CANCELED, FAILED);
            case PENDING_PAYMENT -> Set.of(PAID, PAYMENT_FAILED, SYSTEM_CANCELED, FAILED);
            case PAYMENT_FAILED -> Set.of(PENDING_PAYMENT, SYSTEM_CANCELED);
            case IN_TRANSIT -> Set.of(DELIVERED, DELIVERY_FAILED);

            case IN_STORE_PAYMENT, PAID, PROCESSING, READY_TO_SHIP, READY_FOR_COLLECTION,
                 DELIVERY_FAILED, RETURNED_TO_ORIGIN, DELIVERED, COLLECTED,
                 USER_CANCELED, ADMIN_CANCELED, SYSTEM_CANCELED, FAILED,
                 PARTIALLY_REFUNDED, REFUNDED, PENDING, CANCELLED -> Set.of();
        };
    }

    public boolean canTransitionTo(OrderStatusEn target)
    {
        return target != null && allowedTransitions().contains(target);
    }

    public boolean canSystemTransitionTo(OrderStatusEn target)
    {
        return target != null && systemTransitions().contains(target);
    }

    /**
     * What reaching this status does to the stock the order is holding.
     * <p>
     * Keyed on the destination alone, which is only sound because every RESTORE status
     * is reachable exclusively from pre-dispatch statuses — the invariant
     * {@link #allowedTransitions()} maintains and {@code OrderStatusEnStockEffectTest}
     * pins. If a transition ever admits a cancellation from a dispatched status, this
     * must start taking the source status too.
     * <p>
     * Exhaustive by constant rather than by a default branch: adding a status will not
     * compile until someone answers what reaching it does to stock.
     */
    public StockEffect stockEffect()
    {
        return switch (this) {
            // The order is over and its goods never left the shop.
            case USER_CANCELED, ADMIN_CANCELED, SYSTEM_CANCELED, CANCELLED -> StockEffect.RESTORE;

            // Any failure that is not the gateway declining a payment. Reachable only
            // from pre-dispatch statuses, so the goods are still here.
            case FAILED -> StockEffect.RESTORE;

            // A declined payment keeps the reservation: the shopper is told to retry,
            // and releasing their items first would leave nothing to retry against. The
            // sweep reclaims it if they never come back.
            case PAYMENT_FAILED -> StockEffect.NONE;

            // Reversing a payment is bookkeeping. Whether the goods came back is a
            // physical fact the system does not have, and RETURNED_TO_ORIGIN says they
            // did without saying they are fit to resell — putting any of it back on sale
            // is the returns feature, not this transition.
            case REFUNDED, PARTIALLY_REFUNDED, RETURNED_TO_ORIGIN -> StockEffect.NONE;

            // Either the order is live and holding its reservation, or its goods have
            // genuinely left and there is nothing to return.
            case CREATED, PENDING, PENDING_PAYMENT, IN_STORE_PAYMENT, PAID, PROCESSING,
                 READY_TO_SHIP, READY_FOR_COLLECTION, IN_TRANSIT, DELIVERY_FAILED,
                 DELIVERED, COLLECTED -> StockEffect.NONE;
        };
    }

    /**
     * Whether an order in this status still holds goods that have not left the shop.
     * This is what makes cancellation safe to restock unconditionally.
     * <p>
     * PENDING is false: it is the entity's field default rather than a state an order
     * is put into, and no transition reaches it. Calling it pre-dispatch would make it
     * a terminal status holding goods forever.
     * <p>
     * Exhaustive by constant, so adding a status forces this question to be answered.
     */
    public boolean isPreDispatch()
    {
        return switch (this) {
            case CREATED, PENDING_PAYMENT, IN_STORE_PAYMENT, PAYMENT_FAILED, PAID,
                 PROCESSING, READY_TO_SHIP, READY_FOR_COLLECTION -> true;

            case IN_TRANSIT, DELIVERY_FAILED, RETURNED_TO_ORIGIN, DELIVERED, COLLECTED,
                 USER_CANCELED, ADMIN_CANCELED, SYSTEM_CANCELED, FAILED,
                 PARTIALLY_REFUNDED, REFUNDED, PENDING, CANCELLED -> false;
        };
    }

    /**
     * Whether the abandoned-order sweep may reclaim an order sitting in this status.
     * <p>
     * The rule is "unpaid, still holding its reservation, and not a real commitment".
     * Stock is consumed at CREATED, before payment, so every unpaid status that keeps
     * its reservation needs something to reclaim it — otherwise a shopper who walks
     * away holds those goods forever. PAYMENT_FAILED is included precisely because it
     * deliberately keeps its stock for a retry that may never come.
     * <p>
     * IN_STORE_PAYMENT is excluded although it is unpaid: the shopper has committed to
     * collecting and paying at the counter, so it is not an abandoned checkout. Staff
     * cancel it by hand when nobody arrives.
     * <p>
     * Exhaustive by constant, so a new unpaid status cannot quietly escape the sweep —
     * which is how stock recovery would silently stop working.
     */
    public boolean isReclaimableByStockRecovery()
    {
        return switch (this) {
            case CREATED, PENDING_PAYMENT, PAYMENT_FAILED -> true;

            case IN_STORE_PAYMENT, PAID, PROCESSING, READY_TO_SHIP, READY_FOR_COLLECTION,
                 IN_TRANSIT, DELIVERY_FAILED, RETURNED_TO_ORIGIN, DELIVERED, COLLECTED,
                 USER_CANCELED, ADMIN_CANCELED, SYSTEM_CANCELED, FAILED,
                 PARTIALLY_REFUNDED, REFUNDED, PENDING, CANCELLED -> false;
        };
    }

    /** One forward step (or a fork) plus the cancellations available while goods are held. */
    private static Set<OrderStatusEn> withCancels(OrderStatusEn... forward)
    {
        Set<OrderStatusEn> targets = new LinkedHashSet<>(Set.of(forward));
        targets.addAll(CANCELS);
        return Set.copyOf(targets);
    }
}
