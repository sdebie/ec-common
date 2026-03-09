package org.ecommerce.common.enums;

/**
 * Enum for different price types that can be associated with product variants.
 * Each variant can have different prices for different customer types and scenarios.
 */
public enum PriceTypeEn {
    RETAIL_PRICE,           // Standard retail price
    RETAIL_SALE_PRICE,      // Sale/promotional price for retail customers
    WHOLESALE_PRICE,        // Bulk/wholesale price
    WHOLESALE_SALE_PRICE    // Sale/promotional price for wholesale customers
}
