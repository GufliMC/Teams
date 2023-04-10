package com.guflimc.teams.common;

import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.Profile;

public abstract class EventManager {

    public static EventManager INSTANCE;

    //

    public abstract void onCreate(Team team);

    public abstract void onDelete(Team team);

    public abstract void onJoin(Profile profile, Team team);

    public abstract void onLeave(Profile profile, Team team);

    public abstract void onInvite(Profile profile, Team team);

    public abstract void onInviteDelete(Profile profile, Team team);

    public abstract void onInviteReject(Profile profile, Team team);

}
