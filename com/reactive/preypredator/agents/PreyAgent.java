package com.reactive.preypredator.agents;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.config.DynamicConfig;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.model.Position;

/**
 * Reactive Prey Agent with Dynamic Behavior Adaptation
 */
public class PreyAgent extends ReactiveAgent {
    private static int counter = 0;

    public PreyAgent(Position position, int energy) {
        super("Prey" + (counter++), position, energy);
    }

    @Override
    public void react(ReactiveEnvironment env) {
        if (!alive) return;

        // Check death conditions
        if (energy <= 0 || ticksWithoutFood > Config.PREY_STARVATION_LIMIT) {
            die();
            env.notifyDeath(this);
            return;
        }

        // Consume energy for living
        consumeEnergy(Config.PREY_MOVE_COST);
        decrementCooldown();

        // REACTIVE PERCEPTION
        Position nearestPredator = env.findNearestPredator(position, Config.PREY_VISION_RANGE);
        Position nearestGrass = env.findNearestGrass(position, Config.PREY_VISION_RANGE);

        // REACTIVE DECISION
        if (nearestPredator != null && position.distanceTo(nearestPredator) <= 4) {
            // FLEE FROM PREDATOR
            position = env.moveAwayFrom(position, nearestPredator, id);
        } else if (nearestGrass != null) {
            // SEEK GRASS
            position = env.moveTowards(position, nearestGrass, id);
        } else {
            // RANDOM WALK
            position = env.getRandomAdjacentPosition(position, id);
        }

        // Try to eat grass (✨ DYNAMIC grass gain)
        if (env.eatGrass(position)) {
            gainEnergy(DynamicConfig.preyGrassGain, Config.PREY_ENERGY_MAX);
            ticksWithoutFood = 0;
        } else {
            ticksWithoutFood++;
        }

        // Try to reproduce (✨ DYNAMIC reproduction cooldown)
        if (canReproduce(Config.PREY_MIN_REPRODUCTION_ENERGY)) {
            PreyAgent mate = env.findPreyMate(this, Config.PREY_VISION_RANGE);
            if (mate != null) {
                env.reproducePreyAgents(this, mate);
                setEnergy((int) (energy / Config.PREY_REPRODUCTION_ENERGY_COST));
                setReproductionCooldown(DynamicConfig.preyReproductionCooldown); // ✨ DYNAMIC
            }
        }

        // Update environment
        env.updateAgentPosition(id, position);
    }
}