package com.guflimc.teams.common.domain;

import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.TeamAttributeKey;
import com.guflimc.teams.api.domain.TeamTrait;
import com.guflimc.teams.api.domain.TeamType;
import com.guflimc.teams.api.domain.traits.TeamInviteTrait;
import io.ebean.annotation.Formula;
import io.ebean.annotation.Index;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Table(name = "clans")
@Index(columnNames = {"type", "name"}, unique = true)
public class DTeam implements Team {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String name;

    @Formula(select = "aggr.member_count",
            join = "join (select mb.team_id, count(mb.team_id) as member_count from memberships mb where mb.active = 1 group by mb.team_id) as aggr ON ${ta}.id = aggr.team_id")
    private int members;

    @OneToMany(targetEntity = DTeamAttribute.class, mappedBy = "team",
            cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DTeamAttribute> attributes = new ArrayList<>();

    @OneToMany(targetEntity = DTeamInvite.class, mappedBy = "team",
            cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DTeamInvite> invites = new ArrayList<>();

    @WhenCreated
    private Instant createdAt;

    @WhenModified
    private Instant updatedAt;

    //

    private transient final Map<Class<?>, TeamTrait> traits = new ConcurrentHashMap<>();

    //

    public DTeam() {
    }

    public DTeam(@NotNull TeamType type,  @NotNull String name) {
        this.type = type.name();
        this.name = name;
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public TeamType type() {
        return TeamType.of(type);
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
    public <T extends TeamTrait> void addTrait(T trait) {
        if ( trait(trait.getClass()).isPresent() ) {
            throw new IllegalArgumentException("Trait with the same type or subtype already exists already exists.");
        }
        traits.put(trait.getClass(), trait);
    }

    @Override
    public <T extends TeamTrait> void removeTrait(Class<T> type) {
        traits.remove(type);
    }

    @Override
    public <T extends TeamTrait> Optional<T> trait(Class<T> type) {
        if ( traits.containsKey(type) ) {
            return Optional.of(type.cast(traits.get(type)));
        }

        for ( Class<?> cls : traits.keySet() ) {
            if ( type.isAssignableFrom(cls) ) {
                return Optional.of(type.cast(traits.get(cls)));
            }
        }

        return Optional.empty();
    }

    // INTERNAL

    public Collection<DTeamInvite> invites() {
        return Collections.unmodifiableCollection(invites);
    }

    public void addInvite(@NotNull DProfile sender, @NotNull DProfile target) {
        invites.add(new DTeamInvite(this, sender, target));
    }

    public void removeInvite(@NotNull DTeamInvite invite) {
        invites.remove(invite);
    }

    public Optional<DTeamInvite> invite(@NotNull DProfile target) {
        return invites.stream()
                .filter(invite -> invite.target().equals(target))
                .max(Comparator.comparing(DTeamInvite::createdAt));
    }

    public void setMembers(int members) {
        this.members = members;
    }

}