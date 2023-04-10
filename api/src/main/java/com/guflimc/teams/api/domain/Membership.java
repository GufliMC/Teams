package com.guflimc.teams.api.domain;

import com.guflimc.brick.orm.api.attributes.AttributeKey;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

/**
 * Junction between Profile and Team. Represents a Profile's membership in a Team.
 */
public interface Membership {

    Profile profile();

    Team team();

    Instant createdAt();

    // actions

    void quit();

    //

    // attributes

    <T> void setAttribute(TeamProfileAttributeKey<T> key, T value);

    <T> void removeAttribute(TeamProfileAttributeKey<T> key);

    <T> Optional<T> attribute(TeamProfileAttributeKey<T> key);

    class TeamProfileAttributeKey<T> extends AttributeKey<T> {

        public TeamProfileAttributeKey(String name, Class<T> type, Function<T, String> serializer, Function<String, T> deserializer) {
            super(name, type, serializer, deserializer);
        }

    }

}
