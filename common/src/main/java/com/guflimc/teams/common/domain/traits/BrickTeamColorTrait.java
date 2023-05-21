package com.guflimc.teams.common.domain.traits;

import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.TeamAttributeKey;
import com.guflimc.teams.api.domain.traits.TeamColorTrait;

public class BrickTeamColorTrait implements TeamColorTrait {

    private final static TeamAttributeKey<Integer> TEAM_COLOR = new TeamAttributeKey<>("BRICK_TEAM_COLOR", Integer.class,
            i -> i + "", Integer::parseInt);

    private final Team team;

    public BrickTeamColorTrait(Team team) {
        this.team = team;
    }

    @Override
    public int rgbColor() {
        return team.attribute(TEAM_COLOR).orElse(0);
    }

    @Override
    public void setRgbColor(int rgb) {
        team.setAttribute(TEAM_COLOR, rgb);
    }
}
