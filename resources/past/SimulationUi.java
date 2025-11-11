//package resources.past;
//
//import com.reactive.preypredator.config.Config;
//import com.reactive.preypredator.ui.SimulationUI;
//
//import javax.swing.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
///**
// * FAKE Simulation for testing UI with Lotka-Volterra dynamics
// * Simulates population changes without actual agents
// * Starting: 40 Prey, 15 Predators
// */
//public class SimulationUi {
//    private Environmment fakeEnv;
//    private SimulationUI ui;
//    private Thread simulationThread;
//
//    public SimulationUi() {
//        System.out.println("========================================");
//        System.out.println("FAKE Simulation - Testing UI");
//        System.out.println("Lotka-Volterra Population Dynamics");
//        System.out.println("========================================");
//        System.out.println("Initial Prey: 40");
//        System.out.println("Initial Predators: 15");
//        System.out.println("========================================\n");
//
//        // Create fake environment
//        fakeEnv = new Environmment();
//
//        // Create UI
//        ui = new SimulationUI(fakeEnv);
//
//        // Start simulation
//        start();
//    }
//
//    private void start() {
//        simulationThread = new Thread(() -> {
//            while (fakeEnv.isRunning()) {
//                try {
//                    Thread.sleep(Config.TICK_DURATION_MS);
//
//                    if (!ui.isPaused()) {
//                        fakeEnv.tick();
//                        SwingUtilities.invokeLater(() -> ui.updateDisplay());
//                    }
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    break;
//                }
//            }
//
//            System.out.println("\n========================================");
//            System.out.println("SIMULATION ENDED");
//            System.out.println("========================================");
//            fakeEnv.getDataLogger().printSummary();
//        });
//
//        simulationThread.start();
//    }
//
//    public Environmment getEnvironment() {
//        return fakeEnv;
//    }
//
//    public SimulationUI getUI() {
//        return ui;
//    }
//
//    public void stop() {
//        if (simulationThread != null && simulationThread.isAlive()) {
//            simulationThread.interrupt();
//        }
//    }
//}
//
///**
// * Fake Environment that mimics ReactiveEnvironment
// * Uses Lotka-Volterra equations for population dynamics
// */
//class Environmment extends com.reactive.preypredator.environment.ReactiveEnvironment {
//    private int currentTick;
//    private double preyPopulation;
//    private double predatorPopulation;
//    private double grassCoverage;
//    private double avgPreyEnergy;
//    private double avgPredatorEnergy;
//    private Random random;
//    private com.reactive.preypredator.statistics.DataLogger dataLogger;
//    private Gridd fakeGrid;
//    private List<PrreyAgent> fakePrey;
//    private List<PredattorAgent> fakePredators;
//
//    // Lotka-Volterra parameters (tuned for oscillations with lag)
//    private static final double ALPHA = 0.09;      // Prey birth rate
//    private static final double BETA = 0.0045;     // Predation rate
//    private static final double GAMMA = 0.055;     // Predator death rate
//    private static final double DELTA = 0.0022;    // Predator efficiency
//
//    public Environmment() {
//        super();
//        this.currentTick = 0;
//        this.preyPopulation = 40.0;           // Start with 40 prey
//        this.predatorPopulation = 15.0;       // Start with 15 predators
//        this.grassCoverage = 0.60;
//        this.avgPreyEnergy = 60.0;
//        this.avgPredatorEnergy = 80.0;
//        this.random = new Random();
//        this.dataLogger = new com.reactive.preypredator.statistics.DataLogger("fake_simulation_stats.csv");
//        this.fakeGrid = new Gridd();
//        this.fakePrey = new ArrayList<>();
//        this.fakePredators = new ArrayList<>();
//
//        // Initialize fake agents
//        initializeFakeAgents();
//
//        System.out.println("Fake Environment initialized:");
//        System.out.println("  - Prey: " + (int)preyPopulation);
//        System.out.println("  - Predators: " + (int)predatorPopulation);
//    }
//
//    private void initializeFakeAgents() {
//        // Create fake prey agents
//        for (int i = 0; i < (int)preyPopulation; i++) {
//            fakePrey.add(new PrreyAgent(random));
//        }
//
//        // Create fake predators
//        for (int i = 0; i < (int)predatorPopulation; i++) {
//            fakePredators.add(new PredattorAgent(random));
//        }
//    }
//
//    @Override
//    public void tick() {
//        currentTick++;
//
//        // ========== LOTKA-VOLTERRA EQUATIONS ==========
//        double preyChange = ALPHA * preyPopulation - BETA * preyPopulation * predatorPopulation;
//        double predatorChange = DELTA * preyPopulation * predatorPopulation - GAMMA * predatorPopulation;
//
//        // Add realistic noise
//        double preyNoise = (random.nextDouble() - 0.5) * 2.5;
//        double predatorNoise = (random.nextDouble() - 0.5) * 1.8;
//
//        // Update populations
//        preyPopulation = Math.max(8, Math.min(180, preyPopulation + preyChange + preyNoise));
//        predatorPopulation = Math.max(3, Math.min(90, predatorPopulation + predatorChange + predatorNoise));
//
//        // ========== UPDATE ENERGIES ==========
//        // Prey energy varies based on grass availability
//        avgPreyEnergy = 55 + random.nextDouble() * 20 + (grassCoverage > 0.5 ? 5 : -5);
//        avgPreyEnergy = Math.max(30, Math.min(100, avgPreyEnergy));
//
//        // Predator energy varies based on prey availability
//        double preyDensity = preyPopulation / Math.max(1, predatorPopulation);
//        avgPredatorEnergy = 70 + random.nextDouble() * 25 + (preyDensity > 2 ? 8 : -8);
//        avgPredatorEnergy = Math.max(40, Math.min(120, avgPredatorEnergy));
//
//        // ========== UPDATE GRASS COVERAGE ==========
//        // Grass decreases when prey is high, increases when prey is low
//        double grassTarget = 0.60 - (preyPopulation / 200.0) * 0.3;
//        grassCoverage = grassCoverage * 0.95 + grassTarget * 0.05;
//        grassCoverage = Math.max(0.20, Math.min(0.80, grassCoverage));
//
//        // ========== UPDATE FAKE AGENTS ==========
//        updateFakeAgents();
//
//        // ========== UPDATE GRID ==========
//        fakeGrid.updateWithPopulations((int)preyPopulation, (int)predatorPopulation, grassCoverage);
//
//        // ========== COLLECT STATISTICS ==========
//        collectStatistics();
//
//        // ========== LOG PROGRESS ==========
//        if (currentTick % 20 == 0) {
//            com.reactive.preypredator.statistics.Statistics stats = dataLogger.getLatest();
//            if (stats != null) {
//                System.out.println(String.format("Tick %d: Prey=%d (E=%.1f) | Predators=%d (E=%.1f) | Grass=%.1f%%",
//                        currentTick, stats.getPreyCount(), stats.getAvgPreyEnergy(),
//                        stats.getPredatorCount(), stats.getAvgPredatorEnergy(), stats.getGrassCoverage() * 100));
//            }
//        }
//    }
//
//    private void updateFakeAgents() {
//        int targetPreyCount = (int)Math.round(preyPopulation);
//        int targetPredatorCount = (int)Math.round(predatorPopulation);
//
//        // Adjust fake prey list
//        while (fakePrey.size() < targetPreyCount) {
//            fakePrey.add(new PrreyAgent(random));
//        }
//        while (fakePrey.size() > targetPreyCount && fakePrey.size() > 1) {
//            fakePrey.remove(fakePrey.size() - 1);
//        }
//
//        // Adjust fake predator list
//        while (fakePredators.size() < targetPredatorCount) {
//            fakePredators.add(new PredattorAgent(random));
//        }
//        while (fakePredators.size() > targetPredatorCount && fakePredators.size() > 1) {
//            fakePredators.remove(fakePredators.size() - 1);
//        }
//
//        // Update agent positions randomly
//        fakePrey.forEach(agent -> agent.move());
//        fakePredators.forEach(agent -> agent.move());
//    }
//
//    private void collectStatistics() {
//        int preyCount = (int)Math.round(preyPopulation);
//        int predatorCount = (int)Math.round(predatorPopulation);
//
//        com.reactive.preypredator.statistics.Statistics stats =
//                new com.reactive.preypredator.statistics.Statistics(
//                        currentTick, preyCount, predatorCount,
//                        avgPreyEnergy, avgPredatorEnergy, grassCoverage
//                );
//        dataLogger.log(stats);
//    }
//
//    @Override
//    public boolean isRunning() {
//        return currentTick < Config.MAX_TICKS && preyPopulation > 5 && predatorPopulation > 2;
//    }
//
//    @Override
//    public com.reactive.preypredator.model.Grid getGrid() {
//        return fakeGrid;
//    }
//
//    @Override
//    public List<com.reactive.preypredator.agents.PreyAgent> getPreyAgents() {
//        List<com.reactive.preypredator.agents.PreyAgent> result = new ArrayList<>();
//        for (PrreyAgent fake : fakePrey) {
//            result.add(fake.toRealAgent());
//        }
//        return result;
//    }
//
//    @Override
//    public List<com.reactive.preypredator.agents.PredatorAgent> getPredatorAgents() {
//        List<com.reactive.preypredator.agents.PredatorAgent> result = new ArrayList<>();
//        for (PredattorAgent fake : fakePredators) {
//            result.add(fake.toRealAgent());
//        }
//        return result;
//    }
//
//    @Override
//    public com.reactive.preypredator.statistics.DataLogger getDataLogger() {
//        return dataLogger;
//    }
//
//    @Override
//    public int getCurrentTick() {
//        return currentTick;
//    }
//}
//
///**
// * Fake Grid that mimics real Grid behavior
// */
//class Gridd extends com.reactive.preypredator.model.Grid {
//    private Random random = new Random();
//
//    public Gridd() {
//        super(Config.GRID_WIDTH, Config.GRID_HEIGHT);
//    }
//
//    public void updateWithPopulations(int preyCount, int predatorCount, double grassCoverage) {
//        // Update grass dynamically based on coverage
//        for (int x = 0; x < getWidth(); x++) {
//            for (int y = 0; y < getHeight(); y++) {
//                com.reactive.preypredator.model.Cell cell = getCell(x, y);
//                if (cell != null) {
//                    // Randomly update grass to match target coverage
//                    if (!cell.hasGrass() && random.nextDouble() < grassCoverage * 0.05) {
//                        // Regrow grass
//                        cell.updateGrassRegrowth();
//                    }
//                }
//            }
//        }
//    }
//}
//
///**
// * Fake Prey Agent
// */
//class PrreyAgent {
//    private com.reactive.preypredator.model.Position position;
//    private int energy;
//    private Random random;
//
//    public PrreyAgent(Random random) {
//        this.random = random;
//        this.position = new com.reactive.preypredator.model.Position(
//                random.nextInt(Config.GRID_WIDTH),
//                random.nextInt(Config.GRID_HEIGHT)
//        );
//        this.energy = 50 + random.nextInt(30);
//    }
//
//    public void move() {
//        int dx = random.nextInt(3) - 1;
//        int dy = random.nextInt(3) - 1;
//        position.x = Math.max(0, Math.min(Config.GRID_WIDTH - 1, position.x + dx));
//        position.y = Math.max(0, Math.min(Config.GRID_HEIGHT - 1, position.y + dy));
//        energy = Math.max(30, Math.min(100, energy + random.nextInt(5) - 2));
//    }
//
//    public com.reactive.preypredator.agents.PreyAgent toRealAgent() {
//        return new com.reactive.preypredator.agents.PreyAgent(position, energy);
//    }
//}
//
///**
// * Fake Predator Agent
// */
//class PredattorAgent {
//    private com.reactive.preypredator.model.Position position;
//    private int energy;
//    private Random random;
//
//    public PredattorAgent(Random random) {
//        this.random = random;
//        this.position = new com.reactive.preypredator.model.Position(
//                random.nextInt(Config.GRID_WIDTH),
//                random.nextInt(Config.GRID_HEIGHT)
//        );
//        this.energy = 70 + random.nextInt(30);
//    }
//
//    public void move() {
//        int dx = random.nextInt(3) - 1;
//        int dy = random.nextInt(3) - 1;
//        position.x = Math.max(0, Math.min(Config.GRID_WIDTH - 1, position.x + dx));
//        position.y = Math.max(0, Math.min(Config.GRID_HEIGHT - 1, position.y + dy));
//        energy = Math.max(40, Math.min(120, energy + random.nextInt(5) - 2));
//    }
//
//    public com.reactive.preypredator.agents.PredatorAgent toRealAgent() {
//        return new com.reactive.preypredator.agents.PredatorAgent(position, energy);
//    }
//}