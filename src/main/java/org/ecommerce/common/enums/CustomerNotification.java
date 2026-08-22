package org.ecommerce.common.enums;

/**
 * The email a shopper receives when their order reaches a given status.
 * <p>
 * Which one is a property of the status rather than of whichever writer moved the
 * order there. Before this existed, the single confirmation email was sent from three
 * separate call sites that each decided for themselves — so a new writer sent nothing
 * and nothing caught it, the same defect the stock rules already had.
 * <p>
 * Several statuses share a template because they say the same thing to a shopper: they
 * differ in the copy the template selects, not in the email that arrives.
 *
 * @see OrderStatusEn#customerNotification()
 */
public enum CustomerNotification
{
    /**
     * Deliberately silent. Either nothing has happened yet that the shopper needs
     * telling about, or telling them would be actively unhelpful. FAILED is
     * deliberately NOT one of these despite being vague — see
     * {@link OrderStatusEn#customerNotification()} for which statuses are silent
     * and why.
     */
    NONE,

    /** The order is waiting on the shopper: pay online, pay at the counter, or retry. */
    ACTION_REQUIRED,

    /** Payment succeeded. The shopper's receipt, and the first email they can rely on. */
    CONFIRMED,

    /** Waiting at the counter — needs the store's address, hours and what to bring. */
    READY_FOR_COLLECTION,

    /** On its way, carrying the courier's tracking reference. */
    IN_TRANSIT,

    /** The shopper has their goods, by delivery or over the counter. */
    COMPLETED,

    /** The courier could not deliver, or brought the order back. */
    DELIVERY_PROBLEM,

    /** The order ended before its goods left, however it ended. */
    ENDED,

    /** Money has been sent back, in full or in part. */
    REFUNDED
}
