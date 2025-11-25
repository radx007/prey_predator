package com.reactive.preypredator.statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

/**
 * Enhanced DataLogger with detailed console output for debugging
 */
public class DataLogger {
    private final String csvFilePath;
    private final LinkedList<Statistics> history;
    private PrintWriter csvWriter;

    public DataLogger(String csvFilePath) {
        this.csvFilePath = csvFilePath;
        this.history = new LinkedList<>();
        initializeCSV();
    }

    private void initializeCSV() {
        try {
            csvWriter = new PrintWriter(new FileWriter(csvFilePath));
            csvWriter.println("Tick,PreyCount,PredatorCount,AvgPreyEnergy,AvgPredatorEnergy,GrassCoverage");
        } catch (IOException e) {
            System.err.println("Error initializing CSV: " + e.getMessage());
        }
    }

    public void log(Statistics stats) {
        history.add(stats);

        // Write to CSV
        if (csvWriter != null) {
            csvWriter.printf("%d,%d,%d,%.2f,%.2f,%.4f%n",
                    stats.getTick(),
                    stats.getPreyCount(),
                    stats.getPredatorCount(),
                    stats.getAvgPreyEnergy(),
                    stats.getAvgPredatorEnergy(),
                    stats.getGrassCoverage()
            );
            csvWriter.flush();
        }

        // DETAILED CONSOLE OUTPUT every 5 ticks or first 20 ticks
        if (stats.getTick() % 5 == 0 || stats.getTick() <= 20) {
            System.out.println("═══════════════════════════════════════════════════════");
            System.out.printf("TICK %d SUMMARY:%n", stats.getTick());
            System.out.println("───────────────────────────────────────────────────────");
            System.out.printf("  Prey:      %3d  (Avg Energy: %.1f)%n",
                    stats.getPreyCount(), stats.getAvgPreyEnergy());
            System.out.printf("  Predators: %3d  (Avg Energy: %.1f)%n",
                    stats.getPredatorCount(), stats.getAvgPredatorEnergy());
            System.out.printf("  Grass:     %.1f%%%n", stats.getGrassCoverage() * 100);

            // Calculate ratio
            if (stats.getPredatorCount() > 0) {
                double ratio = (double) stats.getPreyCount() / stats.getPredatorCount();
                System.out.printf("  Prey:Predator Ratio: %.2f:1%n", ratio);
            }
            System.out.println("═══════════════════════════════════════════════════════");
        }
    }

    public LinkedList<Statistics> getHistory() {
        return history;
    }

    public Statistics getLatest() {
        return history.isEmpty() ? null : history.getLast();
    }

    public void printSummary() {
        System.out.println("\n════════════════════════════════════════════════════════");
        System.out.println("                   SIMULATION SUMMARY                    ");
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("Total ticks: " + history.size());

        if (!history.isEmpty()) {
            Statistics last = history.getLast();
            System.out.printf("Final prey count: %d%n", last.getPreyCount());
            System.out.printf("Final predator count: %d%n", last.getPredatorCount());
            System.out.printf("Final grass coverage: %.2f%%%n", last.getGrassCoverage() * 100);
        }
        System.out.println("════════════════════════════════════════════════════════\n");

        if (csvWriter != null) {
            csvWriter.close();
        }
    }
}
