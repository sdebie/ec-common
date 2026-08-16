package org.ecommerce.common.query;

/**
 * Shared JPQL field-name validation — one definition, used by both
 * {@link PanacheQueryBuilder} (sanitize) and
 * {@link org.ecommerce.common.repository.OrderRepository} (its hand-written admin-list
 * ORDER BY, which cannot go through {@code PanacheQueryBuilder} — see that repository for
 * why).
 * <p>
 * Only alphanumerics, underscores, and dots are allowed. Dot notation supports
 * JOIN navigation (e.g. "address.city").
 */
public final class FieldNameValidator
{
    private static final String FIELD_PATTERN = "[\\w.]+";

    private FieldNameValidator() {}

    /**
     * Validates that the given field name matches the allowed pattern.
     *
     * @param field the field name to validate
     * @return the field name unchanged if valid
     * @throws IllegalArgumentException if the field contains disallowed characters
     */
    public static String validate(String field)
    {
        if (field == null || !field.matches(FIELD_PATTERN)) {
            throw new IllegalArgumentException("Invalid field name: '" + field + "'");
        }
        return field;
    }
}
