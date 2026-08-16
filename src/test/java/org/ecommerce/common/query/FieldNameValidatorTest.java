package org.ecommerce.common.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link FieldNameValidator} — the shared field-name sanitizer
 * that guards both PanacheQueryBuilder and buildOrderByClause against injection.
 *
 */
class FieldNameValidatorTest
{
    // --- Rejection cases: comma, whitespace, parenthesis ---

    @Test
    void rejectsFieldContainingComma()
    {
        assertThrows(IllegalArgumentException.class,
                () -> FieldNameValidator.validate("name, 1=1 --"));
    }

    @Test
    void rejectsFieldContainingWhitespace()
    {
        assertThrows(IllegalArgumentException.class,
                () -> FieldNameValidator.validate("name OR 1=1"));
    }

    @Test
    void rejectsFieldContainingOpenParenthesis()
    {
        assertThrows(IllegalArgumentException.class,
                () -> FieldNameValidator.validate("name()"));
    }

    @Test
    void rejectsFieldContainingCloseParenthesisInSubquery()
    {
        assertThrows(IllegalArgumentException.class,
                () -> FieldNameValidator.validate("(select 1)"));
    }

    @Test
    void rejectsNullField()
    {
        assertThrows(IllegalArgumentException.class,
                () -> FieldNameValidator.validate(null));
    }

    // --- Valid field names pass through unchanged ---

    @Test
    void acceptsSimpleFieldName()
    {
        assertEquals("name", FieldNameValidator.validate("name"));
    }

    @Test
    void acceptsDottedFieldName()
    {
        assertEquals("p.name", FieldNameValidator.validate("p.name"));
    }

    @Test
    void acceptsTwoDotNavigation()
    {
        assertEquals("category.id", FieldNameValidator.validate("category.id"));
    }

    @Test
    void acceptsDeepDotNavigation()
    {
        assertEquals("address.city.zip", FieldNameValidator.validate("address.city.zip"));
    }
}
