package com.guflimc.teams.api.domain;

import com.guflimc.brick.orm.api.attributes.AttributeKey;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public interface Team {

    UUID id();

    String name();

    int members();

    Instant createdAt();

    // actions

//    void addTrait(TeamTrait trait);
//
//    <T extends TeamTrait> void removeTrait(Class<T> type);

    <T extends TeamTrait> boolean hasTrait(Class<T> type);

    <T extends TeamTrait> Optional<T> trait(Class<T> type);

    // attributes

    <T> void setAttribute(TeamAttributeKey<T> key, T value);

    <T> void removeAttribute(TeamAttributeKey<T> key);

    <T> Optional<T> attribute(TeamAttributeKey<T> key);

    class TeamAttributeKey<T> extends AttributeKey<T> {

        public TeamAttributeKey(String name, Class<T> type, Function<T, String> serializer, Function<String, T> deserializer) {
            super(name, type, serializer, deserializer);
        }

    }

}
