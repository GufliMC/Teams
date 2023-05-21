package com.guflimc.teams.common.domain;

import com.guflimc.teams.api.domain.*;
import com.guflimc.teams.common.EventManager;
import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.*;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "profiles")
public class DProfile implements Profile {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @OneToMany(targetEntity = DMembership.class, mappedBy = "profile", fetch = FetchType.EAGER,
            orphanRemoval = true, cascade = CascadeType.ALL)
    @Where(clause = "active = 1")
    @DbForeignKey(onDelete = ConstraintMode.SET_NULL)
    private List<DMembership> memberships;

    @OneToMany(targetEntity = DProfileAttribute.class, mappedBy = "profile",
            cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DProfileAttribute> attributes = new ArrayList<>();

    private Instant lastSeenAt;

    @WhenCreated
    private Instant createdAt;

    @WhenModified
    private Instant updatedAt;

    //

    private DProfile() {
    }

    public DProfile(UUID id, String name) {
        this.id = id;
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
    public Collection<Membership> memberships() {
        return Collections.unmodifiableCollection(memberships);
    }

    @Override
    public Optional<Membership> membership(@NotNull TeamType type) {
        return memberships.stream()
                .filter(m -> m.team().type().equals(type))
                .map(m -> (Membership) m)
                .findFirst();
    }

    @Override
    public Optional<Membership> membership(@NotNull Team team) {
        return memberships.stream()
                .filter(m -> m.team().equals(team))
                .map(m -> (Membership) m)
                .findFirst();
    }

    @Override
    public Instant createdAt() {
        return createdAt;
    }

    // actions

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void join(@NotNull Team team) {
        if ( membership(team).isPresent() ) {
            throw new IllegalArgumentException("Already a member of this team.");
        }
        if ( membership(team.type()).isPresent() ) {
            throw new IllegalArgumentException("Already a member of another team with the same type.");
        }

        // join new team
        DMembership membership = new DMembership(this, (DTeam) team);
        memberships.add(membership);

        ((DTeam) team).setMembers(team.members() + 1);

        EventManager.INSTANCE.onJoin(this, team);
    }

    // attributes

    @Override
    public <T> void setAttribute(ProfileAttributeKey<T> key, T value) {
        if (value == null) {
            removeAttribute(key);
            return;
        }

        DAttribute attribute = attributes.stream()
                .filter(attr -> attr.name().equals(key.name()))
                .findFirst().orElse(null);

        if (attribute == null) {
            attributes.add(new DProfileAttribute(this, key, value));
            return;
        }

        attribute.setValue(key, value);
    }

    @Override
    public <T> void removeAttribute(ProfileAttributeKey<T> key) {
        attributes.removeIf(attr -> attr.name().equals(key.name()));
    }

    @Override
    public <T> Optional<T> attribute(ProfileAttributeKey<T> key) {
        return attributes.stream().filter(attr -> attr.name().equals(key.name()))
                .findFirst().map(ra -> ra.value(key));
    }

    // INTERNAL

    public void removeMembership(DMembership membership) {
        memberships.remove(membership);
    }

}