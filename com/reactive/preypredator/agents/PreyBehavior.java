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
 * Reactive cyclic behavior for prey agents
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
        // Wait for environment tick
        if (!environment.isTickReady()) {
            block(10);
            return;
        }

        // Check if dead before action
        if (agent.isDead()) {
            System.out.println("[" + agent.getLocalName() + "] Dying - Energy: " +
                    agent.getEnergy() + ", Starvation: " + agent.getTicksWithoutFood());
            environment.removePreyAgent(agent.getLocalName());
            agent.doDelete();
            return;
        }

        // Perceive environment
        Position currentPos = agent.getPosition();
        if (currentPos == null) {
            System.err.println("[" + agent.getLocalName() + "] ERROR: null position!");
            environment.signalPreyActionComplete();
            return;
        }

        List<PredatorAgent> nearbyPredators = perceivePredators(currentPos);

        // Decision making
        if (!nearbyPredators.isEmpty()) {
            // Flee from predators
            flee(currentPos, nearbyPredators);
        } else {
            // Try to eat grass
            Cell currentCell = environment.getGrid().getCell(currentPos);
            if (currentCell != null && currentCell.hasGrass()) {
                currentCell.eatGrass();
                agent.gainEnergy(Config.PREY_ENERGY_GAIN_GRASS);
            } else {
                // Move towards grass or random walk
                moveTowardsGrass(currentPos);
            }
        }

        // Try to reproduce
        if (agent.canReproduce()) {
            tryReproduce();
        }

        // Energy consumption
        agent.consumeEnergy(Config.PREY_ENERGY_LOSS);
        agent.setTicksWithoutFood(agent.getTicksWithoutFood() + 1);

        // Update cooldowns
        if (agent.getReproductionCooldown() > 0) {
            agent.setReproductionCooldown(agent.getReproductionCooldown() - 1);
        }

        // Signal action complete
        environment.signalPreyActionComplete();
    }

    private List<PredatorAgent> perceivePredators(Position pos) {
        List<PredatorAgent> nearby = new ArrayList<>();
        for (PredatorAgent predator : environment.getPredatorAgents()) {
            if (predator.getPosition().manhattanDistance(pos) <= Config.PREY_VISION_RANGE) {
                nearby.add(predator);
            }
        }
        return nearby;
    }

    private void flee(Position currentPos, List<PredatorAgent> predators) {
        // Calculate average predator position
        double avgX = 0, avgY = 0;
        for (PredatorAgent p : predators) {
            avgX += p.getPosition().x;
            avgY += p.getPosition().y;
        }
        avgX /= predators.size();
        avgY /= predators.size();

        // Move away from predators
        int dx = currentPos.x > avgX ? 1 : -1;
        int dy = currentPos.y > avgY ? 1 : -1;

        Position newPos = new Position(currentPos.x + dx, currentPos.y + dy);
        if (environment.getGrid().isValidPosition(newPos) &&
                environment.getGrid().isWalkable(newPos)) {
            moveTo(newPos);
        } else {
            randomWalk(currentPos);
        }
    }

    private void moveTowardsGrass(Position currentPos) {
        List<Position> grassPositions = findNearbyGrass(currentPos, 5);

        if (!grassPositions.isEmpty()) {
            Position target = grassPositions.get(random.nextInt(grassPositions.size()));
            moveTowards(currentPos, target);
        } else {
            randomWalk(currentPos);
        }
    }

    private List<Position> findNearbyGrass(Position center, int range) {
        List<Position> grassPos = new ArrayList<>();
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                Position pos = new Position(center.x + dx, center.y + dy);
                Cell cell = environment.getGrid().getCell(pos);
                if (cell != null && cell.hasGrass()) {
                    grassPos.add(pos);
                }
            }
        }
        return grassPos;
    }

    private void moveTowards(Position from, Position to) {
        int dx = Integer.compare(to.x, from.x);
        int dy = Integer.compare(to.y, from.y);

        Position newPos = new Position(from.x + dx, from.y + dy);
        if (environment.getGrid().isValidPosition(newPos) &&
                environment.getGrid().isWalkable(newPos)) {
            moveTo(newPos);
        } else {
            randomWalk(from);
        }
    }

    private void randomWalk(Position currentPos) {
        List<Position> validMoves = getValidNeighbors(currentPos);
        if (!validMoves.isEmpty()) {
            Position newPos = validMoves.get(random.nextInt(validMoves.size()));
            moveTo(newPos);
        }
    }

    private List<Position> getValidNeighbors(Position pos) {
        List<Position> neighbors = new ArrayList<>();
        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}, {1,1}, {-1,1}, {1,-1}, {-1,-1}};

        for (int[] dir : directions) {
            Position newPos = new Position(pos.x + dir[0], pos.y + dir[1]);
            if (environment.getGrid().isValidPosition(newPos) &&
                    environment.getGrid().isWalkable(newPos)) {
                neighbors.add(newPos);
            }
        }
        return neighbors;
    }

    private void moveTo(Position newPos) {
        environment.getGrid().setAgentPosition(agent.getLocalName(), newPos);
        agent.setPosition(newPos);
    }

    private void tryReproduce() {
        Position pos = agent.getPosition();

        // Find nearby prey of opposite gender
        for (PreyAgent other : environment.getPreyAgents()) {
            if (other.getLocalName().equals(agent.getLocalName())) continue;
            if (other.getGender() == agent.getGender()) continue;
            if (!other.canReproduce()) continue;
            if (other.getPosition().manhattanDistance(pos) > 1) continue;

            // Reproduce
            Position offspringPos = findEmptyNeighbor(pos);
            if (offspringPos != null) {
                environment.createPreyAgent(offspringPos, Gender.random());

                // Energy cost
                agent.setEnergy(agent.getEnergy() * 2 / 3);
                agent.setReproductionCooldown(Config.PREY_REPRODUCTION_COOLDOWN);

                other.setEnergy(other.getEnergy() * 2 / 3);
                other.setReproductionCooldown(Config.PREY_REPRODUCTION_COOLDOWN);
                break;
            }
        }
    }

    private Position findEmptyNeighbor(Position pos) {
        List<Position> valid = getValidNeighbors(pos);
        return valid.isEmpty() ? null : valid.get(random.nextInt(valid.size()));
    }
}