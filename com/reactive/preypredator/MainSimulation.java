package com.reactive.preypredator;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.ui.SimulationUI;

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
        System.out.println("Tick Delay: " + Config.TICK_DELAY + "ms");
        System.out.println("=========================================================\n");

        // Create reactive environment
        ReactiveEnvironment environment = new ReactiveEnvironment();

        // Create UI
        SimulationUI ui = new SimulationUI(environment);

        // Start simulation loop
        Thread simulationThread = new Thread(() -> {
            System.out.println("\nStarting simulation loop...\n");

            while (environment.isRunning()) {
                if (!ui.isPaused()) {
                    long tickStart = System.currentTimeMillis();

                    // Execute one tick
                    environment.tick();

                    // Update UI after tick completes
                    try {
                        Thread.sleep(50); // Small delay before UI update
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    ui.updateDisplay();

                    // Calculate remaining delay
                    long tickDuration = System.currentTimeMillis() - tickStart;
                    long remainingDelay = Math.max(0, Config.TICK_DELAY - tickDuration);

                    try {
                        Thread.sleep(remainingDelay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    try {
                        Thread.sleep(100); // When paused
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

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down simulation...");
            environment.setRunning(false);
            environment.shutdown();
        }));
    }
}