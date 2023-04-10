package com.guflimc.teams.common;

import com.guflimc.teams.api.TeamManager;
import com.guflimc.teams.api.crest.CrestType;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.Membership;
import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.common.domain.DTeam;
import com.guflimc.teams.common.domain.DMembership;
import com.guflimc.teams.common.domain.DCrestTemplate;
import com.guflimc.teams.common.domain.DProfile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

public abstract class AbstractClanManager implements TeamManager {

    private final Logger logger = LoggerFactory.getLogger(AbstractClanManager.class);

    protected final ClansDatabaseContext databaseContext;

    private final Set<DTeam> clans = new CopyOnWriteArraySet<>();
    private final Set<DProfile> profiles = new CopyOnWriteArraySet<>();

    private final Set<DCrestTemplate> crestTemplates = new CopyOnWriteArraySet<>();

    public AbstractClanManager(ClansDatabaseContext databaseContext) {
        this.databaseContext = databaseContext;
        reload();
    }

    @Override
    public void reload() {
        logger.info("Reloading clan manager...");

        clans.clear();
        clans.addAll(databaseContext.findAllAsync(DTeam.class).join());

        crestTemplates.clear();
        crestTemplates.addAll(databaseContext.findAllAsync(DCrestTemplate.class).join());
    }

    // clans

    @Override
    public Collection<Team> clans() {
        return new ArrayList<>(clans);
    }

    @Override
    public Optional<Team> findClan(@NotNull String name) {
        return clans.stream()
                .filter(c -> c.name().equalsIgnoreCase(name))
                .findFirst().map(c -> c);
    }

    @Override
    public Optional<Team> findClan(@NotNull UUID id) {
        return clans.stream()
                .filter(c -> c.id().equals(id))
                .findFirst().map(c -> c);
    }

    @Override
    public Optional<Team> findClanByTag(@NotNull String tag) {
        return clans.stream()
                .filter(c -> c.tag().equalsIgnoreCase(tag))
                .findFirst().map(c -> c);
    }

    // edit clans

    @Override
    public CompletableFuture<Team> create(@NotNull Profile leader, @NotNull String name, @NotNull String tag) {
        if (name.length() < 3) {
            throw new IllegalArgumentException("Clan name must be at least 3 characters.");
        }
        if (tag.length() < 2 || tag.length() > 3) {
            throw new IllegalArgumentException("Clan tag must be exactly 2 or 3 characters.");
        }
        if (clans.stream().anyMatch(c -> c.name().equalsIgnoreCase(name)
                || c.tag().equalsIgnoreCase(tag))) {
            throw new IllegalArgumentException("A clan with that name or tag already exists.");
        }

        DTeam clan = new DTeam(name, tag);
        clans.add(clan);

        return databaseContext.persistAsync(clan).thenCompose(n -> {
                    leader.join(clan);
                    ((DMembership) leader.clanProfile().orElseThrow()).leader = true;

                    EventManager.INSTANCE.onCreate(clan);

                    return update(leader);
                })
                .thenApply(n -> (Team) clan)
                .exceptionally((ex) -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    @Override
    public CompletableFuture<Void> remove(@NotNull Team team) {
        clans.remove((DTeam) team);

        Set<CompletableFuture<?>> futures = new HashSet<>();

        // online players will leave clan
        profiles.stream().filter(p -> p.clanProfile().map(cp -> cp.clan().equals(team)).orElse(false))
                .forEach(p -> {
                    p.clanProfile().get().quit();
                    futures.add(update(p));
                });

        // TODO consistency check

        EventManager.INSTANCE.onDelete(team);

        return databaseContext.removeAsync(team).thenCompose(n ->
                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)));
    }

    @Override
    public CompletableFuture<Void> update(@NotNull Team team) {
        return databaseContext.persistAsync(team);
    }

    // profiles

    private CompletableFuture<Profile> findProfileBy(Predicate<Profile> predicate, String field, Object value) {
        Profile profile = profiles.stream().filter(predicate).findFirst().orElse(null);
        if (profile != null) {
            return CompletableFuture.completedFuture(profile);
        }
        return databaseContext.findAllWhereAsync(DProfile.class, field, value).thenCompose(pl -> {
            if (!pl.isEmpty()) {
                return CompletableFuture.completedFuture(pl.get(0));
            }
            return CompletableFuture.failedFuture(new NullPointerException("Profile does not exist."));
        });
    }

    @Override
    public CompletableFuture<List<Profile>> profiles(@NotNull Team team) {
        // TODO fix
        return databaseContext.findAllWhereAsync(DProfile.class, "clanProfile.clan.id", team.id())
                .thenApply(list -> list.stream().map(p -> (Profile) p).toList());
    }

    @Override
    public CompletableFuture<Profile> findProfile(@NotNull UUID id) {
        return findProfileBy(p -> p.id().equals(id), "id", id);
    }

    @Override
    public CompletableFuture<Profile> findProfile(@NotNull String name) {
        return findProfileBy(p -> p.name().equalsIgnoreCase(name), "name", name);
    }

    @Override
    public Optional<Profile> findCachedProfile(@NotNull UUID id) {
        return profiles.stream().filter(p -> p.id().equals(id)).map(p -> (Profile) p).findFirst();
    }

    @Override
    public Collection<Profile> cachedProfiles() {
        return Collections.unmodifiableSet(profiles);
    }

    // edit profiles

    public CompletableFuture<Profile> load(@NotNull UUID id, @NotNull String name) {
        return findProfile(id).thenApply(p -> {
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
            dp.setLastSeenAt(Instant.now());
            profiles.add(dp);
            // TODO call events
            return p;
        });
    }

    public void unload(@NotNull UUID id) {
        profiles.removeIf(p -> p.id().equals(id));
    }

    @Override
    public CompletableFuture<Void> update(@NotNull Profile profile) {
        return databaseContext.persistAsync(profile);
    }

    @Override
    public CompletableFuture<Void> update(@NotNull Membership membership) {
        return databaseContext.persistAsync(membership);
    }

    //


    @Override
    public Collection<CrestTemplate> crestTemplates() {
        return Collections.unmodifiableSet(crestTemplates);
    }

    public CompletableFuture<CrestTemplate> addCrestTemplate(@NotNull String name, @NotNull CrestType type, boolean restricted) {
        DCrestTemplate pattern = new DCrestTemplate(name, type, restricted);
        return databaseContext.persistAsync(pattern).thenApply(n -> {
            crestTemplates.add(pattern);
            return pattern;
        });
    }

    @Override
    public CompletableFuture<Void> removeCrestTemplate(@NotNull CrestTemplate crestTemplate) {
        crestTemplates.remove((DCrestTemplate) crestTemplate);
        return databaseContext.removeAsync(crestTemplate);
    }
}
