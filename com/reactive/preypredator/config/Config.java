package com.reactive.preypredator.config;

import com.reactive.preypredator.model.PlacementMode;

/**
 * Configuration adapted from continuous-space Lotka-Volterra system
 * KEY INSIGHT: LOW starvation + VERY SLOW reproduction = stable oscillations
 */
public class Config {

    // ============ GRID ============
    public static int GRID_WIDTH = 60;
    public static int GRID_HEIGHT = 60;

    public static PlacementMode PLACEMENT_MODE = PlacementMode.RANDOM;

    // ============ INITIAL POP ============
    public static int INITIAL_PREY_COUNT = 100;       // More prey for stability
    public static int INITIAL_PREDATOR_COUNT = 25;    // Moderate predators

    // ============ PREY ============ (adapted from friend's config)
    public static int PREY_ENERGY_START = 85;         // Match friend's
    public static int PREY_ENERGY_MAX = 120;          // Match friend's
    public static int PREY_ENERGY_FROM_GRASS = 40;    // Good grass reward
    public static double PREY_ENERGY_MOVE_COST = 1.0; // LOW like friend's (was 2.0)
    public static int PREY_REPRODUCTION_THRESHOLD = 85; // Match friend's
    public static int PREY_REPRODUCTION_COOLDOWN = 300; // LONG like friend's (was 40)
    public static int PREY_REPRODUCTION_COST = 40;     // Match friend's cost
    public static int PREY_STARVATION_THRESHOLD = 5;
    public static int PREY_VISION_RANGE = 10;         // ~100 in continuous = ~10 grid cells

    // ============ PREDATOR ============ (adapted from friend's config)
    public static int PREDATOR_ENERGY_START = 200;         // Match friend's
    public static int PREDATOR_ENERGY_MAX = 300;           // Match friend's
    public static int PREDATOR_ENERGY_FROM_PREY = 50;      // Match friend's gain
    public static double PREDATOR_ENERGY_MOVE_COST = 1.0;  // LOW like friend's (was 8.0!)
    public static int PREDATOR_REPRODUCTION_THRESHOLD = 180; // Match friend's
    public static int PREDATOR_REPRODUCTION_COOLDOWN = 800;  // VERY LONG like friend's (was 50)
    public static int PREDATOR_REPRODUCTION_COST = 70;       // Match friend's cost
    public static int PREDATOR_STARVATION_THRESHOLD = 15;
    public static int PREDATOR_MAX_TICKS_WITHOUT_FOOD = 50;  // Much higher (was 4)
    public static int PREDATOR_VISION_RANGE = 11;            // ~110 continuous = ~11 grid
    public static int PREDATOR_EATING_COOLDOWN = 10;         // NEW: prevent rapid kills

    // ============ ENV ============
    public static int GRASS_REGROWTH_TICKS = 25;        // Moderate regrowth
    public static double GRASS_INITIAL_COVERAGE = 0.60; // More grass
    public static double OBSTACLE_COVERAGE = 0.05;

    // ============ SIM ============
    public static int TICK_DURATION_MS = 100;           // Faster ticks
    public static int MAX_TICKS = 5000;
    public static String CSV_OUTPUT_FILE = "simulation_data.csv";

    // ============ UI ============
    public static final int CELL_SIZE = 15;
    public static final int UI_WIDTH = GRID_WIDTH * CELL_SIZE;
    public static final int UI_HEIGHT = GRID_HEIGHT * CELL_SIZE;

    public static void resetToDefaults() {
        GRID_WIDTH = 60;
        GRID_HEIGHT = 60;
        PLACEMENT_MODE = PlacementMode.FIXED_PATTERN;

        INITIAL_PREY_COUNT = 100;
        INITIAL_PREDATOR_COUNT = 25;

        PREY_ENERGY_START = 85;
        PREY_ENERGY_MAX = 120;
        PREY_ENERGY_FROM_GRASS = 40;
        PREY_ENERGY_MOVE_COST = 1.0;
        PREY_REPRODUCTION_THRESHOLD = 85;
        PREY_REPRODUCTION_COOLDOWN = 300;
        PREY_REPRODUCTION_COST = 40;
        PREY_STARVATION_THRESHOLD = 5;
        PREY_VISION_RANGE = 10;

        PREDATOR_ENERGY_START = 200;
        PREDATOR_ENERGY_MAX = 300;
        PREDATOR_ENERGY_FROM_PREY = 50;
        PREDATOR_ENERGY_MOVE_COST = 1.0;
        PREDATOR_REPRODUCTION_THRESHOLD = 180;
        PREDATOR_REPRODUCTION_COOLDOWN = 800;
        PREDATOR_REPRODUCTION_COST = 70;
        PREDATOR_STARVATION_THRESHOLD = 15;
        PREDATOR_MAX_TICKS_WITHOUT_FOOD = 50;
        PREDATOR_VISION_RANGE = 11;
        PREDATOR_EATING_COOLDOWN = 10;

        GRASS_REGROWTH_TICKS = 25;
        GRASS_INITIAL_COVERAGE = 0.60;
        OBSTACLE_COVERAGE = 0.05;

        TICK_DURATION_MS = 100;
        MAX_TICKS = 5000;
    }
}
