package com.guflimc.teams.common.domain;

import com.guflimc.brick.orm.api.attributes.AttributeKey;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
public class DAttribute {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "attrvalue", nullable = false)
    private String value;

    public DAttribute() {
    }

    public <T> DAttribute(@NotNull AttributeKey<T> key, @NotNull T value) {
        this.name = key.name();
        this.value = key.serialize(value);
    }

    public String name() {
        return name;
    }

    public <T> void setValue(AttributeKey<T> key, T value) {
        if (value == null) {
            this.value = null;
            return;
        }

        this.value = key.serialize(value);
    }

    public <T> T value(AttributeKey<T> key) {
        return key.deserialize(value);
    }

}
