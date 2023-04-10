package com.guflimc.teams.common.domain;

import com.guflimc.teams.api.TeamAPI;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.ClanPermission;
import com.guflimc.teams.api.domain.Membership;
import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.common.EventManager;
import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.*;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "clan_profiles")
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
        team.members--;

        this.active = false;
        profile.memberships.remove(this);
        profile.sentInvites.clear();

        TeamAPI.get().update(profile);

        EventManager.INSTANCE.onLeave(profile, team);
    }

}