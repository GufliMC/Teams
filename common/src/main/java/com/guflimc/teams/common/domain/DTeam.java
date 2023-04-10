package com.guflimc.teams.common.domain;

import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.TeamTrait;
import io.ebean.annotation.Formula;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Table(name = "clans")
public class DTeam implements Team {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Formula(select = "aggr.member_count",
            join = "join (select cp.clan_id, count(cp.clan_id) as member_count from clan_profiles cp where cp.active = 1 group by cp.clan_id) as aggr ON ${ta}.id = aggr.clan_id")
    public int members;

    @OneToMany(targetEntity = DTeamAttribute.class, mappedBy = "clan",
            cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DTeamAttribute> attributes = new ArrayList<>();

    @WhenCreated
    private Instant createdAt;

    @WhenModified
    private Instant updatedAt;

    //

    private transient final Map<Class<?>, TeamTrait> traits = new ConcurrentHashMap<>();

    //

    public DTeam() {
    }

    public DTeam(@NotNull String name) {
        this.name = name;
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int members() {
        return members;
    }

    @Override
    public Instant createdAt() {
        return createdAt;
    }

    // attributes

    @Override
    public <T> void setAttribute(TeamAttributeKey<T> key, T value) {
        if (value == null) {
            removeAttribute(key);
            return;
        }

        DAttribute attribute = attributes.stream()
                .filter(attr -> attr.name().equals(key.name()))
                .findFirst().orElse(null);

        if (attribute == null) {
            attributes.add(new DTeamAttribute(this, key, value));
            return;
        }

        attribute.setValue(key, value);
    }

    @Override
    public <T> void removeAttribute(TeamAttributeKey<T> key) {
        attributes.removeIf(attr -> attr.name().equals(key.name()));
    }

    @Override
    public <T> Optional<T> attribute(TeamAttributeKey<T> key) {
        return attributes.stream().filter(attr -> attr.name().equals(key.name()))
                .findFirst().map(ra -> ra.value(key));
    }

    // traits

    @Override
    public <T extends TeamTrait> boolean hasTrait(Class<T> type) {
        return traits.containsKey(type);
    }

    @Override
    public <T extends TeamTrait> Optional<T> trait(Class<T> type) {
        return Optional.ofNullable(type.cast(traits.get(type)));
    }

    public void addTrait(TeamTrait trait) {
        traits.put(trait.getClass(), trait);
    }

    public void removeTrait(Class<? extends TeamTrait> type) {
        traits.remove(type);
    }

}