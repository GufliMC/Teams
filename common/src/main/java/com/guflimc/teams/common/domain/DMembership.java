package com.guflimc.teams.common.domain;

import com.guflimc.teams.api.TeamAPI;
import com.guflimc.teams.api.domain.Membership;
import com.guflimc.teams.api.domain.MembershipAttributeKey;
import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.common.EventManager;
import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "memberships")
public class DMembership implements Membership {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(targetEntity = DProfile.class, optional = false)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    private DProfile profile;

    @ManyToOne(targetEntity = DTeam.class, optional = false)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    private DTeam team;

    @OneToMany(targetEntity = DMembershipAttribute.class, mappedBy = "membership",
            cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DMembershipAttribute> attributes = new ArrayList<>();

    @DbDefault("true")
    private boolean active = true;

    @WhenCreated
    private Instant createdAt;

    @WhenModified
    private Instant updatedAt;

    //

    private DMembership() {
    }

    public DMembership(DProfile profile, DTeam team) {
        this.profile = profile;
        this.team = team;
    }

    @Override
    public Profile profile() {
        return profile;
    }

    @Override
    public Team team() {
        return team;
    }

    @Override
    public Instant createdAt() {
        return createdAt;
    }

    // actions

    @Override
    public void quit() {
        team.setMembers(team.members() - 1);

        this.active = false;
        profile.removeMembership(this);
        team.invites().removeIf(invite -> invite.target().equals(profile) || invite.sender().equals(profile));

        TeamAPI.get().update(profile);
        TeamAPI.get().update(team);

        EventManager.INSTANCE.onLeave(profile, team);
    }

    @Override
    public <T> void setAttribute(MembershipAttributeKey<T> key, T value) {
        if (value == null) {
            removeAttribute(key);
            return;
        }

        DAttribute attribute = attributes.stream()
                .filter(attr -> attr.name().equals(key.name()))
                .findFirst().orElse(null);

        if (attribute == null) {
            attributes.add(new DMembershipAttribute(this, key, value));
            return;
        }

        attribute.setValue(key, value);
    }

    @Override
    public <T> void removeAttribute(MembershipAttributeKey<T> key) {
        attributes.removeIf(attr -> attr.name().equals(key.name()));
    }

    @Override
    public <T> Optional<T> attribute(MembershipAttributeKey<T> key) {
        return attributes.stream().filter(attr -> attr.name().equals(key.name()))
                .findFirst().map(ra -> ra.value(key));
    }

}