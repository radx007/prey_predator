package com.reactive.preypredator.config;

import com.reactive.preypredator.model.PlacementMode;

/**
 * AGGRESSIVE REBALANCE - Predators must be able to control prey
 */
public class Config {
    // ================= GRID =================
    public static int GRID_WIDTH = 50;
    public static int GRID_HEIGHT = 50;

    // ================= PLACEMENT MODE =================
    public static PlacementMode PLACEMENT_MODE = PlacementMode.FIXED_PATTERN;

    // ================= INITIAL POPULATIONS =================
    public static int INITIAL_PREY_COUNT = 100;  // Reduced
    public static int INITIAL_PREDATOR_COUNT = 25;  // Increased

    // ================= PREY PARAMETERS =================
    // HEAVILY NERFED - Prey reproducing way too fast
    public static int PREY_ENERGY_START = 120;
    public static int PREY_ENERGY_MAX = 180;
    public static int PREY_ENERGY_FROM_GRASS = 40;  // Reduced from 48
    public static double PREY_ENERGY_MOVE_COST = 1.0;  // Increased from 0.7
    public static int PREY_REPRODUCTION_THRESHOLD = 100;  // Much harder (was 85)
    public static int PREY_REPRODUCTION_COOLDOWN = 6;  // Much slower (was 4)
    public static int PREY_STARVATION_THRESHOLD = 5;
    public static int PREY_VISION_RANGE = 7;

    // ================= PREDATOR PARAMETERS =================
    // HEAVILY BUFFED - Predators need to hunt effectively
    public static int PREDATOR_ENERGY_START = 110;  // Higher
    public static int PREDATOR_ENERGY_MAX = 160;  // Higher (was 140)
    public static int PREDATOR_ENERGY_FROM_PREY = 80;  // Much higher reward (was 70)
    public static double PREDATOR_ENERGY_MOVE_COST = 2.5;  // Lower cost (was 3.5)
    public static int PREDATOR_REPRODUCTION_THRESHOLD = 110;  // Easier (was 120)
    public static int PREDATOR_REPRODUCTION_COOLDOWN = 5;  // Faster (was 7)
    public static int PREDATOR_STARVATION_THRESHOLD = 12;  // Medium (was 15)
    public static int PREDATOR_VISION_RANGE = 8;  // Increased (was 6)

    // ================= ENVIRONMENT =================
    public static int GRASS_REGROWTH_TICKS = 15;  // Slower (was 11)
    public static double GRASS_INITIAL_COVERAGE = 0.75;  // Less grass (was 0.82)
    public static double OBSTACLE_COVERAGE = 0.04;  // More obstacles

    // ================= SIMULATION =================
    public static int TICK_DURATION_MS = 500;
    public static int MAX_TICKS = 5000;
    public static String CSV_OUTPUT_FILE = "simulation_data.csv";

    // ================= UI =================
    public static final int CELL_SIZE = 15;
    public static final int UI_WIDTH = GRID_WIDTH * CELL_SIZE;
    public static final int UI_HEIGHT = GRID_HEIGHT * CELL_SIZE;

    public static void resetToDefaults() {
        GRID_WIDTH = 50;
        GRID_HEIGHT = 50;
        PLACEMENT_MODE = PlacementMode.FIXED_PATTERN;
        INITIAL_PREY_COUNT = 100;
        INITIAL_PREDATOR_COUNT = 25;
        PREY_ENERGY_START = 120;
        PREY_ENERGY_MAX = 180;
        PREY_ENERGY_FROM_GRASS = 40;
        PREY_ENERGY_MOVE_COST = 1.0;
        PREY_REPRODUCTION_THRESHOLD = 100;
        PREY_REPRODUCTION_COOLDOWN = 6;
        PREY_STARVATION_THRESHOLD = 5;
        PREY_VISION_RANGE = 7;
        PREDATOR_ENERGY_START = 110;
        PREDATOR_ENERGY_MAX = 160;
        PREDATOR_ENERGY_FROM_PREY = 80;
        PREDATOR_ENERGY_MOVE_COST = 2.5;
        PREDATOR_REPRODUCTION_THRESHOLD = 110;
        PREDATOR_REPRODUCTION_COOLDOWN = 5;
        PREDATOR_STARVATION_THRESHOLD = 12;
        PREDATOR_VISION_RANGE = 8;
        GRASS_REGROWTH_TICKS = 15;
        GRASS_INITIAL_COVERAGE = 0.75;
        OBSTACLE_COVERAGE = 0.04;
        TICK_DURATION_MS = 500;
        MAX_TICKS = 5000;
    }
}
