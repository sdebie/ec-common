package org.ecommerce.common.enums;

/**
 * What reaching a given order status does to the stock the order is holding.
 * <p>
 * Stock is consumed once, at CREATED, before payment. Every status that ends an
 * order therefore has to answer what became of those goods, and the answer is a
 * property of the status itself rather than of whichever writer happens to move
 * the order there — the abandoned-order sweep and a staff cancellation must not
 * be able to reach different conclusions about the same transition.
 *
 * @see OrderStatusEn#stockEffect()
 */
public enum StockEffect
{
    /** The order keeps its reservation: the goods are still committed to it. */
    NONE,

    /** The order is over and its goods never left; the stock goes back to inventory. */
    RESTORE
}
