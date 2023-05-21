package com.guflimc.teams.api.domain;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TeamType {

    private final static Map<String, TeamType> TYPES = new ConcurrentHashMap<>();

    private final String name;

    private TeamType(String name) {
        this.name = name;
    }

    //

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TeamType t && t.name.equals(name);
    }

    //

    public static TeamType of(@NotNull String name) {
        name = name.toLowerCase();
        return TYPES.computeIfAbsent(name, TeamType::new);
    }


}
