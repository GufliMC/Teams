package com.guflimc.teams.api.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface Team {

    UUID id();

    TeamType type();

    String name();

    int members();

    Instant createdAt();

    // actions

    <T extends TeamTrait> void addTrait(T trait);

    <T extends TeamTrait> void removeTrait(Class<T> type);

    <T extends TeamTrait> Optional<T> trait(Class<T> type);

    // attributes

    <T> void setAttribute(TeamAttributeKey<T> key, T value);

    <T> void removeAttribute(TeamAttributeKey<T> key);

    <T> Optional<T> attribute(TeamAttributeKey<T> key);


}
