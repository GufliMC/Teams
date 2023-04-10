package com.guflimc.clans.spigot.api.events;

import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.Profile;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ProfileClanInviteDeleteEvent extends ProfileClanEvent {

    public ProfileClanInviteDeleteEvent(Team team, Profile profile, boolean async) {
        super(team, profile, async);
    }

    //

    private static final HandlerList handlers = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
