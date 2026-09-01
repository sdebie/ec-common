package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.StaffUserEntity;

import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class StaffRepository extends BaseRepository<StaffUserEntity, UUID>
{
    /**
     * Every field here is a plain column — {@code StaffUserEntity} has no relations to worry
     * about traversing. Excludes {@code passwordHash} and the {@code passwordResetCode*}
     * family: the {@code staffList}/{@code staffCount} GraphQL queries this repository backs
     * are {@code @RolesAllowed("SUPER_ADMIN")}, but even a super-admin caller should not be
     * able to turn an EQUALS/ILIKE filter into an oracle for another staff member's password
     * or reset-code hash — the same mechanism as the known VIEWER/CustomerRepository
     * credential-oracle vulnerability, just against a different entity.
     */
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of(
            "id", "email", "fullName", "role", "isActive", "resetPassword", "createdAt");

    @Override
    protected Class<StaffUserEntity> getEntityClass()
    {
        return StaffUserEntity.class;
    }

    @Override
    protected Set<String> filterableFields()
    {
        return ALLOWED_FILTER_FIELDS;
    }

    public StaffUserEntity findByEmail(String email)
    {
        return find("lower(email) = lower(?1)", email).firstResult();
    }

    /** Returns a staff user with the given email that belongs to a different record than {@code excludeId}. */
    public StaffUserEntity findByEmailExcludingId(String email, UUID excludeId)
    {
        if (excludeId == null) {
            return find("lower(email) = lower(?1)", email).firstResult();
        }
        return find("lower(email) = lower(?1) and id != ?2", email, excludeId).firstResult();
    }
}
