package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.StaffUserEntity;

import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class StaffRepository extends BaseRepository<StaffUserEntity, UUID>
{
    private static final Set<String> ALLOWED_FILTER_FIELDS = Set.of("id", "email", "fullName", "role", "isActive", "resetPassword", "createdAt");

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

    public StaffUserEntity findByEmailExcludingId(String email, UUID excludeId)
    {
        if (excludeId == null) {
            return find("lower(email) = lower(?1)", email).firstResult();
        }
        return find("lower(email) = lower(?1) and id != ?2", email, excludeId).firstResult();
    }
}
