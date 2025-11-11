package com.reactive.preypredator.config;

/**
 * Configuration parameters tuned for classic Lotka-Volterra oscillating dynamics
 *
 * KEY PRINCIPLES FOR OSCILLATIONS:
 * 1. Prey must reproduce FASTER than predators
 * 2. Predators must have HIGHER energy costs (forcing dependence on prey)
 * 3. Grass must regrow fast enough to sustain prey recovery
 * 4. Vision ranges create the "lag effect" - predators don't immediately find all prey
 */
public class Config {

    // ================= GRID =================
    public static final int GRID_WIDTH = 40;
    public static final int GRID_HEIGHT = 40;

    // ================= INITIAL POPULATIONS =================
    // Start with MORE prey than predators (typical ratio 3:1 to 5:1)
    public static final int INITIAL_PREY_COUNT = 100;
    public static final int INITIAL_PREDATOR_COUNT = 25;

    // ================= PREY PARAMETERS =================
    // Prey: Fast reproduction, low energy costs
    public static final int PREY_ENERGY_START = 50;
    public static final int PREY_ENERGY_MAX = 100;
    public static final int PREY_VISION_RANGE = 6;           // Good grass detection
    public static final int PREY_MOVE_COST = 1;              // VERY LOW - prey are efficient
    public static final int PREY_GRASS_GAIN = 12;            // Good energy from grass
    public static final int PREY_REPRODUCTION_COOLDOWN = 35; // FASTER reproduction
    public static final double PREY_REPRODUCTION_ENERGY_COST = 1.3;
    public static final int PREY_MIN_REPRODUCTION_ENERGY = 50; // Lower threshold
    public static final int PREY_STARVATION_LIMIT = 40;      // More resilient to starvation

    // ================= PREDATOR PARAMETERS =================
    // Predators: Slower reproduction, HIGH energy costs (creates lag)
    public static final int PREDATOR_ENERGY_START = 95;
    public static final int PREDATOR_ENERGY_MAX = 140;
    public static final int PREDATOR_VISION_RANGE = 7;         // Slightly better than prey
    public static final double PREDATOR_MOVE_COST = 5.2;       // LOWER than before - too high kills them
    public static final int PREDATOR_HUNT_GAIN = 25;           // Good reward for hunting
    public static final double PREDATOR_ATTACK_SUCCESS_RATE = 0.5; // Better success rate
    public static final int PREDATOR_REPRODUCTION_COOLDOWN = 50; // SLOWER reproduction (lag effect)
    public static final double PREDATOR_REPRODUCTION_ENERGY_COST = 1.4;
    public static final int PREDATOR_MIN_REPRODUCTION_ENERGY = 58; // Higher threshold
    public static final int PREDATOR_STARVATION_LIMIT = 20;    // More time to find food

    // ================= ENVIRONMENT PARAMETERS =================
    // Fast grass regrowth = stable prey population base
    public static final int GRASS_REGROWTH_TIME = 18;          // FASTER regrowth
    public static final double INITIAL_GRASS_COVERAGE = 0.4;  // MORE grass
    public static final double OBSTACLE_COVERAGE = 0.02;       // FEWER obstacles

    // ================= SIMULATION PARAMETERS =================
    public static final int TICK_DURATION_MS = 80;             // Slightly faster
    public static final int MAX_TICKS = 8000;                  // Longer to see multiple cycles
    public static final int CELL_SIZE = 18;
    public static final int UI_WIDTH = GRID_WIDTH * CELL_SIZE;
    public static final int UI_HEIGHT = GRID_HEIGHT * CELL_SIZE + 150;

    // ================= LOTKA-VOLTERRA TUNING NOTES =================
    /*
     * ACHIEVING OSCILLATIONS:
     *
     * 1. PREY BOOM PHASE:
     *    - Low predators + abundant grass = prey reproduce rapidly
     *    - Prey population increases exponentially
     *
     * 2. PREDATOR RESPONSE (LAG):
     *    - More prey = more food for predators
     *    - Predators reproduce (but SLOWER due to cooldown=50 vs prey=25)
     *    - This delay creates the phase shift
     *
     * 3. PREDATOR BOOM / PREY CRASH:
     *    - Many predators hunt efficiently
     *    - Prey population decreases rapidly
     *
     * 4. PREDATOR CRASH:
     *    - Few prey = predators starve
     *    - High movement costs accelerate predator decline
     *
     * 5. RECOVERY:
     *    - Few predators = prey can reproduce safely
     *    - Fast grass regrowth supports recovery
     *    - Cycle repeats
     *
     * KEY RATIOS FOR OSCILLATIONS:
     * - Prey reproduction cooldown / Predator reproduction cooldown ≈ 0.5
     * - Initial Prey / Initial Predators ≈ 4:1
     * - Predator move cost should be 2-4x prey move cost
     * - Grass regrowth should be < 25 ticks for stable cycles
     *
     * TROUBLESHOOTING:
     * - If predators die out: Increase PREDATOR_HUNT_GAIN or decrease PREDATOR_MOVE_COST
     * - If prey die out: Increase GRASS_REGROWTH or INITIAL_GRASS_COVERAGE
     * - If no oscillations: Increase difference between reproduction cooldowns
     * - If oscillations decay: Adjust energy costs and gains for balance
     */
}