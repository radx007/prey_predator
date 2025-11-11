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
 * Reactive cyclic behavior for predator agents
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
        // Wait for environment tick
        if (!environment.isTickReady()) {
            block(10);
            return;
        }

        // Check if dead before action
        if (agent.isDead()) {
            System.out.println("[" + agent.getLocalName() + "] DIED - Energy: " +
                    agent.getEnergy() + ", Starvation ticks: " + agent.getTicksWithoutFood());
            environment.removePredatorAgent(agent.getLocalName());
            agent.doDelete();
            return;
        }

        // Perceive environment
        Position currentPos = agent.getPosition();
        if (currentPos == null) {
            System.err.println("[" + agent.getLocalName() + "] ERROR: null position!");
            environment.signalPredatorActionComplete();
            return;
        }

        List<PreyAgent> nearbyPrey = perceivePrey(currentPos);

        // Decision making
        boolean ateFood = false;
        if (!nearbyPrey.isEmpty()) {
            // Hunt prey
            ateFood = hunt(currentPos, nearbyPrey);
        } else {
            // Random walk
            randomWalk(currentPos);
            System.out.println("[" + agent.getLocalName() + "] Random walk to " + agent.getPosition());
        }

        // Try to reproduce
        if (agent.canReproduce()) {
            tryReproduce();
        }

        // Energy consumption - ONLY if didn't eat
        agent.consumeEnergy(Config.PREDATOR_MOVE_COST);

        // FIXED: Only increment starvation if didn't eat this tick
        if (!ateFood) {
            agent.setTicksWithoutFood(agent.getTicksWithoutFood() + 1);
        }

        // Update cooldowns
        if (agent.getReproductionCooldown() > 0) {
            agent.setReproductionCooldown(agent.getReproductionCooldown() - 1);
        }

        // Signal action complete
        environment.signalPredatorActionComplete();
    }

    private List<PreyAgent> perceivePrey(Position pos) {
        List<PreyAgent> nearby = new ArrayList<>();
        for (PreyAgent prey : environment.getPreyAgents()) {
            if (prey.getPosition().manhattanDistance(pos) <= Config.PREDATOR_VISION_RANGE) {
                nearby.add(prey);
            }
        }
        return nearby;
    }

    private boolean hunt(Position currentPos, List<PreyAgent> preyList) {
        // Find closest prey
        PreyAgent target = null;
        int minDist = Integer.MAX_VALUE;

        for (PreyAgent prey : preyList) {
            int dist = prey.getPosition().manhattanDistance(currentPos);
            if (dist < minDist) {
                minDist = dist;
                target = prey;
            }
        }

        if (target != null) {
            Position targetPos = target.getPosition();

            // If on same position, attempt capture
            if (currentPos.equals(targetPos)) {
                if (random.nextDouble() < Config.PREDATOR_ATTACK_SUCCESS_RATE) {
                    // Successful capture
                    System.out.println("[" + agent.getLocalName() + "] CAUGHT PREY " + target.getLocalName() +
                            " at " + currentPos + " (gained " + Config.PREDATOR_HUNT_GAIN + " energy)");
                    agent.gainEnergy(Config.PREDATOR_HUNT_GAIN);
                    target.setEnergy(0); // Kill prey
                    return true;  // Ate food
                } else {
                    System.out.println("[" + agent.getLocalName() + "] MISSED catching " + target.getLocalName() + " at " + currentPos);
                }
            } else {
                // Move towards prey
                System.out.println("[" + agent.getLocalName() + "] Chasing " + target.getLocalName() +
                        " from " + currentPos + " to " + targetPos);
                moveTowards(currentPos, targetPos);
            }
        }
        return false;  // Didn't eat
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

        // Find nearby predators of opposite gender
        for (PredatorAgent other : environment.getPredatorAgents()) {
            if (other.getLocalName().equals(agent.getLocalName())) continue;
            if (other.getGender() == agent.getGender()) continue;
            if (!other.canReproduce()) continue;
            if (other.getPosition().manhattanDistance(pos) > 1) continue;

            // Reproduce
            Position offspringPos = findEmptyNeighbor(pos);
            if (offspringPos != null) {
                Gender babyGender = Gender.random();
                System.out.println("[" + agent.getLocalName() + "] REPRODUCED with " + other.getLocalName() +
                        " at " + pos + " -> new " + babyGender + " predator at " + offspringPos);
                environment.createPredatorAgent(offspringPos, babyGender);

                // Energy cost
                agent.setEnergy(agent.getEnergy() * 2 / 3);
                agent.setReproductionCooldown(Config.PREDATOR_REPRODUCTION_COOLDOWN);

                other.setEnergy(other.getEnergy() * 2 / 3);
                other.setReproductionCooldown(Config.PREDATOR_REPRODUCTION_COOLDOWN);
                break;
            }
        }
    }

    private Position findEmptyNeighbor(Position pos) {
        List<Position> valid = getValidNeighbors(pos);
        return valid.isEmpty() ? null : valid.get(random.nextInt(valid.size()));
    }
}