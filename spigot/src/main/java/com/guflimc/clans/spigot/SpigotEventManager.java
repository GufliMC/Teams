package com.guflimc.clans.spigot;

import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.common.EventManager;
import com.guflimc.clans.spigot.api.events.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.util.function.Supplier;

public class SpigotEventManager extends EventManager {

    private void wrap(Supplier<Event> supplier) {
        try {
            Bukkit.getServer().getPluginManager().callEvent(supplier.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Team team) {
        wrap(() -> new ClanCreateEvent(team, !Bukkit.isPrimaryThread()));
    }

    @Override
    public void onDelete(Team team) {
        wrap(() -> new ClanDeleteEvent(team, !Bukkit.isPrimaryThread()));
    }

    @Override
    public void onJoin(Profile profile, Team team) {
        wrap(() -> new ProfileClanJoinEvent(team, profile, !Bukkit.isPrimaryThread()));
    }

    @Override
    public void onLeave(Profile profile, Team team) {
        wrap(() -> new ProfileClanLeaveEvent(team, profile, !Bukkit.isPrimaryThread()));
    }

    @Override
    public void onInvite(Profile profile, Team team) {
        wrap(() -> new ProfileClanInviteEvent(team, profile, !Bukkit.isPrimaryThread()));
    }

    @Override
    public void onInviteDelete(Profile profile, Team team) {
        wrap(() -> new ProfileClanInviteDeleteEvent(team, profile, !Bukkit.isPrimaryThread()));
    }

    @Override
    public void onInviteReject(Profile profile, Team team) {
        wrap(() -> new ProfileClanInviteRejectEvent(team, profile, !Bukkit.isPrimaryThread()));
    }

}
