package com.reactive.preypredator.model;

import java.util.Random;

/**
 * Gender enum for agents
 */
public enum Gender {
    MALE,
    FEMALE;

    private static final Random random = new Random();

    public static Gender random() {
        return random.nextBoolean() ? MALE : FEMALE;
    }
}