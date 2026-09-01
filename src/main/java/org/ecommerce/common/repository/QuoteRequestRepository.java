package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.QuoteRequestEntity;

import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class QuoteRequestRepository extends BaseRepository<QuoteRequestEntity, UUID> {

    /**
     * {@code allQuoteRequests}/{@code quoteRequestCount} are VIEWER-reachable. Excludes the
     * {@code quotedBy} relation to {@code StaffUserEntity} entirely — it carries
     * {@code passwordHash} and the {@code passwordResetCode*} family, and a VIEWER-role caller
     * could otherwise use it as the same kind of credential oracle already found on
     * {@code CustomerRepository} (via {@code user.passwordResetCodeHash}), just reached
     * through {@code quotedBy.*} instead of {@code user.*}. Also excludes {@code items} (a
     * to-many association with no {@link
     * org.ecommerce.common.query.PanacheQueryBuilder.CollectionExistsRewrite} registered
     * here, whose {@code items.quoteRequest} back-reference would otherwise offer a second
     * route back into {@code quotedBy.*}).
     */
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of(
            "status", "createdAt", "name", "company", "statusChangedAt", "quotedAmount");

    @Override
    protected Class<QuoteRequestEntity> getEntityClass() {
        return QuoteRequestEntity.class;
    }

    @Override
    protected Set<String> filterableFields() {
        return ALLOWED_FILTER_FIELDS;
    }
}
