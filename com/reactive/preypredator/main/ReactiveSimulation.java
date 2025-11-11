package com.reactive.preypredator.main;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;

import com.reactive.preypredator.ui.SimulationUI;
import resources.past.SimulationUi;

import javax.swing.*;

/**
 * Main entry point for the REACTIVE Predator-Prey Simulation
 * NO JADE FRAMEWORK - NO ACL MESSAGES
 * Pure reactive agents that directly observe and interact with environment
 */
public class ReactiveSimulation {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("REACTIVE Predator-Prey Simulation");
        System.out.println("NO JADE - NO ACL MESSAGES");
        System.out.println("Pure Reactive Multi-Agent System");
        System.out.println("========================================");
        System.out.println("Configuration:");
        System.out.println("  Grid Size: " + Config.GRID_WIDTH + "x" + Config.GRID_HEIGHT);
        System.out.println("  Initial Prey: " + Config.INITIAL_PREY_COUNT);
        System.out.println("  Initial Predators: " + Config.INITIAL_PREDATOR_COUNT);
        System.out.println("  Max Ticks: " + Config.MAX_TICKS);
        System.out.println("  Tick Duration: " + Config.TICK_DURATION_MS + "ms");
        System.out.println("========================================\n");

        // Create reactive environment
        ReactiveEnvironment environment = new ReactiveEnvironment();

        // Create UI
//        SimulationUI ui = new SimulationUI(environment);
        SimulationUi simulation = new SimulationUi();
        // Main simulation loop in separate thread
//        new Thread(() -> {
//            while (environment.isRunning()) {
//                try {
//                    Thread.sleep(Config.TICK_DURATION_MS);
//
//                    if (!ui.isPaused()) {
//                        // All agents react to current environment state
//                        environment.tick();
//
//                        // Update UI on Event Dispatch Thread
//                        SwingUtilities.invokeLater(() -> ui.updateDisplay());
//                    }
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    break;
//                }
//            }
//
//            // Simulation ended
//            System.out.println("\n========================================");
//            System.out.println("SIMULATION ENDED");
//            System.out.println("========================================");
//            environment.getDataLogger().printSummary();
//
//        }).start();
    }
}