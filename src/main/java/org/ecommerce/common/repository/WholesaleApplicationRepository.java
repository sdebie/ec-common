package org.ecommerce.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.ecommerce.common.entity.WholesaleApplicationEntity;

import java.util.UUID;

@ApplicationScoped
public class WholesaleApplicationRepository extends BaseRepository<WholesaleApplicationEntity, UUID> {
}

