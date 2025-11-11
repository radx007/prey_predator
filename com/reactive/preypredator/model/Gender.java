package com.reactive.preypredator.model;

import java.util.Random;

/**
 * Enum representing the gender of agents for reproduction
 */
public enum Gender {
    MALE,
    FEMALE;

    private static final Random random = new Random();

    /**
     * Returns a random gender
     */
    public static Gender random() {
        return random.nextBoolean() ? MALE : FEMALE;
    }
}