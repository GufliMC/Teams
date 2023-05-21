package com.guflimc.teams.api.domain;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface Profile {

    UUID id();

    String name();

    Collection<Membership> memberships();

    Optional<Membership> membership(@NotNull TeamType team);

    Optional<Membership> membership(@NotNull Team team);

    Instant createdAt();

    // actions

    void join(@NotNull Team team);

    // attributes

    <T> void setAttribute(ProfileAttributeKey<T> key, T value);

    <T> void removeAttribute(ProfileAttributeKey<T> key);

    <T> Optional<T> attribute(ProfileAttributeKey<T> key);


}
