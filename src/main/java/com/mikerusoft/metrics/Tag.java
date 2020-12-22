package com.mikerusoft.metrics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tag {

    public static Tag of(String key, String value) {
        return new Tag(key, value);
    }

    private final String key;
    private final String value;
}
