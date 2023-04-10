package com.guflimc.teams.api.domain.traits;

import com.guflimc.teams.api.domain.TeamTrait;
import net.kyori.adventure.text.Component;

public interface TeamTagTrait extends TeamTrait {

    Component tag();

    void setTag(Component tag);

}
