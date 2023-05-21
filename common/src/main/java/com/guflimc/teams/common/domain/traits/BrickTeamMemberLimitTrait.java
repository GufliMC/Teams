package com.guflimc.teams.common.domain.traits;

import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.TeamAttributeKey;
import com.guflimc.teams.api.domain.traits.TeamMemberLimitTrait;

public class BrickTeamMemberLimitTrait implements TeamMemberLimitTrait {

    private final static TeamAttributeKey<Integer> TEAM_MEMBER_LIMIT = new TeamAttributeKey<>("TEAM_MEMBER_LIMIT", Integer.class,
            i -> i + "", Integer::parseInt);

    private final Team team;
    private final int defaultMemberLimit;

    public BrickTeamMemberLimitTrait(Team team, int defaultMemberLimit) {
        this.team = team;
        this.defaultMemberLimit = Math.max(0, defaultMemberLimit);
    }

    @Override
    public int memberLimit() {
        return team.attribute(TEAM_MEMBER_LIMIT).orElse(defaultMemberLimit);
    }

    @Override
    public void setMemberLimit(int limit) {
        team.setAttribute(TEAM_MEMBER_LIMIT, limit);
    }

}
