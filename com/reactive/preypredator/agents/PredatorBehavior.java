package com.reactive.preypredator.agents;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.model.Gender;
import com.reactive.preypredator.model.Position;
import jade.core.behaviours.CyclicBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fully reactive cyclic behavior for predator agents
 * No ACL messages - reacts to environment state changes
 */
public class PredatorBehavior extends CyclicBehaviour {
    private final PredatorAgent agent;
    private final ReactiveEnvironment environment;
    private final Random random;

    public PredatorBehavior(PredatorAgent agent, ReactiveEnvironment environment) {
        super(agent);
        this.agent = agent;
        this.environment = environment;
        this.random = new Random();
    }

    @Override
    public void action() {
        // Reactive waiting: block until environment activates tick
        synchronized (environment.getTickLock()) {
            while (!environment.isTickActive()) {
                try {
                    environment.getTickLock().wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        // If dead, do nothing
        if (!agent.isAlive()) {
            return;
        }

        Position currentPos = agent.getPosition();

        // 1. Try to hunt nearby prey
        List<PreyAgent> nearbyPrey = environment.getNearbyPreyAgents(
                currentPos, 1
        );

        if (!nearbyPrey.isEmpty()) {
            // EAT prey
            PreyAgent prey = nearbyPrey.get(0);
            prey.setAlive(false);
            environment.removeDeadAgent(prey.getLocalName());

            agent.setEnergy(agent.getEnergy() + Config.PREDATOR_ENERGY_FROM_PREY);
            agent.resetTicksWithoutFood();
        } else {
            agent.incrementTicksWithoutFood();
        }

        // 2. Seek prey within vision range
        List<Position> preyPositions = environment.getNearbyPreyPositions(
                currentPos, Config.PREDATOR_VISION_RANGE
        );

        Position nextPos;
        if (!preyPositions.isEmpty()) {
            // CHASE nearest prey
            Position target = findNearest(currentPos, preyPositions);
            nextPos = moveToward(currentPos, target);
        } else {
            // WANDER randomly
            nextPos = randomWalk(currentPos);
        }

        // 3. Move to next position
        if (nextPos != null && !nextPos.equals(currentPos)) {
            environment.moveAgent(agent.getLocalName(), nextPos);
            agent.setPosition(nextPos);
            agent.consumeEnergy(Config.PREDATOR_ENERGY_MOVE_COST);
        }

        // 4. Attempt reproduction
        if (agent.getReproductionCooldown() == 0 &&
                agent.getEnergy() >= Config.PREDATOR_REPRODUCTION_THRESHOLD) {
            attemptReproduction();
        } else {
            agent.decrementReproductionCooldown();
        }

        // 5. Check starvation/death
        if (agent.getEnergy() <= Config.PREDATOR_STARVATION_THRESHOLD) {
            agent.setAlive(false);
            environment.removeDeadAgent(agent.getLocalName());
        }

        // Signal completion
        environment.signalAgentCompletion();
    }

    /**
     * Move one step toward target
     */
    private Position moveToward(Position current, Position target) {
        int dx = Integer.compare(target.x, current.x);
        int dy = Integer.compare(target.y, current.y);

        Position preferred = new Position(current.x + dx, current.y + dy);
        if (environment.isPositionAvailable(preferred.x, preferred.y)) {
            return preferred;
        }

        // Try alternatives
        List<Position> alternatives = getNeighbors(current);
        if (!alternatives.isEmpty()) {
            return alternatives.get(random.nextInt(alternatives.size()));
        }

        return current;
    }

    /**
     * Random walk
     */
    private Position randomWalk(Position current) {
        List<Position> neighbors = getNeighbors(current);
        return neighbors.isEmpty() ? current : neighbors.get(random.nextInt(neighbors.size()));
    }

    /**
     * Get available neighboring positions
     */
    private List<Position> getNeighbors(Position pos) {
        List<Position> neighbors = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int newX = pos.x + dx;
                int newY = pos.y + dy;
                if (environment.isPositionAvailable(newX, newY)) {
                    neighbors.add(new Position(newX, newY));
                }
            }
        }
        return neighbors;
    }

    /**
     * Find nearest position from list
     */
    private Position findNearest(Position current, List<Position> positions) {
        Position nearest = null;
        double minDist = Double.MAX_VALUE;

        for (Position pos : positions) {
            double dist = current.distanceTo(pos);
            if (dist < minDist) {
                minDist = dist;
                nearest = pos;
            }
        }

        return nearest;
    }

    /**
     * Attempt reproduction with nearby mate
     */
    private void attemptReproduction() {
        List<PredatorAgent> nearbyPredators = environment.getNearbyPredatorAgents(
                agent.getPosition(), 2
        );

        for (PredatorAgent mate : nearbyPredators) {
            if (
//                    mate.getGender() != agent.getGender() &&
                    mate.getReproductionCooldown() == 0 &&
                    mate.getEnergy() >= Config.PREDATOR_REPRODUCTION_THRESHOLD) {

                // Create offspring
                environment.createPredatorOffspring(agent.getPosition());

                // Set cooldowns and reduce energy
                agent.setReproductionCooldown(Config.PREDATOR_REPRODUCTION_COOLDOWN);
                mate.setReproductionCooldown(Config.PREDATOR_REPRODUCTION_COOLDOWN);
                agent.setEnergy(agent.getEnergy() - 30);
                mate.setEnergy(mate.getEnergy() - 30);
                break;
            }
        }
    }
}
