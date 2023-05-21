package com.guflimc.teams.common.domain.traits;

import com.guflimc.teams.api.domain.Profile;
import com.guflimc.teams.api.domain.traits.TeamInviteTrait;
import com.guflimc.teams.common.domain.DProfile;
import com.guflimc.teams.common.domain.DTeam;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public class BrickTeamInviteTrait implements TeamInviteTrait {

    private final DTeam team;

    public BrickTeamInviteTrait(DTeam team) {
        this.team = team;
    }

    @Override
    public void invite(@NotNull Profile sender, @NotNull Profile target) {
        team.addInvite((DProfile) sender, (DProfile) target);
    }

    @Override
    public Collection<TeamInvite> invites() {
        return team.invites().stream().map(inv -> (TeamInvite) inv).toList();
    }

    @Override
    public Optional<TeamInvite> invite(@NotNull Profile target) {
        return team.invite((DProfile) target).map(inv -> inv);
    }
}
