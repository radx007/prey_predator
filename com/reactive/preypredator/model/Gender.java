package com.reactive.preypredator.model;

import java.util.Random;

/**
 * Gender enum for reproduction mechanics
 */
public enum Gender {
    MALE, FEMALE;

    private static final Random random = new Random();

    public static Gender random() {
        return random.nextBoolean() ? MALE : FEMALE;
    }
}
