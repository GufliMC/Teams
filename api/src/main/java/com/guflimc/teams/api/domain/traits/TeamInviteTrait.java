package com.guflimc.teams.api.domain.traits;

import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.TeamTrait;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface TeamInviteTrait extends TeamTrait {

    void invite(@NotNull Profile sender, @NotNull Profile target);

    Collection<TeamInvite> invites();

    Optional<TeamInvite> invite(@NotNull Profile target);

    //

    interface TeamInvite {

        UUID id();

        UUID sender();

        UUID target();

        Team team();

        void reject();

        void accept();

        void cancel();

        boolean isExpired();

        boolean isRejected();

        boolean isAccepted();

        boolean isCancelled();

        default boolean isAnswered() {
            return isRejected() || isAccepted();
        }

        default boolean isActive() {
            return !isAnswered() && !isExpired() && !isCancelled();
        }

    }

}
