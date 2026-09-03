package org.ecommerce.common.query;

import org.ecommerce.common.query.enums.FilterOperator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link PanacheQueryBuilder}'s BETWEEN/NOT_BETWEEN handling — added alongside
 * the operator implementation itself, which previously had no coverage at all.
 */
class PanacheQueryBuilderBetweenTest
{
    private static FilterRequest withBetween(FilterOperator operator, String from, String to)
    {
        FilterRequest request = new FilterRequest();
        request.setFilters(List.of(new Filter("createdAt", operator, List.of(from, to))));
        return request;
    }

    @Test
    void betweenGeneratesInclusiveRangeClauseWithBothBoundsBound()
    {
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(withBetween(FilterOperator.BETWEEN, "low", "high"), null, null, null);
        assertEquals("createdAt BETWEEN :p0 AND :p1", builder.query());
        assertEquals("low", builder.params().get("p0"));
        assertEquals("high", builder.params().get("p1"));
    }

    @Test
    void notBetweenGeneratesExcludingRangeClause()
    {
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(withBetween(FilterOperator.NOT_BETWEEN, "low", "high"), null, null, null);
        assertEquals("createdAt NOT BETWEEN :p0 AND :p1", builder.query());
        assertEquals("low", builder.params().get("p0"));
        assertEquals("high", builder.params().get("p1"));
    }

    // --- Each bound goes through the same coercion as any other operator's value(s) ---

    @Test
    void betweenCoercesEachBoundLikeAnyOtherOperator()
    {
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(withBetween(FilterOperator.BETWEEN, "10", "20"), null, null, null);
        assertEquals(10L, builder.params().get("p0"));
        assertEquals(20L, builder.params().get("p1"));
    }

    // --- Exactly two values are required, same as the bounds a BETWEEN clause needs ---

    @Test
    void betweenWithFewerThanTwoValuesThrows()
    {
        FilterRequest request = new FilterRequest();
        request.setFilters(List.of(new Filter("createdAt", FilterOperator.BETWEEN, List.of("10"))));
        assertThrows(IllegalArgumentException.class,
                () -> PanacheQueryBuilder.from(request, null, null, null));
    }

    @Test
    void betweenWithMoreThanTwoValuesThrows()
    {
        FilterRequest request = new FilterRequest();
        request.setFilters(List.of(new Filter("createdAt", FilterOperator.BETWEEN, List.of("10", "20", "30"))));
        assertThrows(IllegalArgumentException.class,
                () -> PanacheQueryBuilder.from(request, null, null, null));
    }

    // --- Two BETWEEN filters in one request must not collide on param names ---

    @Test
    void secondBetweenFilterGetsItsOwnParamNames()
    {
        FilterRequest request = new FilterRequest();
        request.setFilters(List.of(
                new Filter("createdAt", FilterOperator.BETWEEN, List.of("low", "high")),
                new Filter("updatedAt", FilterOperator.BETWEEN, List.of("early", "late"))));

        PanacheQueryBuilder builder = PanacheQueryBuilder.from(request, null, null, null);
        assertEquals("createdAt BETWEEN :p0 AND :p1 AND updatedAt BETWEEN :p2 AND :p3", builder.query());
        assertEquals("low", builder.params().get("p0"));
        assertEquals("high", builder.params().get("p1"));
        assertEquals("early", builder.params().get("p2"));
        assertEquals("late", builder.params().get("p3"));
    }
}
