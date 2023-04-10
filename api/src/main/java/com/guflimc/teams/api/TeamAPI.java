package com.guflimc.teams.api;

import org.jetbrains.annotations.ApiStatus;

public class TeamAPI {

    private TeamAPI() {}

    private static TeamManager teamManager;

    @ApiStatus.Internal
    public static void register(TeamManager manager) {
        teamManager = manager;
    }

    //

    public static TeamManager get() {
        return teamManager;
    }
    
}
