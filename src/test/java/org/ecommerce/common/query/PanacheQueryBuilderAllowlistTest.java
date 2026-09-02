package org.ecommerce.common.query;

import io.quarkus.panache.common.Sort;
import org.ecommerce.common.query.enums.FilterOperator;
import org.ecommerce.common.query.enums.SortDirection;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link PanacheQueryBuilder}'s per-field allowlist — the mechanism closing the
 * gap where a client-supplied {@link FilterRequest} could name any syntactically-valid field
 * path (including through a JPA association, e.g. {@code "user.passwordHash"}) with no check
 * on whether it was actually safe to expose. {@link FieldNameValidator} only guards syntax;
 * these tests guard semantics.
 */
class PanacheQueryBuilderAllowlistTest
{
    private static FilterRequest withFilter(String key, FilterOperator operator, String value)
    {
        FilterRequest request = new FilterRequest();
        request.setFilters(List.of(new Filter(key, operator, value)));
        return request;
    }

    private static FilterRequest withSort(String field)
    {
        FilterRequest request = new FilterRequest();
        SortRequest sort = new SortRequest();
        sort.setField(field);
        sort.setDirection(SortDirection.ASC);
        request.setSort(List.of(sort));
        return request;
    }

    /** {@code Sort} has no useful {@code toString()} — assert on its columns instead. */
    private static String sortColumnNames(Sort sort)
    {
        return sort.getColumns().stream().map(Sort.Column::getName).toList().toString();
    }

    // --- No allowlist passed (null) means unrestricted, preserving pre-existing behavior ---

    @Test
    void nullAllowlistPermitsAnyField()
    {
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(withFilter("anything.at.all", FilterOperator.EQUALS, "x"));
        assertEquals("anything.at.all = :p0", builder.query());
    }

    // --- An empty (non-null) allowlist rejects every field — the fail-closed default ---

    @Test
    void emptyAllowlistRejectsFilterField()
    {
        FilterRequest request = withFilter("status", FilterOperator.EQUALS, "ACTIVE");
        assertThrows(IllegalArgumentException.class,
                () -> PanacheQueryBuilder.from(request, null, Set.of()));
    }

    @Test
    void emptyAllowlistFallsBackToDefaultSortInsteadOfThrowing()
    {
        FilterRequest request = withSort("status");
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(request, null, Set.of());
        assertEquals("[id]", sortColumnNames(builder.sort()));
    }

    // --- A field on the allowlist works normally ---

    @Test
    void allowlistedFieldIsPermittedAsFilter()
    {
        FilterRequest request = withFilter("status", FilterOperator.EQUALS, "ACTIVE");
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(request, null, Set.of("status"));
        assertEquals("status = :p0", builder.query());
        assertEquals("ACTIVE", builder.params().get("p0"));
    }

    @Test
    void allowlistedFieldIsPermittedAsSort()
    {
        FilterRequest request = withSort("status");
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(request, null, Set.of("status"));
        assertEquals("[status]", sortColumnNames(builder.sort()));
    }

    // --- A field NOT on the allowlist is rejected, even reached through a dotted association
    //     path — this is the exact shape of the real, previously-live vulnerability (a VIEWER
    //     staff role reading password-reset-code hashes via a CustomerRepository filter) ---

    @Test
    void filteringByFieldNotOnAllowlistThrows()
    {
        FilterRequest request = withFilter("user.passwordResetCodeHash", FilterOperator.ILIKE, "a");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> PanacheQueryBuilder.from(request, null, Set.of("user.email")));
        assertEquals("Filtering by \"user.passwordResetCodeHash\" is not permitted", ex.getMessage());
    }

    @Test
    void sortingByFieldNotOnAllowlistIsSkippedRatherThanThrowing()
    {
        // Unlike an unpermitted filter, an unpermitted sort silently falls through to the
        // default rather than erroring — see PanacheQueryBuilder.buildSort's own reasoning:
        // there's a sensible fallback for sort where there isn't one for a dropped filter.
        FilterRequest request = withSort("user.passwordResetCodeHash");
        PanacheQueryBuilder builder = PanacheQueryBuilder.from(request, null, Set.of("user.email"));
        assertEquals("[id]", sortColumnNames(builder.sort()));
    }

    @Test
    void oneAllowedAndOneDisallowedSortFieldKeepsOnlyTheAllowedOne()
    {
        FilterRequest request = new FilterRequest();
        SortRequest disallowed = new SortRequest();
        disallowed.setField("user.passwordResetCodeHash");
        SortRequest allowed = new SortRequest();
        allowed.setField("status");
        request.setSort(List.of(disallowed, allowed));

        PanacheQueryBuilder builder = PanacheQueryBuilder.from(request, null, Set.of("status"));
        assertEquals("[status]", sortColumnNames(builder.sort()));
    }

    // --- The allowlist gates a field regardless of which operator is used against it ---

    @Test
    void disallowedFieldThrowsRegardlessOfOperator()
    {
        FilterRequest isNotNullRequest = new FilterRequest();
        isNotNullRequest.setFilters(List.of(new Filter("user.mfaEnabled", FilterOperator.IS_NOT_NULL, (String) null)));
        assertThrows(IllegalArgumentException.class,
                () -> PanacheQueryBuilder.from(isNotNullRequest, null, Set.of("status")));
    }

    // --- A CollectionExistsRewrite-eligible key is still checked against the allowlist ---

    @Test
    void collectionRewriteKeyIsStillSubjectToTheAllowlist()
    {
        PanacheQueryBuilder.CollectionExistsRewrite rewrite =
                new PanacheQueryBuilder.CollectionExistsRewrite("category.", "p", "categories", "CategoryEntity", "category");
        FilterRequest request = withFilter("category.id", FilterOperator.EQUALS, "11111111-1111-1111-1111-111111111111");

        assertThrows(IllegalArgumentException.class,
                () -> PanacheQueryBuilder.from(request, null, rewrite, Set.of("status")),
                "category.id must still be rejected when it isn't on the allowlist, even though the rewrite would otherwise handle it");

        PanacheQueryBuilder builder = PanacheQueryBuilder.from(request, null, rewrite, Set.of("category.id"));
        assertEquals(
                "EXISTS (SELECT 1 FROM CategoryEntity category WHERE category MEMBER OF p.categories AND category.id = :p0)",
                builder.query());
    }
}
