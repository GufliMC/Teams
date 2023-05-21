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
        name = "membership_attributes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"membership_id", "name"})
)
public class DMembershipAttribute extends DAttribute {

    @ManyToOne(optional = false)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    private DMembership membership;

    public DMembershipAttribute() {
    }

    public <T> DMembershipAttribute(@NotNull DMembership membership, @NotNull AttributeKey<T> key, @NotNull T value) {
        super(key, value);
        this.membership = membership;
    }

}
