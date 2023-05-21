package com.guflimc.teams.api.domain;

import java.time.Instant;
import java.util.Optional;

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

    <T> void setAttribute(MembershipAttributeKey<T> key, T value);

    <T> void removeAttribute(MembershipAttributeKey<T> key);

    <T> Optional<T> attribute(MembershipAttributeKey<T> key);

}
