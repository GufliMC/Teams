package com.guflimc.clans.spigot.api.events;

import com.guflimc.teams.api.domain.Team;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ClanCreateEvent extends ClanEvent {

    public ClanCreateEvent(Team team, boolean async) {
        super(team, async);
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
