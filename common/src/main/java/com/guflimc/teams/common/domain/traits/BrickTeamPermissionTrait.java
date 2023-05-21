package com.guflimc.teams.common.domain.traits;

import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.api.domain.ProfileAttributeKey;
import com.guflimc.teams.api.domain.traits.TeamPermissionTrait;
import com.guflimc.teams.common.domain.DTeam;
import org.jetbrains.annotations.NotNull;

public class BrickTeamPermissionTrait implements TeamPermissionTrait {

    private final DTeam team;

    public BrickTeamPermissionTrait(DTeam team) {
        this.team = team;
    }

    @Override
    public void addPermission(@NotNull Profile profile, @NotNull String permission) {
        profile.setAttribute(permission(permission), true);
    }

    @Override
    public void removePermission(@NotNull Profile profile, @NotNull String permission) {
        profile.removeAttribute(permission(permission));
    }

    @Override
    public boolean hasPermission(@NotNull Profile profile, @NotNull String permission) {
        return profile.attribute(permission(permission)).orElse(false);
    }

    //

    private ProfileAttributeKey<Boolean> permission(@NotNull String permission) {
        return new ProfileAttributeKey<>("TEAM_PERMISSION_" + team.id() + "_" + permission, Boolean.class,
                b -> b + "", Boolean::parseBoolean);
    }
}
