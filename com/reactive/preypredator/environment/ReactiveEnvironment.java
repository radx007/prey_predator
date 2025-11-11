package com.reactive.preypredator.environment;

import com.reactive.preypredator.agents.PreyAgent;
import com.reactive.preypredator.agents.PredatorAgent;
import com.reactive.preypredator.agents.ReactiveAgent;
import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.config.DynamicConfig;
import com.reactive.preypredator.model.Cell;
import com.reactive.preypredator.model.Grid;
import com.reactive.preypredator.model.Position;
import com.reactive.preypredator.statistics.DataLogger;
import com.reactive.preypredator.statistics.Statistics;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Reactive Environment - Central hub for all agent interactions
 */
public class ReactiveEnvironment {
    private Grid grid;
    private List<PreyAgent> preyAgents;
    private List<PredatorAgent> predatorAgents;
    private int currentTick;
    private DataLogger dataLogger;
    private Random random;

    public ReactiveEnvironment() {
        this.grid = new Grid(Config.GRID_WIDTH, Config.GRID_HEIGHT);
        this.preyAgents = new CopyOnWriteArrayList<>();
        this.predatorAgents = new CopyOnWriteArrayList<>();
        this.currentTick = 0;
        this.dataLogger = new DataLogger("simulation_stats.csv");
        this.random = new Random();

        initializePopulation();
    }

    private void initializePopulation() {
        for (int i = 0; i < Config.INITIAL_PREY_COUNT; i++) {
            Position pos = findRandomEmptyPosition();
            if (pos != null) {
                PreyAgent prey = new PreyAgent(pos, Config.PREY_ENERGY_START);
                preyAgents.add(prey);
                grid.setAgentPosition(prey.getId(), pos);
            }
        }

        for (int i = 0; i < Config.INITIAL_PREDATOR_COUNT; i++) {
            Position pos = findRandomEmptyPosition();
            if (pos != null) {
                PredatorAgent predator = new PredatorAgent(pos, Config.PREDATOR_ENERGY_START);
                predatorAgents.add(predator);
                grid.setAgentPosition(predator.getId(), pos);
            }
        }

        System.out.println("Environment initialized:");
        System.out.println("  - Prey: " + preyAgents.size());
        System.out.println("  - Predators: " + predatorAgents.size());
    }

    public void tick() {
        currentTick++;
        grid.updateGrassRegrowth();

        DynamicConfig.update(
                currentTick,
                preyAgents.size(),
                predatorAgents.size(),
                grid.getGrassCoverage()
        );

        for (PreyAgent prey : new ArrayList<>(preyAgents)) {
            if (prey.isAlive()) {
                prey.react(this);
            }
        }

        for (PredatorAgent predator : new ArrayList<>(predatorAgents)) {
            if (predator.isAlive()) {
                predator.react(this);
            }
        }

        collectStatistics();

        if (currentTick % 20 == 0) {
            Statistics stats = dataLogger.getLatest();
            if (stats != null) {
                System.out.println(String.format("Tick %d: Prey=%d (E=%.1f) | Predators=%d (E=%.1f) | Grass=%.1f%%",
                        currentTick, stats.getPreyCount(), stats.getAvgPreyEnergy(),
                        stats.getPredatorCount(), stats.getAvgPredatorEnergy(), stats.getGrassCoverage() * 100));
            }
        }
    }

    private void collectStatistics() {
        int preyCount = preyAgents.size();
        int predatorCount = predatorAgents.size();

        double avgPreyEnergy = preyCount > 0
                ? preyAgents.stream().mapToInt(PreyAgent::getEnergy).average().orElse(0)
                : 0;

        double avgPredatorEnergy = predatorCount > 0
                ? predatorAgents.stream().mapToInt(PredatorAgent::getEnergy).average().orElse(0)
                : 0;

        double grassCoverage = grid.getGrassCoverage();

        Statistics stats = new Statistics(currentTick, preyCount, predatorCount,
                avgPreyEnergy, avgPredatorEnergy, grassCoverage);
        dataLogger.log(stats);
    }

    // REACTIVE ENVIRONMENT SERVICES

    public Position findNearestPredator(Position from, int range) {
        Position nearest = null;
        double minDist = Double.MAX_VALUE;

        for (PredatorAgent predator : predatorAgents) {
            if (!predator.isAlive()) continue;
            double dist = from.distanceTo(predator.getPosition());
            if (dist <= range && dist < minDist) {
                minDist = dist;
                nearest = predator.getPosition();
            }
        }

        return nearest;
    }

    public PreyAgent findNearestPrey(Position from, int range) {
        PreyAgent nearest = null;
        double minDist = Double.MAX_VALUE;

        for (PreyAgent prey : preyAgents) {
            if (!prey.isAlive()) continue;
            double dist = from.distanceTo(prey.getPosition());
            if (dist <= range && dist < minDist) {
                minDist = dist;
                nearest = prey;
            }
        }

        return nearest;
    }

    public Position findNearestGrass(Position from, int range) {
        Position nearest = null;
        double minDist = Double.MAX_VALUE;

        for (int x = Math.max(0, from.x - range); x < Math.min(grid.getWidth(), from.x + range + 1); x++) {
            for (int y = Math.max(0, from.y - range); y < Math.min(grid.getHeight(), from.y + range + 1); y++) {
                Position pos = new Position(x, y);
                Cell cell = grid.getCell(x, y);
                if (cell != null && cell.hasGrass()) {
                    double dist = from.distanceTo(pos);
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = pos;
                    }
                }
            }
        }

