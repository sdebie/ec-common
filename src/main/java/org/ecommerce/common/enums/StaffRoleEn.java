package org.ecommerce.common.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum StaffRoleEn {
    SUPER_ADMIN,      // Full access to everything
    CATALOG_MANAGER,  // Manage products, categories, and brands
    ORDER_MANAGER,    // Manage customer orders and shipping
    VIEWER;           // Read-only access to the dashboard

    private static final Set<String> NAMES = Arrays.stream(values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    /** Every role's name, for checking a JWT/SecurityIdentity's roles against "is staff at all." */
    public static Set<String> names()
    {
        return NAMES;
    }
}