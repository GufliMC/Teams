package com.guflimc.teams.api.domain;

import com.guflimc.brick.orm.api.attributes.AttributeKey;

import java.util.function.Function;

public class TeamAttributeKey<T> extends AttributeKey<T> {

    public TeamAttributeKey(String name, Class<T> type, Function<T, String> serializer, Function<String, T> deserializer) {
        super(name, type, serializer, deserializer);
    }

}