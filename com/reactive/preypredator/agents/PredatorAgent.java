package com.reactive.preypredator.agents;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.config.DynamicConfig;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.model.Position;

/**
 * Reactive Predator Agent with Dynamic Behavior Adaptation
 */
public class PredatorAgent extends ReactiveAgent {
    private static int counter = 0;

    public PredatorAgent(Position position, int energy) {
        super("Predator" + (counter++), position, energy);
    }

    @Override
    public void react(ReactiveEnvironment env) {
        if (!alive) return;

        // Check death conditions
        if (energy <= 0 || ticksWithoutFood > Config.PREDATOR_STARVATION_LIMIT) {
            die();
            env.notifyDeath(this);
            return;
        }

        // Consume energy for living (✨ DYNAMIC move cost)
        consumeEnergy(DynamicConfig.predatorMoveCost);
        decrementCooldown();

        // REACTIVE PERCEPTION
        PreyAgent nearestPrey = env.findNearestPrey(position, Config.PREDATOR_VISION_RANGE);

        // REACTIVE DECISION
        if (nearestPrey != null) {
            // CHASE PREY
            position = env.moveTowards(position, nearestPrey.getPosition(), id);

            // Try to attack if close enough (✨ DYNAMIC success rate)
            if (position.distanceTo(nearestPrey.getPosition()) <= 1.5) {
                if (Math.random() < DynamicConfig.predatorAttackSuccessRate) {
                    // SUCCESSFUL HUNT
                    nearestPrey.die();
                    env.notifyDeath(nearestPrey);
                    gainEnergy(Config.PREDATOR_HUNT_GAIN, Config.PREDATOR_ENERGY_MAX);
                    ticksWithoutFood = 0;
                    System.out.println(id + " successfully hunted " + nearestPrey.getId());
                }
            }
        } else {


            // RANDOM WALK.
            position = env.getRandomAdjacentPosition(position, id);
            ticksWithoutFood++;
        }

        // Try to reproduce (✨ DYNAMIC reproduction cooldown)
        if (canReproduce(Config.PREDATOR_MIN_REPRODUCTION_ENERGY)) {
            PredatorAgent mate = env.findPredatorMate(this, Config.PREDATOR_VISION_RANGE);
            if (mate != null) {
                env.reproducePredatorAgents(this, mate);
                setEnergy((int) (energy / Config.PREDATOR_REPRODUCTION_ENERGY_COST));
                setReproductionCooldown(DynamicConfig.predatorReproductionCooldown); // ✨ DYNAMIC
            }

        }

        // Update environment
        env.updateAgentPosition(id, position);
    }
}