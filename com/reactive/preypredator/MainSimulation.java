package com.reactive.preypredator;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.ui.SimulationUI;
import resources.past.SimulationUi;

/**
 * Main entry point for the reactive JADE predator-prey simulation
 */
public class MainSimulation {

    public static void main(String[] args) {
        System.out.println("Starting REACTIVE Predator-Prey Multi-Agent Simulation");
        System.out.println("=========================================================");
        System.out.println("Grid Size: " + Config.GRID_WIDTH + "x" + Config.GRID_HEIGHT);
        System.out.println("Initial Prey: " + Config.INITIAL_PREY_COUNT);
        System.out.println("Initial Predators: " + Config.INITIAL_PREDATOR_COUNT);
        System.out.println("Tick Delay: " + Config.TICK_DURATION_MS + "ms");
        System.out.println("=========================================================\n");

        // Create reactive environment
        ReactiveEnvironment environment = new ReactiveEnvironment();

        // Create UI
//        SimulationUi ui = new SimulationUi();
        SimulationUI ui = new SimulationUI(environment);
        // Start simulation loop
        Thread simulationThread = new Thread(() -> {
            System.out.println("\nStarting simulation loop...\n");

            while (environment.isRunning()) {
                if (!ui.isPaused()) {
                    long tickStart = System.currentTimeMillis();

                    // Execute one tick
                    environment.tick();

                    // FIXED: Update UI immediately after tick (no delay)
                    ui.updateDisplay();

                    // Calculate remaining delay
                    long tickDuration = System.currentTimeMillis() - tickStart;
                    long remainingDelay = Math.max(0, Config.TICK_DELAY - tickDuration);

                    // Sleep for the remaining time to maintain consistent tick rate
                    try {
                        Thread.sleep(remainingDelay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    // When paused, still update UI to show current state
                    ui.updateDisplay();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            System.out.println("\nSimulation loop ended.");
            environment.getDataLogger().printSummary();
        });

        simulationThread.start();

        // Optional: Additional UI update thread for extra smoothness
        Thread uiUpdateThread = new Thread(() -> {
            while (environment.isRunning()) {
                if (!ui.isPaused()) {
                    ui.updateDisplay();
                }
                try {
                    // Update UI more frequently than ticks (every 100ms)
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        // Uncomment the line below if you want continuous UI updates
        // uiUpdateThread.start();

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down simulation...");
            environment.setRunning(false);
            environment.shutdown();
        }));
    }
}