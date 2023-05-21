package com.guflimc.teams.common;

import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.api.domain.traits.TeamInviteTrait;

public abstract class EventManager {

    public static EventManager INSTANCE;

    //

    public abstract void onCreate(Team team);

    public abstract void onDelete(Team team);

    public abstract void onJoin(Profile profile, Team team);

    public abstract void onLeave(Profile profile, Team team);

    public abstract void onInviteSent(TeamInviteTrait.TeamInvite invite);

    public abstract void onInviteCancel(TeamInviteTrait.TeamInvite invite);

    public abstract void onInviteDecline(TeamInviteTrait.TeamInvite invite);

}
