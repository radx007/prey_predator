package com.reactive.preypredator.agents;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.model.Cell;
import com.reactive.preypredator.model.Gender;
import com.reactive.preypredator.model.Position;
import jade.core.behaviours.CyclicBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ADAPTED: Low energy loss + slow reproduction
 */
public class PreyBehavior extends CyclicBehaviour {
    private final PreyAgent agent;
    private final ReactiveEnvironment environment;
    private final Random random;

    public PreyBehavior(PreyAgent agent, ReactiveEnvironment environment) {
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

        // 1. Try to eat grass
        Position currentPos = agent.getPosition();
        Cell currentCell = environment.getGrid().getCell(currentPos.x, currentPos.y);

        if (currentCell != null && currentCell.hasGrass()) {
            currentCell.eatGrass();
            agent.setEnergy(agent.getEnergy() + Config.PREY_ENERGY_FROM_GRASS);
            agent.resetTicksWithoutFood();
        } else {
            agent.incrementTicksWithoutFood();
        }

        // 2. Check for nearby predators and flee
        List<Position> predatorPositions = environment.getNearbyPredatorPositions(
                currentPos, Config.PREY_VISION_RANGE);

        Position nextPos;
        if (!predatorPositions.isEmpty()) {
            nextPos = fleeFromPredators(currentPos, predatorPositions);
        } else {
            nextPos = seekGrass(currentPos);
        }

        // 3. Move
        if (nextPos != null && !nextPos.equals(currentPos)) {
            environment.moveAgent(agent.getLocalName(), nextPos);
            agent.setPosition(nextPos);
        }

        // LOW energy cost (adapted from friend's config)
        agent.consumeEnergy(Config.PREY_ENERGY_MOVE_COST);

        // 4. Cooldown
        agent.decrementReproductionCooldown();

        // 5. Death check
        if (agent.getEnergy() <= Config.PREY_STARVATION_THRESHOLD) {
            agent.setAlive(false);
            environment.removeDeadAgent(agent.getLocalName());
            environment.signalAgentCompletion();
            return;
        }

        // 6. Reproduction (long cooldown prevents explosion)
        if (agent.getReproductionCooldown() == 0 &&
                agent.getEnergy() >= Config.PREY_REPRODUCTION_THRESHOLD) {
            attemptReproduction();
        }

        environment.signalAgentCompletion();
    }

    private Position fleeFromPredators(Position current, List<Position> predators) {
        Position nearest = findNearest(current, predators);
        if (nearest == null) return current;

        int dx = current.x - nearest.x;
        int dy = current.y - nearest.y;

        double magnitude = Math.sqrt(dx * dx + dy * dy);
        if (magnitude > 0) {
            dx = (int) Math.round(dx / magnitude);
            dy = (int) Math.round(dy / magnitude);
        }

        List<Position> candidates = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int newX = current.x + i;
                int newY = current.y + j;

                if (environment.isPositionAvailable(newX, newY)) {
                    Position candidate = new Position(newX, newY);
                    if (i == dx || j == dy) {
                        candidates.add(0, candidate);
                    } else {
                        candidates.add(candidate);
                    }
                }
            }
        }

        return candidates.isEmpty() ? current : candidates.get(0);
    }

    private Position seekGrass(Position current) {
        List<Position> grassPositions = new ArrayList<>();

        for (int dx = -Config.PREY_VISION_RANGE; dx <= Config.PREY_VISION_RANGE; dx++) {
            for (int dy = -Config.PREY_VISION_RANGE; dy <= Config.PREY_VISION_RANGE; dy++) {
                int x = current.x + dx;
                int y = current.y + dy;

                Cell cell = environment.getGrid().getCell(x, y);
                if (cell != null && cell.hasGrass()) {
                    grassPositions.add(new Position(x, y));
                }
            }
        }

        if (!grassPositions.isEmpty()) {
            Position target = findNearest(current, grassPositions);
            return moveToward(current, target);
        }

        return randomWalk(current);
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
        List<PreyAgent> nearbyPrey = environment.getNearbyPreyAgents(
                agent.getPosition(), 2);

        for (PreyAgent mate : nearbyPrey) {
            if (mate.getReproductionCooldown() == 0 &&
                    mate.getEnergy() >= Config.PREY_REPRODUCTION_THRESHOLD) {

                environment.createPreyOffspring(agent.getPosition());

                agent.setReproductionCooldown(Config.PREY_REPRODUCTION_COOLDOWN);
                mate.setReproductionCooldown(Config.PREY_REPRODUCTION_COOLDOWN);

                // Use config cost
                agent.setEnergy(agent.getEnergy() - Config.PREY_REPRODUCTION_COST);
                mate.setEnergy(mate.getEnergy() - Config.PREY_REPRODUCTION_COST);
                break;
            }
        }
    }
}
