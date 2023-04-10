package com.guflimc.clans.spigot.api;

import com.guflimc.teams.api.TeamAPI;
import org.jetbrains.annotations.ApiStatus;

public class SpigotClanAPI {

    private SpigotClanAPI() {
    }

    private static SpigotClanManager clanManager;

    @ApiStatus.Internal
    public static void register(SpigotClanManager manager) {
        clanManager = manager;
        TeamAPI.register(manager);
    }

    //

    public static SpigotClanManager get() {
        return clanManager;
    }

}
