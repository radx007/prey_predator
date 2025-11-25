package com.reactive.preypredator.environment;

import com.reactive.preypredator.agents.PreyAgent;
import com.reactive.preypredator.agents.PredatorAgent;
import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.model.PlacementMode;
import com.reactive.preypredator.model.Gender;
import com.reactive.preypredator.model.Grid;
import com.reactive.preypredator.model.Position;
import com.reactive.preypredator.statistics.DataLogger;
import com.reactive.preypredator.statistics.Statistics;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ReactiveEnvironment {
    private Grid grid;
    private AgentContainer container;
    private Map<String, PreyAgent> preyAgents;
    private Map<String, PredatorAgent> predatorAgents;
    private DataLogger dataLogger;
    private int currentTick;
    private Random random;
    private volatile boolean running = true;

    private final Object tickLock = new Object();
    private boolean tickActive = false;
    private CountDownLatch agentLatch;
    private Set<String> deadAgents;


    public boolean isRunning() {
        return running;
    }
    public ReactiveEnvironment() {
        this.grid = new Grid(Config.GRID_WIDTH, Config.GRID_HEIGHT);
        this.preyAgents = new ConcurrentHashMap<>();
        this.predatorAgents = new ConcurrentHashMap<>();
        this.deadAgents = ConcurrentHashMap.newKeySet();
        this.dataLogger = new DataLogger(Config.CSV_OUTPUT_FILE);
        this.currentTick = 0;
        this.random = new Random();

        initializeJADE();
        spawnInitialAgents();
    }

    private void initializeJADE() {
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "false");
        container = rt.createMainContainer(profile);
    }

    private void spawnInitialAgents() {
        if (Config.PLACEMENT_MODE == PlacementMode.FIXED_PATTERN) {
            spawnAgentsFixedPattern();
        } else {
            spawnAgentsRandom();
        }

        System.out.println("[Environment] Spawned " + Config.INITIAL_PREY_COUNT + " prey and "
                + Config.INITIAL_PREDATOR_COUNT + " predators (Mode: " + Config.PLACEMENT_MODE + ")");
    }

    private void spawnAgentsRandom() {
        for (int i = 0; i < Config.INITIAL_PREY_COUNT; i++) {
            createPreyAgent("Prey_" + i);
        }

        for (int i = 0; i < Config.INITIAL_PREDATOR_COUNT; i++) {
            createPredatorAgent("Predator_" + i);
        }
    }

    /**
     * FIXED: Alternating genders for reproduction
     */
    private void spawnAgentsFixedPattern() {
        int centerX = Config.GRID_WIDTH / 2;
        int centerY = Config.GRID_HEIGHT / 2;

        int preyPlaced = 0;
        int predatorPlaced = 0;

        // Place prey in compact center cluster with ALTERNATING genders
        int preyRadiusX = Config.GRID_WIDTH / 6;
        int preyRadiusY = Config.GRID_HEIGHT / 6;

        for (int x = centerX - preyRadiusX; x <= centerX + preyRadiusX && preyPlaced < Config.INITIAL_PREY_COUNT; x++) {
            for (int y = centerY - preyRadiusY; y <= centerY + preyRadiusY && preyPlaced < Config.INITIAL_PREY_COUNT; y++) {
                if (!grid.isWithinBounds(x, y)) continue;
                if (!grid.getCell(x, y).isWalkable()) continue;

                Position pos = new Position(x, y);
                String name = "Prey_" + preyPlaced;

                // CRITICAL FIX: Alternate male/female for reproduction
                Gender gender = (preyPlaced % 2 == 0) ? Gender.MALE : Gender.FEMALE;

                try {
                    Object[] args = {this, pos, gender, name};
                    AgentController ac = container.createNewAgent(name,
                            "com.reactive.preypredator.agents.PreyAgent", args);
                    ac.start();
                    preyPlaced++;
                } catch (StaleProxyException e) {
                    System.err.println("Error creating fixed-position prey: " + e.getMessage());
                }
            }
        }

        // Place predators in ring with ALTERNATING genders
        int predatorRadius = preyRadiusX + 5;
        int angleStep = 360 / Config.INITIAL_PREDATOR_COUNT;

        for (int i = 0; i < Config.INITIAL_PREDATOR_COUNT; i++) {
            int angleDeg = i * angleStep;
            double angle = Math.toRadians(angleDeg);
            int x = centerX + (int) Math.round(predatorRadius * Math.cos(angle));
            int y = centerY + (int) Math.round(predatorRadius * Math.sin(angle));

            Position pos = findNearestWalkable(x, y);
            if (pos == null) continue;

            String name = "Predator_" + predatorPlaced;

            // CRITICAL FIX: Alternate male/female for reproduction
            Gender gender = (predatorPlaced % 2 == 0) ? Gender.MALE : Gender.FEMALE;

            try {
                Object[] args = {this, pos, gender, name};
                AgentController ac = container.createNewAgent(name,
                        "com.reactive.preypredator.agents.PredatorAgent", args);
                ac.start();
                predatorPlaced++;
            } catch (StaleProxyException e) {
                System.err.println("Error creating fixed-position predator: " + e.getMessage());
            }
        }

        System.out.println("[Environment] Fixed placement: " + preyPlaced + " prey (50% M/F), "
                + predatorPlaced + " predators (50% M/F)");
    }

    private Position findNearestWalkable(int x, int y) {
        if (grid.isWithinBounds(x, y) && grid.getCell(x, y).isWalkable()) {
            return new Position(x, y);
        }

        for (int radius = 1; radius <= 5; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int newX = x + dx;
                    int newY = y + dy;
                    if (grid.isWithinBounds(newX, newY) && grid.getCell(newX, newY).isWalkable()) {
                        return new Position(newX, newY);
                    }
                }
            }
        }
        return null;
    }

    private void createPreyAgent(String name) {
        Position pos = grid.getRandomEmptyPosition();
        if (pos == null) return;

        try {
            Object[] args = {this, pos, Gender.random(), name};
            AgentController ac = container.createNewAgent(name,
                    "com.reactive.preypredator.agents.PreyAgent", args);
            ac.start();
        } catch (StaleProxyException e) {
            System.err.println("Error creating prey agent: " + e.getMessage());
        }
    }

    private void createPredatorAgent(String name) {
        Position pos = grid.getRandomEmptyPosition();
        if (pos == null) return;

        try {
            Object[] args = {this, pos, Gender.random(), name};
            AgentController ac = container.createNewAgent(name,
                    "com.reactive.preypredator.agents.PredatorAgent", args);
            ac.start();
        } catch (StaleProxyException e) {
            System.err.println("Error creating predator agent: " + e.getMessage());
        }
    }

    public void createPreyOffspring(Position parentPos) {
        String name = "Prey_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
        createPreyAgent(name);
    }

    public void createPredatorOffspring(Position parentPos) {
        String name = "Predator_" + System.currentTimeMillis() + "_" + random.nextInt(1000);
        createPredatorAgent(name);
    }

    public synchronized void registerPreyAgent(PreyAgent agent) {
        preyAgents.put(agent.getLocalName(), agent);
        grid.setAgentPosition(agent.getLocalName(), agent.getPosition());
    }

    public synchronized void registerPredatorAgent(PredatorAgent agent) {
        predatorAgents.put(agent.getLocalName(), agent);
        grid.setAgentPosition(agent.getLocalName(), agent.getPosition());
    }

    public synchronized void moveAgent(String agentId, Position newPos) {
        if (isPositionAvailable(newPos.x, newPos.y)) {
            grid.setAgentPosition(agentId, newPos);
        }
    }

    public boolean isPositionAvailable(int x, int y) {
        if (!grid.isWithinBounds(x, y)) return false;
        if (!grid.getCell(x, y).isWalkable()) return false;

        Position pos = new Position(x, y);
        for (PreyAgent prey : preyAgents.values()) {
            if (prey.isAlive() && prey.getPosition().equals(pos)) return false;
        }
        for (PredatorAgent predator : predatorAgents.values()) {
            if (predator.isAlive() && predator.getPosition().equals(pos)) return false;
        }

        return true;
    }

    public List<Position> getNearbyPreyPositions(Position center, int range) {
        List<Position> positions = new ArrayList<>();
        for (PreyAgent prey : preyAgents.values()) {
            if (prey.isAlive() && center.distanceTo(prey.getPosition()) <= range) {
                positions.add(prey.getPosition());
            }
        }
        return positions;
    }

    public List<Position> getNearbyPredatorPositions(Position center, int range) {
        List<Position> positions = new ArrayList<>();
        for (PredatorAgent predator : predatorAgents.values()) {
            if (predator.isAlive() && center.distanceTo(predator.getPosition()) <= range) {
                positions.add(predator.getPosition());
            }
        }
        return positions;
    }

    public List<PreyAgent> getNearbyPreyAgents(Position center, int range) {
        List<PreyAgent> nearby = new ArrayList<>();
        for (PreyAgent prey : preyAgents.values()) {
            if (prey.isAlive() && center.distanceTo(prey.getPosition()) <= range) {
                nearby.add(prey);
            }
        }
        return nearby;
    }

    public List<PredatorAgent> getNearbyPredatorAgents(Position center, int range) {
        List<PredatorAgent> nearby = new ArrayList<>();
        for (PredatorAgent predator : predatorAgents.values()) {
            if (predator.isAlive() && center.distanceTo(predator.getPosition()) <= range) {
                nearby.add(predator);
            }
        }
        return nearby;
    }

    public synchronized void removeDeadAgent(String agentId) {
        deadAgents.add(agentId);
    }

    private void cleanupDeadAgents() {
        for (String agentId : deadAgents) {
            preyAgents.remove(agentId);
            predatorAgents.remove(agentId);
            grid.removeAgent(agentId);

            try {
                AgentController ac = container.getAgent(agentId);
                if (ac != null) {
                    ac.kill();
                }
            } catch (Exception e) {
                // Already dead
            }
        }
        deadAgents.clear();
    }

    public void tick() {
        if (!running) return;

        currentTick++;

        int activeAgents = 0;
        for (PreyAgent prey : preyAgents.values()) {
            if (prey.isAlive()) activeAgents++;
        }
        for (PredatorAgent predator : predatorAgents.values()) {
            if (predator.isAlive()) activeAgents++;
        }

        if (activeAgents == 0) return;

        agentLatch = new CountDownLatch(activeAgents);

        synchronized (tickLock) {
            tickActive = true;
            tickLock.notifyAll();
        }

        try {
            boolean completed = agentLatch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                System.err.println("[Environment] Warning: Tick " + currentTick + " timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        synchronized (tickLock) {
            tickActive = false;
        }

        cleanupDeadAgents();
        grid.updateGrassRegrowth();
        collectStatistics();
    }

    public void signalAgentCompletion() {
        if (agentLatch != null) {
            agentLatch.countDown();
        }
    }

    private void collectStatistics() {
        int preyCount = 0;
        int predatorCount = 0;
        double totalPreyEnergy = 0;
        double totalPredatorEnergy = 0;

        for (PreyAgent prey : preyAgents.values()) {
            if (prey.isAlive()) {
                preyCount++;
                totalPreyEnergy += prey.getEnergy();
            }
        }

        for (PredatorAgent predator : predatorAgents.values()) {
            if (predator.isAlive()) {
                predatorCount++;
                totalPredatorEnergy += predator.getEnergy();
            }
        }

        double avgPreyEnergy = preyCount > 0 ? totalPreyEnergy / preyCount : 0.0;
        double avgPredatorEnergy = predatorCount > 0 ? totalPredatorEnergy / predatorCount : 0.0;
        double grassCoverage = grid.getGrassCoverage();

        Statistics stats = new Statistics(currentTick, preyCount, predatorCount,
                avgPreyEnergy, avgPredatorEnergy, grassCoverage);
        dataLogger.log(stats);
    }

    public Grid getGrid() {
        return grid;
    }

    public synchronized Collection<PreyAgent> getPreyAgents() {
        List<PreyAgent> alive = new ArrayList<>();
        for (PreyAgent prey : preyAgents.values()) {
            if (prey.isAlive()) {
                alive.add(prey);
            }
        }
        return alive;
    }

    public synchronized Collection<PredatorAgent> getPredatorAgents() {
        List<PredatorAgent> alive = new ArrayList<>();
        for (PredatorAgent predator : predatorAgents.values()) {
            if (predator.isAlive()) {
                alive.add(predator);
            }
        }
        return alive;
    }

    public DataLogger getDataLogger() {
        return dataLogger;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public Object getTickLock() {
        return tickLock;
    }

    public boolean isTickActive() {
        return tickActive;
    }

    public void shutdown() {
        try {
            container.kill();
        } catch (Exception e) {
            System.err.println("Error shutting down JADE: " + e.getMessage());
        }
    }
}
