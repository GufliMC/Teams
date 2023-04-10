package com.guflimc.teams.api;

import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.Membership;
import com.guflimc.teams.api.domain.TeamTrait;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TeamManager {

    void reload();

    // teams

    Collection<Team> teams();

    Optional<Team> team(@NotNull String name);

    Optional<Team> team(@NotNull UUID id);

    CompletableFuture<Team> create(@NotNull String name);

    CompletableFuture<Void> remove(@NotNull Team team);

    CompletableFuture<Void> update(@NotNull Team team);

    // profiles

    CompletableFuture<Profile> profile(@NotNull UUID id);

    CompletableFuture<Profile> profile(@NotNull String name);

    CompletableFuture<List<Profile>> profiles(@NotNull Team team);

    CompletableFuture<Void> update(@NotNull Profile profile);

    //

    /**
     * Add a trait to a team, make sure traits do not cause unwanted
     * side effects to other teams or the state of the program.
     */
    void addTrait(@NotNull Team team, @NotNull TeamTrait trait);

    /**
     * Remove a trait from a team, make sure traits do not cause unwanted
     * side effects to other teams or the state of the program.
     */
    void removeTrait(@NotNull Team team, @NotNull TeamTrait trait);

    //

//    CompletableFuture<Void> update(@NotNull Membership membership);

}
