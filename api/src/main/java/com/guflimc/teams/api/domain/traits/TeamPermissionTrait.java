package com.guflimc.teams.api.domain.traits;

import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.api.domain.TeamTrait;
import org.jetbrains.annotations.NotNull;

public interface TeamPermissionTrait extends TeamTrait {

    void addPermission(@NotNull Profile profile, @NotNull String permission);

    void removePermission(@NotNull Profile profile, @NotNull String permission);

    boolean hasPermission(@NotNull Profile profile, @NotNull String permission);

}
