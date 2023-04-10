package com.guflimc.teams.api.domain.traits;

import com.guflimc.teams.api.domain.TeamTrait;

public interface TeamMemberLimitTrait extends TeamTrait {

    int memberLimit();

    void setMemberLimit(int limit);

}
