package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.StaffUserEntity;

import java.util.UUID;

@ApplicationScoped
public class StaffRepository extends BaseRepository<StaffUserEntity, UUID>
{

    /** Returns a staff user with the given email that belongs to a different record than {@code excludeId}. */
    public StaffUserEntity findByEmailExcludingId(String email, UUID excludeId)
    {
        if (excludeId == null) {
            return find("lower(email) = lower(?1)", email).firstResult();
        }
        return find("lower(email) = lower(?1) and id != ?2", email, excludeId).firstResult();
    }
}
