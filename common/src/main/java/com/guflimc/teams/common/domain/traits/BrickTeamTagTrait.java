package com.guflimc.teams.common.domain.traits;

import com.guflimc.teams.api.domain.Team;
import com.guflimc.teams.api.domain.TeamAttributeKey;
import com.guflimc.teams.api.domain.traits.TeamTagTrait;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class BrickTeamTagTrait implements TeamTagTrait {

    private final static TeamAttributeKey<Component> TEAM_TAG = new TeamAttributeKey<>("BRICK_TEAM_TAG", Component.class,
            GsonComponentSerializer.gson()::serialize, GsonComponentSerializer.gson()::deserialize);

    private final Team team;
    private final int maxTagLength;

    public BrickTeamTagTrait(Team team, int maxTagLength) {
        this.team = team;
        this.maxTagLength = maxTagLength;
    }

    @Override
    public Component tag() {
        return team.attribute(TEAM_TAG).orElse(Component.text(""));
    }

    @Override
    public void setTag(Component tag) {
        String str = PlainTextComponentSerializer.plainText().serialize(tag);
        if ( str.length() < 2 || str.length() > maxTagLength) {
            throw new IllegalArgumentException("Tag must be between 2 and " + maxTagLength + " characters long.");
        }

        team.setAttribute(TEAM_TAG, tag);
    }
}
