package com.guflimc.teams.common.domain;

import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.traits.TeamInviteTrait;
import com.guflimc.teams.common.EventManager;
import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbForeignKey;
import io.ebean.annotation.WhenCreated;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "team_invites")
public class DTeamInvite implements TeamInviteTrait.TeamInvite {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(targetEntity = DTeam.class, optional = false)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    private DTeam team;

    @ManyToOne(targetEntity = DProfile.class, optional = false)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    private DProfile sender;

    @ManyToOne(targetEntity = DProfile.class, optional = false)
    @DbForeignKey(onDelete = ConstraintMode.CASCADE)
    private DProfile target;

    @DbDefault("false")
    private boolean declined;

    @DbDefault("false")
    private boolean accepted;

    @DbDefault("false")
    private boolean cancelled;

    @WhenCreated
    private Instant createdAt = Instant.now();

    //

    public DTeamInvite() {
    }

    public DTeamInvite(@NotNull DTeam team, @NotNull DProfile sender, @NotNull DProfile target) {
        this.team = team;
        this.sender = sender;
        this.target = target;

        EventManager.INSTANCE.onInviteSent(this);
    }

    @Override
    public Profile sender() {
        return sender;
    }

    @Override
    public Profile target() {
        return target;
    }

    @Override
    public Team team() {
        return team;
    }

    @Override
    public void decline() {
        if (isAnswered()) {
            throw new IllegalStateException("This invite is already answered.");
        }

        this.declined = true;
        EventManager.INSTANCE.onInviteDecline(this);
    }

    @Override
    public void accept() {
        if (isAnswered()) {
            throw new IllegalStateException("This invite is already answered.");
        }

        this.accepted = true;
        target.join(team);
    }

    @Override
    public void cancel() {
        if (isAnswered()) {
            throw new IllegalStateException("This invite is already answered.");
        }

        this.cancelled = true;
        EventManager.INSTANCE.onInviteCancel(this);
    }

    @Override
    public boolean isExpired() {
        return Instant.now().isAfter(createdAt.plus(24, ChronoUnit.HOURS));
    }

    @Override
    public boolean isDeclined() {
        return declined;
    }

    @Override
    public boolean isAccepted() {
        return accepted;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    Instant createdAt() {
        return createdAt;
    }

}