        return nearest;
    }

    public PreyAgent findPreyMate(PreyAgent agent, int range) {
        for (PreyAgent other : preyAgents) {
            if (!other.isAlive() || other.getId().equals(agent.getId())) continue;
            if (!other.getGender().equals(agent.getGender()) &&
                    agent.getPosition().distanceTo(other.getPosition()) <= range &&
                    other.canReproduce(Config.PREY_MIN_REPRODUCTION_ENERGY)) {
                return other;
            }
        }
        return null;
    }

    public PredatorAgent findPredatorMate(PredatorAgent agent, int range) {
        for (PredatorAgent other : predatorAgents) {
            if (!other.isAlive() || other.getId().equals(agent.getId())) continue;
            if (!other.getGender().equals(agent.getGender()) &&
                    agent.getPosition().distanceTo(other.getPosition()) <= range &&
                    other.canReproduce(Config.PREDATOR_MIN_REPRODUCTION_ENERGY)) {
                return other;
            }
        }
        return null;
    }

    public void reproducePreyAgents(PreyAgent parent1, PreyAgent parent2) {
        Position babyPos = getRandomAdjacentPosition(parent1.getPosition(), null);
        if (babyPos != null && !babyPos.equals(parent1.getPosition())) {
            PreyAgent baby = new PreyAgent(babyPos, Config.PREY_ENERGY_START);
            preyAgents.add(baby);
            grid.setAgentPosition(baby.getId(), babyPos);
        }
    }

    public void reproducePredatorAgents(PredatorAgent parent1, PredatorAgent parent2) {
        Position babyPos = getRandomAdjacentPosition(parent1.getPosition(), null);
        if (babyPos != null && !babyPos.equals(parent1.getPosition())) {
            PredatorAgent baby = new PredatorAgent(babyPos, Config.PREDATOR_ENERGY_START);
            predatorAgents.add(baby);
            grid.setAgentPosition(baby.getId(), babyPos);
        }
    }

    public boolean eatGrass(Position pos) {
        Cell cell = grid.getCell(pos.x, pos.y);
        if (cell != null && cell.hasGrass()) {
            cell.eatGrass();
            return true;
        }
        return false;
    }

    public void notifyDeath(ReactiveAgent agent) {
        if (agent instanceof PreyAgent) {
            preyAgents.remove(agent);
        } else if (agent instanceof PredatorAgent) {
            predatorAgents.remove(agent);
        }
        grid.removeAgent(agent.getId());
    }

    public void updateAgentPosition(String agentId, Position pos) {
        grid.setAgentPosition(agentId, pos);
    }

    public Position moveTowards(Position from, Position to, String agentId) {
        List<Position> adjacent = getAdjacentPositions(from, agentId);
        if (adjacent.isEmpty()) return from;

        Position best = from;
        double minDist = Double.MAX_VALUE;

        for (Position pos : adjacent) {
            double dist = pos.distanceTo(to);
            if (dist < minDist) {
                minDist = dist;
                best = pos;
            }
        }

        return best;
    }

    public Position moveAwayFrom(Position from, Position threat, String agentId) {
        List<Position> adjacent = getAdjacentPositions(from, agentId);
        if (adjacent.isEmpty()) return from;

        Position best = from;
        double maxDist = -1;

        for (Position pos : adjacent) {
            double dist = pos.distanceTo(threat);
            if (dist > maxDist) {
                maxDist = dist;
                best = pos;
            }
        }

        return best;
    }

    public Position getRandomAdjacentPosition(Position pos, String agentId) {
        List<Position> adjacent = getAdjacentPositions(pos, agentId);
        if (adjacent.isEmpty()) return pos;
        return adjacent.get(random.nextInt(adjacent.size()));
    }

    private List<Position> getAdjacentPositions(Position pos, String excludeAgent) {
        List<Position> adjacent = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        Set<String> occupiedPositions = new HashSet<>();
        for (Map.Entry<String, Position> entry : grid.getAgentPositions().entrySet()) {
            if (excludeAgent == null || !entry.getKey().equals(excludeAgent)) {
                Position p = entry.getValue();
                occupiedPositions.add(p.x + "," + p.y);
            }
        }

        for (int[] dir : directions) {
            Position newPos = new Position(pos.x + dir[0], pos.y + dir[1]);
            String key = newPos.x + "," + newPos.y;

            if (grid.isWalkable(newPos) && !occupiedPositions.contains(key)) {
                adjacent.add(newPos);
            }
        }

        return adjacent;
    }

    private Position findRandomEmptyPosition() {
        Set<String> occupiedPositions = new HashSet<>();
        for (Position pos : grid.getAgentPositions().values()) {
            occupiedPositions.add(pos.x + "," + pos.y);
        }

        for (int attempt = 0; attempt < 100; attempt++) {
            int x = random.nextInt(grid.getWidth());
            int y = random.nextInt(grid.getHeight());
            Position pos = new Position(x, y);
            String key = x + "," + y;

            if (grid.isWalkable(pos) && !occupiedPositions.contains(key)) {
                return pos;
            }
        }
        return null;
    }

    public boolean isRunning() {
        return currentTick < Config.MAX_TICKS && !preyAgents.isEmpty() && !predatorAgents.isEmpty();
    }

    public Grid getGrid() {
        return grid;
    }

    public List<PreyAgent> getPreyAgents() {
        return new ArrayList<>(preyAgents);
    }

    public List<PredatorAgent> getPredatorAgents() {
        return new ArrayList<>(predatorAgents);
    }

    public DataLogger getDataLogger() {
        return dataLogger;
    }

    public int getCurrentTick() {
        return currentTick;
    }
}