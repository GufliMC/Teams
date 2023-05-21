package com.guflimc.teams.api;

import com.guflimc.teams.api.domain.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface TeamManager {

    void reload();

    // teams

    Collection<Team> teams(@NotNull TeamType type);

    Optional<Team> team(@NotNull TeamType type, @NotNull String name);

    Optional<Team> team(@NotNull TeamType type, @NotNull UUID id);

    CompletableFuture<Team> create(@NotNull TeamType type, @NotNull String name);

    CompletableFuture<Void> remove(@NotNull Team team);

    CompletableFuture<Void> update(@NotNull Team team);

    // profiles

    CompletableFuture<Profile> profile(@NotNull UUID id);

    CompletableFuture<Profile> profile(@NotNull String name);

    CompletableFuture<List<Profile>> profiles(@NotNull Team team);

    CompletableFuture<Void> update(@NotNull Profile profile);

    //

    CompletableFuture<Void> update(@NotNull Membership membership);

}
