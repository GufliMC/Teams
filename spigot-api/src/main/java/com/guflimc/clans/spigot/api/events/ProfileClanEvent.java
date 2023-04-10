package com.guflimc.clans.spigot.api.events;

import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.Profile;

public abstract class ProfileClanEvent extends ClanEvent {

    private final Profile profile;

    public ProfileClanEvent(Team team, Profile profile, boolean async) {
        super(team, async);
        this.profile = profile;
    }

    public Profile profile() {
        return profile;
    }

}
