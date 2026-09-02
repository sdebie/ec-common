package org.ecommerce.common.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "sage_settings")
public class SageSettingsEntity extends PanacheEntityBase
{
    @Id
    private UUID id;

    @Column(name = "company_id")
    private String companyId;

    @Column(name = "retail_id")
    private String retailId;

    @Column(name = "wholesale_id")
    private String wholesaleId;

    @Column(name = "api_url")
    private String apiUrl;

    @Column(name = "key")
    private String key;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "items_lastupdate")
    private LocalDateTime itemsLastUpdate;

    @Column(name = "items_lastid")
    private String itemsLastId;

    @Column(name = "pricelis_lastid")
    private String priceListLastId;
}
