package com.reactive.preypredator.config;

/**
 * Dynamic Configuration System for Lotka-Volterra Oscillations
 *
 * This class adapts simulation parameters in real-time based on:
 * - Population ratios (predator/prey balance)
 * - Grass coverage (resource availability)
 * - Simulation progression (evolutionary adaptation)
 *
 * Goal: Maintain stable oscillations while preventing extinction
 */
public class DynamicConfig {

    // ==================== CURRENT DYNAMIC VALUES ====================
    // These adjust automatically during simulation
    public static double predatorAttackSuccessRate = Config.PREDATOR_ATTACK_SUCCESS_RATE;
    public static int grassRegrowthTime = Config.GRASS_REGROWTH_TIME;
    public static int preyReproductionCooldown = Config.PREY_REPRODUCTION_COOLDOWN;
    public static int predatorReproductionCooldown = Config.PREDATOR_REPRODUCTION_COOLDOWN;
    public static int preyGrassGain = Config.PREY_GRASS_GAIN;
    public static double predatorMoveCost = Config.PREDATOR_MOVE_COST;

    // ==================== SMOOTHING PARAMETERS ====================
    private static final double FAST_LERP = 0.35;   // Quick adaptation
    private static final double MEDIUM_LERP = 0.12; // Moderate adaptation
    private static final double SLOW_LERP = 0.05;   // Gradual drift

    // ==================== CRITICAL THRESHOLDS ====================
    private static final int MIN_PREY_FOR_STABILITY = 10;
    private static final int MIN_PREDATOR_FOR_STABILITY = 5;
    private static final double CRITICAL_GRASS_THRESHOLD = 0.25;
    private static final double HIGH_GRASS_THRESHOLD = 0.75;

    /**
     * Linear interpolation for smooth transitions
     */
    private static double lerp(double current, double target, double speed) {
        return current + (target - current) * speed;
    }

    /**
     * Main update method - called every simulation tick
     *
     * @param tick Current simulation tick
     * @param preyCount Current prey population
     * @param predatorCount Current predator population
     * @param grassCoverage Current grass coverage (0.0 to 1.0)
     */
    public static void update(int tick, int preyCount, int predatorCount, double grassCoverage) {
        // Safety check
        if (preyCount <= 0 && predatorCount <= 0) return;

        // Calculate population dynamics
        double predatorToPreyRatio = (predatorCount + 1.0) / (preyCount + 1.0);
        double preyToPredatorRatio = (preyCount + 1.0) / (predatorCount + 1.0);

        // ========== EMERGENCY INTERVENTIONS ==========
        handleEmergencySituations(preyCount, predatorCount, grassCoverage);

        // ========== PREY REPRODUCTION DYNAMICS ==========
        adaptPreyReproduction(predatorToPreyRatio, preyCount);

        // ========== PREDATOR HUNTING EFFICIENCY ==========
        adaptPredatorEfficiency(preyCount, predatorCount);

        // ========== PREDATOR REPRODUCTION DYNAMICS ==========
        adaptPredatorReproduction(preyCount, preyToPredatorRatio);

        // ========== GRASS REGROWTH DYNAMICS ==========
        adaptGrassRegrowth(grassCoverage, preyCount);

        // ========== EVOLUTIONARY ADAPTATION ==========
        if (tick % 500 == 0) {
            applyEvolutionaryDrift(tick);
        }

        // ========== LOGGING (Optional - for debugging) ==========
        if (tick % 100 == 0) {
            logCurrentState(tick, preyCount, predatorCount, grassCoverage);
        }
    }

    /**
     * Handle critical situations to prevent total collapse
     */
    private static void handleEmergencySituations(int preyCount, int predatorCount, double grassCoverage) {
        // CRITICAL: Prey near extinction
        if (preyCount < MIN_PREY_FOR_STABILITY) {
            preyReproductionCooldown = (int) lerp(preyReproductionCooldown, 15, FAST_LERP);
            preyGrassGain = (int) lerp(preyGrassGain, 35, FAST_LERP);
            predatorAttackSuccessRate = Math.max(0.3, predatorAttackSuccessRate * 0.95);

            // Emergency grass boost
            if (grassCoverage < CRITICAL_GRASS_THRESHOLD) {
                grassRegrowthTime = (int) lerp(grassRegrowthTime, 8, FAST_LERP);
            }
        }

        // CRITICAL: Predators near extinction
        if (predatorCount < MIN_PREDATOR_FOR_STABILITY) {
            predatorReproductionCooldown = (int) lerp(predatorReproductionCooldown, 35, FAST_LERP);
            predatorMoveCost = lerp(predatorMoveCost, 2.0, FAST_LERP);
            predatorAttackSuccessRate = Math.min(0.85, predatorAttackSuccessRate * 1.05);
        }

        // Ecosystem collapse prevention
        if (grassCoverage < CRITICAL_GRASS_THRESHOLD && preyCount < 30) {
            grassRegrowthTime = (int) lerp(grassRegrowthTime, 10, FAST_LERP);
        }
    }

    /**
     * Adapt prey reproduction based on predator pressure
     */
    private static void adaptPreyReproduction(double predatorToPreyRatio, int preyCount) {
        // High predator pressure → prey reproduce FASTER (survival response)
        if (predatorToPreyRatio > 0.5) {
            int targetCooldown = 18; // Fast breeding under pressure
            preyReproductionCooldown = (int) lerp(preyReproductionCooldown, targetCooldown, MEDIUM_LERP);
        }
        // Low predator pressure → prey reproduce normally
        else if (predatorToPreyRatio < 0.15) {
            int targetCooldown = 30; // Slower when safe
            preyReproductionCooldown = (int) lerp(preyReproductionCooldown, targetCooldown, SLOW_LERP);
        }
        // Balanced
        else {
            preyReproductionCooldown = (int) lerp(preyReproductionCooldown, Config.PREY_REPRODUCTION_COOLDOWN, SLOW_LERP);
        }

        // Clamp to reasonable values
        preyReproductionCooldown = Math.max(15, Math.min(40, preyReproductionCooldown));
    }

