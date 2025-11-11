package com.reactive.preypredator.config;

/**
 * Full Configuration for a reactive predator-prey simulation
 * BALANCED SETTINGS with Dynamic Adjustments for stable Lotka-Volterra oscillations
 */
public class Config {
    // ================= GRID =================
    public static final int GRID_WIDTH = 50;
    public static final int GRID_HEIGHT = 50;
    public static final int CELL_SIZE = 12;

    // ================= UI =================
    public static final int UI_WIDTH = GRID_WIDTH * CELL_SIZE;
    public static final int UI_HEIGHT = GRID_HEIGHT * CELL_SIZE + 150;
    public static final int TICK_DELAY = 1000; // ms

    // ================= INITIAL POPULATIONS =================
    public static final int INITIAL_PREY_COUNT = 1;
    public static final int INITIAL_PREDATOR_COUNT = 1;
    public static final int OBSTACLE_COUNT = 50;

    // ================= PREY PARAMETERS =================
    public static final int PREY_ENERGY_MAX = 40;
    public static final int PREY_ENERGY_START = 30;
    public static final int PREY_ENERGY_LOSS = 1;
    public static final int PREY_ENERGY_GAIN_GRASS = 10;
    public static final int PREY_REPRODUCTION_THRESHOLD = 25;
    public static final int PREY_REPRODUCTION_COOLDOWN = 15;
    public static final int PREY_STARVATION_LIMIT = 60;
    public static final int PREY_VISION_RANGE = 6;

    // ================= PREDATOR PARAMETERS =================
    public static final int PREDATOR_ENERGY_MAX = 50;
    public static final int PREDATOR_ENERGY_START = 35;
    public static final int PREDATOR_ENERGY_LOSS = 1;
    public static final int PREDATOR_ENERGY_GAIN_PREY = 20;
    public static final int PREDATOR_REPRODUCTION_THRESHOLD = 35;
    public static final int PREDATOR_REPRODUCTION_COOLDOWN = 25;
    public static final int PREDATOR_STARVATION_LIMIT = 50;
    public static final double PREDATOR_CAPTURE_PROBABILITY = 0.5;
    public static final int PREDATOR_VISION_RANGE = 7;

    // ================= GRASS PARAMETERS =================
    public static final double GRASS_INITIAL_COVERAGE = 0.75;
    public static final int GRASS_REGROWTH_TIME = 12;

    // ================= STATISTICS =================
    public static final int STATS_HISTORY_SIZE = 500;
}

/**
 * DynamicConfig - Automatically adjusts parameters to prevent extinction
 * and maintain Lotka-Volterra oscillations.
 */
class DynamicConfig {
    // Current dynamic values
    public static double predatorCaptureProbability = Config.PREDATOR_CAPTURE_PROBABILITY;
    public static int preyReproductionCooldown = Config.PREY_REPRODUCTION_COOLDOWN;
    public static int predatorReproductionCooldown = Config.PREDATOR_REPRODUCTION_COOLDOWN;
    public static int grassRegrowthTime = Config.GRASS_REGROWTH_TIME;
    public static int preyEnergyGainGrass = Config.PREY_ENERGY_GAIN_GRASS;
    public static int predatorEnergyGainPrey = Config.PREDATOR_ENERGY_GAIN_PREY;

    // Smoothing
    private static final double FAST_LERP = 0.25;
    private static final double MEDIUM_LERP = 0.12;
    private static final double SLOW_LERP = 0.05;

    // Critical thresholds
    private static final int MIN_PREY = 10;
    private static final int MIN_PREDATOR = 5;
    private static final double CRITICAL_GRASS = 0.25;
    private static final double HIGH_GRASS = 0.80;

    private static double lerp(double current, double target, double speed) {
        return current + (target - current) * speed;
    }

