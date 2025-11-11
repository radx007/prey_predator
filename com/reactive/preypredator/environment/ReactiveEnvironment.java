package com.reactive.preypredator.environment;

import com.reactive.preypredator.agents.PreyAgent;
import com.reactive.preypredator.agents.PredatorAgent;
import com.reactive.preypredator.config.Config;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reactive environment that manages the simulation without ACL messages
 */
public class ReactiveEnvironment {
    private Grid grid;
    private AgentContainer container;
    private Map<String, PreyAgent> preyRegistry;
    private Map<String, PredatorAgent> predatorRegistry;
    private DataLogger dataLogger;
    private int currentTick;
    private boolean running;

    // Synchronization for tick-based execution
    private volatile boolean tickReady;
    private AtomicInteger preyActionsComplete;
    private AtomicInteger predatorActionsComplete;

    public ReactiveEnvironment() {
        this.grid = new Grid(Config.GRID_WIDTH, Config.GRID_HEIGHT);
        this.preyRegistry = new ConcurrentHashMap<>();
        this.predatorRegistry = new ConcurrentHashMap<>();
        this.dataLogger = new DataLogger("simulation_data.csv");
        this.currentTick = 0;
        this.running = true;
        this.tickReady = false;
        this.preyActionsComplete = new AtomicInteger(0);
        this.predatorActionsComplete = new AtomicInteger(0);

        initializeJADE();

        // Give JADE time to initialize
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        initializeAgents();

        // Give agents time to register
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Environment initialized with " +
                preyRegistry.size() + " prey and " +
                predatorRegistry.size() + " predators");
    }

    private void initializeJADE() {
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.GUI, "false");
        container = rt.createMainContainer(profile);
    }

    private void initializeAgents() {
        // Create prey agents
        for (int i = 0; i < Config.INITIAL_PREY_COUNT; i++) {
            Position pos = grid.getRandomWalkablePosition();
            createPreyAgent(pos, Gender.random());
        }

        // Create predator agents
        for (int i = 0; i < Config.INITIAL_PREDATOR_COUNT; i++) {
            Position pos = grid.getRandomWalkablePosition();
            createPredatorAgent(pos, Gender.random());
        }
    }

    public synchronized void createPreyAgent(Position pos, Gender gender) {
        try {
            String name = "Prey_" + System.nanoTime();
            Object[] args = {this, pos, gender, name};

            AgentController ac = container.createNewAgent(name,
                    "com.reactive.preypredator.agents.PreyAgent", args);
            ac.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public synchronized void createPredatorAgent(Position pos, Gender gender) {
        try {
            String name = "Predator_" + System.nanoTime();
            Object[] args = {this, pos, gender, name};

            AgentController ac = container.createNewAgent(name,
                    "com.reactive.preypredator.agents.PredatorAgent", args);
            ac.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    // Registration methods called by agents
    public synchronized void registerPreyAgent(String name, PreyAgent agent) {
        preyRegistry.put(name, agent);
        grid.setAgentPosition(name, agent.getPosition());
    }

    public synchronized void registerPredatorAgent(String name, PredatorAgent agent) {
        predatorRegistry.put(name, agent);
        grid.setAgentPosition(name, agent.getPosition());
    }

    public synchronized void removePreyAgent(String name) {
        grid.removeAgent(name);
        preyRegistry.remove(name);
    }

    public synchronized void removePredatorAgent(String name) {
        grid.removeAgent(name);
        predatorRegistry.remove(name);
    }

    /**
     * Execute one simulation tick with proper synchronization
     */
    public void tick() {
        if (!running) return;

        currentTick++;

        // Phase 1: Environment updates (no agent interaction)
        grid.updateGrassRegrowth();

        // Phase 2: Signal agents to act
        int preyCount = preyRegistry.size();
        int predCount = predatorRegistry.size();

        preyActionsComplete.set(0);
        predatorActionsComplete.set(0);
        tickReady = true;

        // Phase 3: Wait for all agents to complete their actions
        long startWait = System.currentTimeMillis();
        long timeout = 5000; // FIXED: Reduced to 5 seconds for faster response

        while ((preyActionsComplete.get() < preyCount ||
                predatorActionsComplete.get() < predCount) &&
                System.currentTimeMillis() - startWait < timeout) {
            try {
                Thread.sleep(2); // FIXED: Reduced sleep for faster response
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        tickReady = false;

        // FIXED: Removed the 50ms buffer - we want immediate response

        // Phase 4: Clean up dead agents (safe to modify now)
        cleanupDeadAgents();

        // Phase 5: Collect statistics
        collectStatistics();

        // Debug output every 10 ticks
        if (currentTick % 10 == 0) {
            Statistics latest = dataLogger.getLatest();
            if (latest != null) {
                System.out.println(String.format(
                        "[Tick %d] Prey: %d (Avg Energy: %.1f) | Predators: %d (Avg Energy: %.1f) | Grass: %.1f%%",
                        currentTick, latest.getPreyCount(), latest.getAvgPreyEnergy(),
                        latest.getPredatorCount(), latest.getAvgPredatorEnergy(),
                        latest.getGrassCoverage() * 100
                ));
            }
        }

        // Phase 6: Check for extinction
        if (preyRegistry.isEmpty() || predatorRegistry.isEmpty()) {
            System.out.println("\n!!! Simulation ended - extinction at tick " + currentTick + " !!!");
            System.out.println("Remaining Prey: " + preyRegistry.size());
            System.out.println("Remaining Predators: " + predatorRegistry.size());
            dataLogger.printSummary();
            running = false;
        }
    }

    private void cleanupDeadAgents() {
        // Create snapshots to avoid concurrent modification
        List<String> deadPrey = new ArrayList<>();
        List<String> deadPreds = new ArrayList<>();

        // Identify dead prey
        for (Map.Entry<String, PreyAgent> entry : preyRegistry.entrySet()) {
            PreyAgent prey = entry.getValue();
            if (prey != null && (!prey.isAlive() || prey.isDead())) {
                deadPrey.add(entry.getKey());
            }
        }

        // Identify dead predators
        for (Map.Entry<String, PredatorAgent> entry : predatorRegistry.entrySet()) {
            PredatorAgent pred = entry.getValue();
            if (pred != null && (!pred.isAlive() || pred.isDead())) {
                deadPreds.add(entry.getKey());
            }
        }

        // Remove dead agents
        for (String name : deadPrey) {
            grid.removeAgent(name);
            preyRegistry.remove(name);
        }

        for (String name : deadPreds) {
            grid.removeAgent(name);
            predatorRegistry.remove(name);
        }

        if (!deadPrey.isEmpty() || !deadPreds.isEmpty()) {
            System.out.println(String.format("[Tick %d] Removed %d dead prey, %d dead predators",
                    currentTick, deadPrey.size(), deadPreds.size()));
        }
    }

    private void collectStatistics() {
        int preyCount = preyRegistry.size();
        int predatorCount = predatorRegistry.size();

        double avgPreyEnergy = preyRegistry.values().stream()
                .filter(Objects::nonNull)
                .mapToInt(PreyAgent::getEnergy)
                .average()
                .orElse(0.0);

        double avgPredatorEnergy = predatorRegistry.values().stream()
                .filter(Objects::nonNull)
                .mapToInt(PredatorAgent::getEnergy)
                .average()
                .orElse(0.0);

        double grassCoverage = grid.getGrassCoverage();

        Statistics stats = new Statistics(currentTick, preyCount, predatorCount,
                avgPreyEnergy, avgPredatorEnergy, grassCoverage);

        dataLogger.log(stats);
    }

    // Synchronization methods for agents
    public boolean isTickReady() {
        return tickReady;
    }

    public void signalPreyActionComplete() {
        preyActionsComplete.incrementAndGet();
    }

    public void signalPredatorActionComplete() {
        predatorActionsComplete.incrementAndGet();
    }

    // Getters - return snapshots for thread safety
    public Grid getGrid() { return grid; }

    public synchronized List<PreyAgent> getPreyAgents() {
        return new ArrayList<>(preyRegistry.values());
    }

    public synchronized List<PredatorAgent> getPredatorAgents() {
        return new ArrayList<>(predatorRegistry.values());
    }

    public DataLogger getDataLogger() { return dataLogger; }
    public int getCurrentTick() { return currentTick; }
    public boolean isRunning() { return running; }
    public void setRunning(boolean running) { this.running = running; }

    public void shutdown() {
        running = false;
        try {
            container.kill();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}