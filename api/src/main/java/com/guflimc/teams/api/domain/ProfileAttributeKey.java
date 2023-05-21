package com.guflimc.teams.api.domain;

import com.guflimc.brick.orm.api.attributes.AttributeKey;

import java.util.function.Function;

public class ProfileAttributeKey<T> extends AttributeKey<T> {

    public ProfileAttributeKey(String name, Class<T> type, Function<T, String> serializer, Function<String, T> deserializer) {
        super(name, type, serializer, deserializer);
    }

}