    public static void update(int tick, int preyCount, int predatorCount, double grassCoverage) {
        if (preyCount <= 0 && predatorCount <= 0) return;

        double predatorToPreyRatio = (predatorCount + 1.0) / (preyCount + 1.0);
        double preyToPredatorRatio = (preyCount + 1.0) / (predatorCount + 1.0);

        // === Emergency situations ===
        if (preyCount < MIN_PREY) {
            preyReproductionCooldown = (int) lerp(preyReproductionCooldown, 10, FAST_LERP);
            preyEnergyGainGrass = (int) lerp(preyEnergyGainGrass, 20, FAST_LERP);
            predatorCaptureProbability = Math.max(0.3, predatorCaptureProbability * 0.9);
            if (grassCoverage < CRITICAL_GRASS) grassRegrowthTime = (int) lerp(grassRegrowthTime, 8, FAST_LERP);
        }

        if (predatorCount < MIN_PREDATOR) {
            predatorReproductionCooldown = (int) lerp(predatorReproductionCooldown, 20, FAST_LERP);
            predatorEnergyGainPrey = (int) lerp(predatorEnergyGainPrey, 15, FAST_LERP);
            predatorCaptureProbability = Math.min(0.8, predatorCaptureProbability * 1.1);
        }

        // === Prey reproduction dynamics ===
        if (predatorToPreyRatio > 0.5) preyReproductionCooldown = (int) lerp(preyReproductionCooldown, 12, MEDIUM_LERP);
        else if (predatorToPreyRatio < 0.15) preyReproductionCooldown = (int) lerp(preyReproductionCooldown, 18, SLOW_LERP);
        else preyReproductionCooldown = (int) lerp(preyReproductionCooldown, Config.PREY_REPRODUCTION_COOLDOWN, SLOW_LERP);

        preyReproductionCooldown = Math.max(10, Math.min(25, preyReproductionCooldown));

        // === Predator efficiency adaptation ===
        double preyPerPredator = (double) preyCount / Math.max(1, predatorCount);
        if (preyPerPredator < 2.0) predatorCaptureProbability = lerp(predatorCaptureProbability, 0.3, MEDIUM_LERP);
        else if (preyPerPredator > 6.0) predatorCaptureProbability = lerp(predatorCaptureProbability, 0.7, MEDIUM_LERP);
        else predatorCaptureProbability = lerp(predatorCaptureProbability, Config.PREDATOR_CAPTURE_PROBABILITY, SLOW_LERP);

        predatorCaptureProbability = Math.max(0.25, Math.min(0.8, predatorCaptureProbability));

        // === Predator reproduction adaptation ===
        if (preyToPredatorRatio < 3.0) predatorReproductionCooldown = (int) lerp(predatorReproductionCooldown, 60, MEDIUM_LERP);
        else if (preyToPredatorRatio > 8.0) predatorReproductionCooldown = (int) lerp(predatorReproductionCooldown, 20, MEDIUM_LERP);
        else predatorReproductionCooldown = (int) lerp(predatorReproductionCooldown, Config.PREDATOR_REPRODUCTION_COOLDOWN, SLOW_LERP);

        predatorReproductionCooldown = Math.max(15, Math.min(60, predatorReproductionCooldown));

        // === Grass regrowth adaptation ===
        if (grassCoverage < 0.30 && preyCount > 40) grassRegrowthTime = (int) lerp(grassRegrowthTime, 10, FAST_LERP);
        else if (grassCoverage < CRITICAL_GRASS) grassRegrowthTime = (int) lerp(grassRegrowthTime, 12, MEDIUM_LERP);
        else if (grassCoverage > HIGH_GRASS) grassRegrowthTime = (int) lerp(grassRegrowthTime, 25, SLOW_LERP);
        else grassRegrowthTime = (int) lerp(grassRegrowthTime, Config.GRASS_REGROWTH_TIME, SLOW_LERP);

        grassRegrowthTime = Math.max(8, Math.min(40, grassRegrowthTime));

        // === Evolutionary drift ===
        if (tick % 1000 == 0 && tick > 1000) predatorCaptureProbability = Math.min(0.75, predatorCaptureProbability * 1.01);
        if (tick % 1500 == 0 && tick > 1500) preyEnergyGainGrass = Math.min(20, preyEnergyGainGrass + 1);

        // === Optional logging ===
        if (tick % 100 == 0) {
            System.out.println(String.format(
                    "[DynamicConfig] Tick=%d | P/P Ratio=%.2f | Pred Capture=%.2f | Prey Cooldown=%d | Pred Cooldown=%d | Grass Regrow=%d",
                    tick,
                    (double) predatorCount / Math.max(1, preyCount),
                    predatorCaptureProbability,
                    preyReproductionCooldown,
                    predatorReproductionCooldown,
                    grassRegrowthTime
            ));
        }
    }

    // Reset dynamic values to defaults
    public static void reset() {
        predatorCaptureProbability = Config.PREDATOR_CAPTURE_PROBABILITY;
        preyReproductionCooldown = Config.PREY_REPRODUCTION_COOLDOWN;
        predatorReproductionCooldown = Config.PREDATOR_REPRODUCTION_COOLDOWN;
        grassRegrowthTime = Config.GRASS_REGROWTH_TIME;
        preyEnergyGainGrass = Config.PREY_ENERGY_GAIN_GRASS;
        predatorEnergyGainPrey = Config.PREDATOR_ENERGY_GAIN_PREY;
    }
}
