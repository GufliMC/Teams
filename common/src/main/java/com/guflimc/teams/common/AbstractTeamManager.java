package com.guflimc.teams.common;

import com.guflimc.teams.api.TeamManager;
import com.guflimc.teams.api.domain.Membership;
import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.TeamType;
import com.guflimc.teams.common.config.TeamsConfig;
import com.guflimc.teams.common.domain.DProfile;
import com.guflimc.teams.common.domain.DTeam;
import com.guflimc.teams.common.domain.traits.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractTeamManager implements TeamManager {

    private final Logger logger = LoggerFactory.getLogger(AbstractTeamManager.class);

    protected final TeamsConfig config;
    protected final TeamsDatabaseContext databaseContext;

    private final Set<DTeam> teams = new CopyOnWriteArraySet<>();
    private final Set<DProfile> profiles = new CopyOnWriteArraySet<>();

    public AbstractTeamManager(TeamsConfig config, TeamsDatabaseContext databaseContext) {
        this.config = config;
        this.databaseContext = databaseContext;
        reload();
    }

    @Override
    public void reload() {
        logger.info("Reloading team manager...");

        teams.clear();
        teams.addAll(databaseContext.findAllAsync(DTeam.class).join());
        teams.forEach(this::configure);
    }

    private void configure(DTeam team) {
        for (TeamsConfig.TeamTypeConfig ttc : config.teamTypes ) {
            if ( !team.type().name().equals(ttc.name) ) {
                continue;
            }

            team.addTrait(new BrickTeamPermissionTrait(team));

            if ( ttc.colorTrait ) {
                team.addTrait(new BrickTeamColorTrait(team));
            }
            if ( ttc.inviteTrait ) {
                team.addTrait(new BrickTeamInviteTrait(team));
            }
            if ( ttc.tagTrait ) {
                team.addTrait(new BrickTeamTagTrait(team, ttc.maxTagLength));
            }
            if ( ttc.memberLimitTrait ) {
                team.addTrait(new BrickTeamMemberLimitTrait(team, ttc.defaultMemberLimit));
            }

            break;
        }
    }

    //

    @Override
    public Collection<Team> teams(@NotNull TeamType type) {
        return teams.stream()
                .filter(c -> c.type() == type)
                .map(t -> (Team) t)
                .toList();
    }

    @Override
    public Optional<Team> team(@NotNull TeamType type, @NotNull String name) {
        return teams.stream()
                .filter(c -> c.type() == type)
                .filter(c -> c.name().equalsIgnoreCase(name))
                .findFirst().map(c -> c);
    }

    @Override
    public Optional<Team> team(@NotNull TeamType type, @NotNull UUID id) {
        return teams.stream()
                .filter(c -> c.type() == type)
                .filter(c -> c.id().equals(id))
                .findFirst().map(c -> c);
    }

    @Override
    public CompletableFuture<Team> create(@NotNull TeamType type, @NotNull String name) {
        if (teams(type).stream().anyMatch(c -> c.name().equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("A team of that type with that name already exists.");
        }

        DTeam team = new DTeam(type, name);
        teams.add(team);
        configure(team);

        return databaseContext.persistAsync(team).thenApply(n -> {
            EventManager.INSTANCE.onCreate(team);
            return team;
        });
    }

    @Override
    public CompletableFuture<Profile> profile(@NotNull UUID id) {
        Profile profile = profiles.stream()
                .filter(p -> p.id().equals(id))
                .findFirst().orElse(null);
        if (profile != null) {
            return CompletableFuture.completedFuture(profile);
        }
        return databaseContext.findAsync(DProfile.class, id)
                .thenApply(p -> p);
    }

    @Override
    public CompletableFuture<Profile> profile(@NotNull String name) {
        Profile profile = profiles.stream()
                .filter(p -> p.name().equalsIgnoreCase(name))
                .findFirst().orElse(null);
        if (profile != null) {
            return CompletableFuture.completedFuture(profile);
        }
        return databaseContext.findAllWhereAsync(DProfile.class, "name", name)
                .thenApply(p -> p.isEmpty() ? null : p.get(0));
    }

    @Override
    public CompletableFuture<Void> remove(@NotNull Team team) {
        teams.remove((DTeam) team);

        Set<CompletableFuture<?>> futures = new HashSet<>();

        // online players will leave team
        profiles.stream().map(p -> p.membership(team).orElse(null))
                .filter(Objects::nonNull)
                .forEach(membership -> {
                    membership.quit();
                    futures.add(update(membership.profile()));
                });

        return databaseContext.removeAsync(team).thenCompose(n ->
                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)))
                .thenRun(() -> EventManager.INSTANCE.onDelete(team));
    }

    @Override
    public CompletableFuture<Void> update(@NotNull Team team) {
        return databaseContext.persistAsync(team);
    }

    @Override
    public CompletableFuture<Void> update(@NotNull Profile profile) {
        return databaseContext.persistAsync(profile);
    }

    @Override
    public CompletableFuture<Void> update(@NotNull Membership membership) {
        return databaseContext.persistAsync(membership);
    }

    // edit profiles

    public CompletableFuture<Profile> login(@NotNull UUID id, @NotNull String name) {
        return profile(id).thenApply(p -> {
            // update name change
            ((DProfile) p).setName(name);
            databaseContext.persistAsync(p);
            return p;
        }).exceptionally(ex -> {
            // not found, create new profile
            DProfile profile = new DProfile(id, name);
            databaseContext.persistAsync(profile);
            return profile;
        }).thenApply(p -> {
            // add to cache
            DProfile dp = (DProfile) p;
            profiles.add(dp);
            return p;
        });
    }

    public void logout(@NotNull UUID id) {
        profiles.removeIf(p -> p.id().equals(id));
    }
}
