package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.UserEntity;

import java.util.UUID;

@ApplicationScoped
public class UserRepository extends BaseRepository<UserEntity, UUID>
{
    @Override
    protected Class<UserEntity> getEntityClass()
    {
        return UserEntity.class;
    }

    public UserEntity findByEmail(String email)
    {
        return find("lower(email) = lower(?1)", email).firstResult();
    }
}
