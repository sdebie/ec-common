package org.ecommerce.common.query;

import org.ecommerce.common.entity.OrderEntity;
import org.ecommerce.common.enums.OrderStatusEn;
import org.ecommerce.common.query.enums.FilterOperator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Unit tests for {@link PanacheQueryBuilder}'s field-type-aware coercion — the fix for a
 * filter value that is a well-formed ISO-8601 timestamp string previously falling through
 * {@link PanacheQueryBuilder}'s old shape-only guessing to a raw {@code String}, which then
 * failed to bind against an actual {@code Instant} column. Resolving the field's real type via
 * reflection on the entity class (as already happened for enums) is what closes the gap.
 */
class PanacheQueryBuilderTypeCoercionTest
{
    private static FilterRequest withFilter(String key, FilterOperator operator, String value)
    {
        FilterRequest request = new FilterRequest();
        request.setFilters(List.of(new Filter(key, operator, value)));
        return request;
    }

    private static FilterRequest withFilter(String key, FilterOperator operator, String from, String to)
    {
        FilterRequest request = new FilterRequest();
        request.setFilters(List.of(new Filter(key, operator, List.of(from, to))));
        return request;
    }

    // --- An Instant-typed field coerces an ISO-8601 string to a real Instant ---

    @Test
    void equalsCoercesIsoTimestampStringToInstantForAnInstantField()
    {
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(
                withFilter("createdAt", FilterOperator.EQUALS, "2026-01-01T00:00:00Z"), OrderEntity.class, null, null);

        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), builder.params().get("p0"));
    }

    @Test
    void greaterThanCoercesIsoTimestampStringToInstantForAnInstantField()
    {
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(
                withFilter("createdAt", FilterOperator.GREATER_THAN, "2026-01-01T00:00:00Z"), OrderEntity.class, null, null);

        assertEquals("createdAt > :p0", builder.query());
        assertInstanceOf(Instant.class, builder.params().get("p0"));
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), builder.params().get("p0"));
    }

    @Test
    void betweenCoercesBothIsoTimestampBoundsToInstant()
    {
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(
                withFilter("createdAt", FilterOperator.BETWEEN, "2026-01-01T00:00:00Z", "2026-02-01T00:00:00Z"),
                OrderEntity.class, null, null);

        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), builder.params().get("p0"));
        assertEquals(Instant.parse("2026-02-01T00:00:00Z"), builder.params().get("p1"));
    }

    @Test
    void inCoercesEveryIsoTimestampValueInTheListToInstant()
    {
        FilterRequest request = new FilterRequest();
        request.setFilters(List.of(new Filter("createdAt", FilterOperator.IN,
                List.of("2026-01-01T00:00:00Z", "2026-02-01T00:00:00Z"))));

        PanacheQueryBuilder builder = PanacheQueryBuilder.from(request, OrderEntity.class, null, null);

        @SuppressWarnings("unchecked")
        List<Object> bound = (List<Object>) builder.params().get("p0");
        assertEquals(List.of(Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-02-01T00:00:00Z")), bound);
    }

    // --- A malformed value degrades to the raw string rather than throwing ---

    @Test
    void malformedTimestampStringFallsBackToRawStringRatherThanThrowing()
    {
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(
                withFilter("createdAt", FilterOperator.GREATER_THAN, "not-a-timestamp"), OrderEntity.class, null, null);

        assertEquals("not-a-timestamp", builder.params().get("p0"));
    }

    // --- Enum coercion (the reflection this fix generalizes) keeps working ---

    @Test
    void enumFieldStillCoercesToTheEnumConstantAfterGeneralizingFieldTypeResolution()
    {
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(
                withFilter("status", FilterOperator.EQUALS, "PAID"), OrderEntity.class, null, null);

        assertEquals(OrderStatusEn.PAID, builder.params().get("p0"));
    }

    // --- With no entity class (the caller doesn't always have or need one), coercion is
    //     unchanged: a timestamp-shaped string is not special-cased and falls through to a
    //     plain String, exactly as before this fix ---

    @Test
    void withoutAnEntityClassAnIsoTimestampStringIsNotSpecialCased()
    {
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(
                withFilter("createdAt", FilterOperator.GREATER_THAN, "2026-01-01T00:00:00Z"), null, null, null);

        assertEquals("2026-01-01T00:00:00Z", builder.params().get("p0"));
    }
}
