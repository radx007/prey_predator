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
 * ADAPTED: Low starvation + eating cooldown + very slow reproduction
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

        if (!agent.isAlive()) {
            environment.signalAgentCompletion();
            return;
        }

        Position currentPos = agent.getPosition();
        boolean ateFood = false;

        // 1. Try to hunt adjacent prey (only if eating cooldown = 0)
        if (agent.getEatingCooldown() == 0) {
            List<PreyAgent> adjacentPrey = environment.getNearbyPreyAgents(currentPos, 1);

            if (!adjacentPrey.isEmpty()) {
                PreyAgent prey = adjacentPrey.get(0);
                prey.setAlive(false);
                environment.removeDeadAgent(prey.getLocalName());

                agent.setEnergy(agent.getEnergy() + Config.PREDATOR_ENERGY_FROM_PREY);
                agent.resetTicksWithoutFood();
                agent.setTicksSinceLastMeal(0);
                agent.setEatingCooldown(Config.PREDATOR_EATING_COOLDOWN); // NEW: cooldown
                ateFood = true;
            }
        }

        if (!ateFood) {
            agent.incrementTicksWithoutFood();

            if (agent.getTicksSinceLastMeal() < Integer.MAX_VALUE) {
                agent.setTicksSinceLastMeal(agent.getTicksSinceLastMeal() + 1);
            }
        }

        // 2. Movement: Chase prey or wander
        List<Position> preyPositions = environment.getNearbyPreyPositions(
                currentPos, Config.PREDATOR_VISION_RANGE);

        Position nextPos;
        if (!preyPositions.isEmpty()) {
            Position target = findNearest(currentPos, preyPositions);
            nextPos = moveToward(currentPos, target);
        } else {
            nextPos = randomWalk(currentPos);
        }

        if (nextPos != null && !nextPos.equals(currentPos)) {
            environment.moveAgent(agent.getLocalName(), nextPos);
            agent.setPosition(nextPos);
        }

        // 3. LOW energy cost (adapted from friend's config)
        agent.consumeEnergy(Config.PREDATOR_ENERGY_MOVE_COST);

        // 4. Cooldown decrements
        agent.decrementReproductionCooldown();
        agent.decrementEatingCooldown(); // NEW

        // 5. DEATH CONDITIONS (more lenient now)
        boolean starvedByEnergy = agent.getEnergy() <= Config.PREDATOR_STARVATION_THRESHOLD;
        boolean starvedByTime = agent.getTicksWithoutFood() >= Config.PREDATOR_MAX_TICKS_WITHOUT_FOOD;

        if (starvedByEnergy || starvedByTime) {
            agent.setAlive(false);
            environment.removeDeadAgent(agent.getLocalName());
            environment.signalAgentCompletion();
            return;
        }

        // 6. REPRODUCTION: Very long cooldown prevents explosions
        if (agent.getReproductionCooldown() == 0 &&
                agent.getEnergy() >= Config.PREDATOR_REPRODUCTION_THRESHOLD) {
            attemptReproduction();
        }

        environment.signalAgentCompletion();
    }

    private Position moveToward(Position current, Position target) {
        int dx = Integer.compare(target.x, current.x);
        int dy = Integer.compare(target.y, current.y);

        Position preferred = new Position(current.x + dx, current.y + dy);
        if (environment.isPositionAvailable(preferred.x, preferred.y)) {
            return preferred;
        }

        List<Position> alternatives = getNeighbors(current);
        if (!alternatives.isEmpty()) {
            return alternatives.get(random.nextInt(alternatives.size()));
        }

        return current;
    }

    private Position randomWalk(Position current) {
        List<Position> neighbors = getNeighbors(current);
        return neighbors.isEmpty() ? current : neighbors.get(random.nextInt(neighbors.size()));
    }

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

    private void attemptReproduction() {
        List<PredatorAgent> nearbyPredators = environment.getNearbyPredatorAgents(
                agent.getPosition(), 2);

        for (PredatorAgent mate : nearbyPredators) {
            if (mate.getReproductionCooldown() == 0 &&
                    mate.getEnergy() >= Config.PREDATOR_REPRODUCTION_THRESHOLD) {

                environment.createPredatorOffspring(agent.getPosition());

                agent.setReproductionCooldown(Config.PREDATOR_REPRODUCTION_COOLDOWN);
                mate.setReproductionCooldown(Config.PREDATOR_REPRODUCTION_COOLDOWN);

                // Use config cost
                agent.setEnergy(agent.getEnergy() - Config.PREDATOR_REPRODUCTION_COST);
                mate.setEnergy(mate.getEnergy() - Config.PREDATOR_REPRODUCTION_COST);
                break;
            }
        }
    }
}