    /**
     * Adapt predator hunting efficiency based on prey availability
     * (Functional Response - Type II)
     */
    private static void adaptPredatorEfficiency(int preyCount, int predatorCount) {
        // Calculate prey density per predator
        double preyPerPredator = (double) preyCount / Math.max(1, predatorCount);

        // Low prey availability → hunting becomes HARDER
        if (preyPerPredator < 2.0) {
            double targetRate = Config.PREDATOR_ATTACK_SUCCESS_RATE * 0.6;
            predatorAttackSuccessRate = lerp(predatorAttackSuccessRate, targetRate, MEDIUM_LERP);
        }
        // Abundant prey → hunting becomes EASIER
        else if (preyPerPredator > 6.0) {
            double targetRate = Math.min(0.80, Config.PREDATOR_ATTACK_SUCCESS_RATE * 1.15);
            predatorAttackSuccessRate = lerp(predatorAttackSuccessRate, targetRate, MEDIUM_LERP);
        }
        // Return to baseline
        else {
            predatorAttackSuccessRate = lerp(predatorAttackSuccessRate,
                    Config.PREDATOR_ATTACK_SUCCESS_RATE, SLOW_LERP);
        }

        // Clamp to reasonable bounds
        predatorAttackSuccessRate = Math.max(0.25, Math.min(0.85, predatorAttackSuccessRate));
    }

    /**
     * Adapt predator reproduction based on food availability
     */
    private static void adaptPredatorReproduction(int preyCount, double preyToPredatorRatio) {
        // Scarce prey → predators reproduce SLOWER (lag effect)
        if (preyToPredatorRatio < 3.0) {
            int targetCooldown = 85; // Long cooldown when food is scarce
            predatorReproductionCooldown = (int) lerp(predatorReproductionCooldown, targetCooldown, MEDIUM_LERP);
        }
        // Abundant prey → predators reproduce FASTER
        else if (preyToPredatorRatio > 8.0) {
            int targetCooldown = 45; // Quick reproduction when well-fed
            predatorReproductionCooldown = (int) lerp(predatorReproductionCooldown, targetCooldown, MEDIUM_LERP);
        }
        // Return to baseline
        else {
            predatorReproductionCooldown = (int) lerp(predatorReproductionCooldown,
                    Config.PREDATOR_REPRODUCTION_COOLDOWN, SLOW_LERP);
        }

        // Clamp to reasonable values
        predatorReproductionCooldown = Math.max(40, Math.min(100, predatorReproductionCooldown));
    }

    /**
     * Adapt grass regrowth based on coverage and prey population
     */
    private static void adaptGrassRegrowth(double grassCoverage, int preyCount) {
        // Low grass + many prey → FAST regrowth (prevent starvation)
        if (grassCoverage < 0.30 && preyCount > 40) {
            grassRegrowthTime = (int) lerp(grassRegrowthTime, 12, FAST_LERP);
        }
        // Very low grass → emergency regrowth
        else if (grassCoverage < CRITICAL_GRASS_THRESHOLD) {
            grassRegrowthTime = (int) lerp(grassRegrowthTime, 15, MEDIUM_LERP);
        }
        // High grass coverage → SLOW regrowth (let prey graze)
        else if (grassCoverage > HIGH_GRASS_THRESHOLD) {
            grassRegrowthTime = (int) lerp(grassRegrowthTime, 35, SLOW_LERP);
        }
        // Normal range
        else {
            grassRegrowthTime = (int) lerp(grassRegrowthTime, Config.GRASS_REGROWTH_TIME, SLOW_LERP);
        }

        // Clamp to reasonable values
        grassRegrowthTime = Math.max(8, Math.min(40, grassRegrowthTime));
    }

    /**
     * Apply subtle evolutionary changes over long time scales
     */
    private static void applyEvolutionaryDrift(int tick) {
        // Predators slowly improve hunting over time (learning)
        if (tick % 1000 == 0 && tick > 1000) {
            predatorAttackSuccessRate = Math.min(0.75, predatorAttackSuccessRate * 1.01);
        }

        // Prey slowly improve efficiency over time
        if (tick % 1500 == 0 && tick > 1500) {
            preyGrassGain = Math.min(35, preyGrassGain + 1);
        }
    }

    /**
     * Log current dynamic state for debugging
     */
    private static void logCurrentState(int tick, int preyCount, int predatorCount, double grassCoverage) {
        System.out.println(String.format(
                "[DynamicConfig] Tick=%d | P/P Ratio=%.2f | Pred Attack=%.2f | " +
                        "Prey Cooldown=%d | Pred Cooldown=%d | Grass Regrow=%d",
                tick,
                (double) predatorCount / Math.max(1, preyCount),
                predatorAttackSuccessRate,
                preyReproductionCooldown,
                predatorReproductionCooldown,
                grassRegrowthTime
        ));
    }

    /**
     * Reset to baseline configuration (for testing)
     */
    public static void reset() {
        predatorAttackSuccessRate = Config.PREDATOR_ATTACK_SUCCESS_RATE;
        grassRegrowthTime = Config.GRASS_REGROWTH_TIME;
        preyReproductionCooldown = Config.PREY_REPRODUCTION_COOLDOWN;
        predatorReproductionCooldown = Config.PREDATOR_REPRODUCTION_COOLDOWN;
        preyGrassGain = Config.PREY_GRASS_GAIN;
        predatorMoveCost = Config.PREDATOR_MOVE_COST;
    }
}