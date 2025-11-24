package com.reactive.preypredator;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.ui.SimulationUI;
import com.reactive.preypredator.ui.StartupDialog;

import javax.swing.*;

/**
 * Main entry point with throttled UI updates
 */
public class MainSimulation {
    private static volatile boolean shouldRestart = false;
    private static volatile boolean showStartupDialog = true;
    private static ReactiveEnvironment environment;
    private static SimulationUI ui;
    private static long lastUIUpdate = 0;
    private static final long UI_UPDATE_INTERVAL = 50; // Update UI every 50ms minimum

    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
        }

        while (true) {
            // Show startup dialog
            if (showStartupDialog) {
                StartupDialog startupDialog = StartupDialog.showDialog();

                if (!startupDialog.shouldStartSimulation()) {
                    System.out.println("Simulation cancelled by user.");
                    break;
                }

                System.out.println("\n" + (startupDialog.isUseDefaultConfig() ?
                        "Using default configuration" : "Using custom configuration"));
            }

            showStartupDialog = false;
            shouldRestart = false;

            runSimulation();

            // Check if we should restart or show startup dialog again
            if (!shouldRestart) {
                break;
            }
        }

        System.out.println("\nProgram terminated.");
        System.exit(0);
    }

    private static void runSimulation() {
        System.out.println("\n==========================================================");
        System.out.println("Starting REACTIVE Predator-Prey Multi-Agent Simulation");
        System.out.println("==========================================================");
        System.out.println("Grid Size: " + Config.GRID_WIDTH + "x" + Config.GRID_HEIGHT);
        System.out.println("Initial Prey: " + Config.INITIAL_PREY_COUNT);
        System.out.println("Initial Predators: " + Config.INITIAL_PREDATOR_COUNT);
        System.out.println("Tick Duration: " + Config.TICK_DURATION_MS + "ms");
        System.out.println("Max Ticks: " + Config.MAX_TICKS);
        System.out.println("==========================================================\n");

        // Create reactive environment
        environment = new ReactiveEnvironment();

        // Create UI with restart callback
        if (ui != null) {
            ui.dispose();
        }

        ui = new SimulationUI(environment, () -> {
            shouldRestart = true;
            showStartupDialog = false; // Restart with same config
        });

        // Reset UI update timer
        lastUIUpdate = System.currentTimeMillis();

        // Main simulation loop
        for (int tick = 0; tick < Config.MAX_TICKS && !ui.isStopped(); tick++) {
            // Pause handling
            while (ui.isPaused() && !ui.isStopped()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            if (ui.isStopped()) break;

            // Execute tick
            environment.tick();

            // Throttle UI updates - only update every UI_UPDATE_INTERVAL ms
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUIUpdate >= UI_UPDATE_INTERVAL) {
                SwingUtilities.invokeLater(() -> ui.updateDisplay());
                lastUIUpdate = currentTime;
            }

            // Check extinction
            long preyCount = environment.getPreyAgents().size();
            long predatorCount = environment.getPredatorAgents().size();

            if (preyCount == 0 && predatorCount == 0) {
                System.out.println("\nEXTINCTION EVENT! Both populations died out at tick " + tick);
                String message = "Extinction Event at tick " + tick + "!\n" +
                        "Both populations have died out.\n\n" +
                        "Suggestions:\n" +
                        "- Increase initial populations\n" +
                        "- Lower energy costs\n" +
                        "- Increase energy gains\n" +
                        "- Faster grass regrowth";
                ui.showExtinctionDialog(message);
                // Force final UI update
                SwingUtilities.invokeLater(() -> ui.updateDisplay());
                break;
            }

            if (preyCount == 0 && tick % 50 == 0) {
                System.out.println("Prey extinction at tick " + tick + "! Only " + predatorCount + " predators remain.");
            }

            if (predatorCount == 0 && tick > 100 && tick % 50 == 0) {
                System.out.println("Predators extinct at tick " + tick + "! " + preyCount + " prey population stabilizing.");
            }

            // Delay for visualization
            try {
                Thread.sleep(Config.TICK_DURATION_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Force final UI update
        SwingUtilities.invokeLater(() -> ui.updateDisplay());

        // Print final summary
        if (!shouldRestart) {
            environment.getDataLogger().printSummary();
            System.out.println("\nSimulation data saved to: " + Config.CSV_OUTPUT_FILE);
        }

        // Shutdown
        if (!shouldRestart) {
            environment.shutdown();
            if (ui != null) {
                ui.dispose();
            }
            System.out.println("\nSimulation completed successfully!");
        } else {
            environment.shutdown();
            System.out.println("\nRestarting simulation...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
