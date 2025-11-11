package com.reactive.preypredator.statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataLogger {
    private List<Statistics> history;
    private String outputFile;
    private boolean headerWritten = false;
    private static boolean fileCleared = false; // Ensures clearing happens only once

    public DataLogger(String outputFile) {
        this.history = new ArrayList<>();
        this.outputFile = outputFile;

        // Clear the file at the first instantiation
        clearFile();
    }

    /**
     * Clear the CSV file content when simulation starts
     */
    private void clearFile() {
        if (!fileCleared) {
            try (FileWriter writer = new FileWriter(outputFile, false)) {
                // Open in overwrite mode (false) to erase everything
                writer.write("");
                fileCleared = true;
                System.out.println("[DataLogger] Cleared old simulation data from " + outputFile);
            } catch (IOException e) {
                System.err.println("Error clearing CSV file: " + e.getMessage());
            }
        }
    }

    /**
     * Log a statistics snapshot and write to file
     */
    public void log(Statistics stats) {
        history.add(stats);
        writeToFile(stats);
    }

    private void writeToFile(Statistics stats) {
        try (FileWriter writer = new FileWriter(outputFile, true)) {
            if (!headerWritten && history.size() == 1) {
                writer.write("Tick,PreyCount,PredatorCount,AvgPreyEnergy,AvgPredatorEnergy,GrassCoverage\n");
                headerWritten = true;
            }
            writer.write(String.format("%d,%d,%d,%.2f,%.2f,%.4f\n",
                    stats.getTick(),
                    stats.getPreyCount(),
                    stats.getPredatorCount(),
                    stats.getAvgPreyEnergy(),
                    stats.getAvgPredatorEnergy(),
                    stats.getGrassCoverage()));
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }

    public List<Statistics> getHistory() {
        return new ArrayList<>(history);
    }

    public Statistics getLatest() {
        return history.isEmpty() ? null : history.get(history.size() - 1);
    }

    public void printSummary() {
        if (history.isEmpty()) {
            System.out.println("No data logged.");
            return;
        }

        Statistics first = history.get(0);
        Statistics last = history.get(history.size() - 1);

        System.out.println("\n=== Simulation Summary ===");
        System.out.println("Duration: " + last.getTick() + " ticks");
        System.out.println("Initial -> Final Prey: " + first.getPreyCount() + " -> " + last.getPreyCount());
        System.out.println("Initial -> Final Predators: " + first.getPredatorCount() + " -> " + last.getPredatorCount());
        System.out.println("Final Grass Coverage: " + String.format("%.2f%%", last.getGrassCoverage() * 100));
        System.out.println("==========================");
    }

    public String getOutputFile() {
        return outputFile;
    }
}
