package com.guflimc.teams.common.domain;

import com.guflimc.brick.orm.api.attributes.AttributeKey;
import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbForeignKey;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
        name = "profile_attributes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "name"})
)
public class DProfileAttribute extends DAttribute {

    @ManyToOne(optional = false)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    private DProfile profile;

    public DProfileAttribute() {
    }

    public <T> DProfileAttribute(@NotNull DProfile profile, @NotNull AttributeKey<T> key, @NotNull T value) {
        super(key, value);
        this.profile = profile;
    }

}
