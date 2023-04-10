package com.guflimc.clans.spigot.api;

import com.guflimc.teams.api.TeamAPI;
import com.guflimc.teams.api.TeamManager;
import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.Membership;
import com.guflimc.teams.api.domain.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface SpigotClanManager extends TeamManager {

    default Collection<Player> onlinePlayers(@NotNull Team team) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> TeamAPI.get().findCachedProfile(p.getUniqueId())
                        .flatMap(Profile::clanProfile)
                        .map(Membership::clan)
                        .filter(c -> c.equals(team))
                        .isPresent()
                ).map(Player.class::cast).toList();
    }

    default Optional<Team> findClan(@NotNull Player player) {
        return TeamAPI.get()
                .findCachedProfile(player.getUniqueId())
                .flatMap(Profile::clanProfile)
                .map(Membership::clan);
    }

    default Optional<Membership> clanProfile(@NotNull Player player) {
        return TeamAPI.get().findCachedProfile(player.getUniqueId())
                .flatMap(Profile::clanProfile);
    }

    ItemStack crest(@NotNull Team team);

}
