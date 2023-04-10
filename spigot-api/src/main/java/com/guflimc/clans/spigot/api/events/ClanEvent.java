package com.guflimc.clans.spigot.api.events;

import com.guflimc.teams.api.domain.Team;
import org.bukkit.event.Event;

public abstract class ClanEvent extends Event {

    private final Team team;

    public ClanEvent(Team team, boolean async) {
        super(async);
        this.team = team;
    }

    public Team clan() {
        return team;
    }

}
