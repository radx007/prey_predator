package com.reactive.preypredator.statistics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Logs simulation statistics over time and writes to CSV
 */
public class DataLogger {
    private LinkedList<Statistics> history;
    private String outputFile;
    private boolean headerWritten = false;
    private static boolean fileCleared = false;

    public DataLogger(String outputFile) {
        this.history = new LinkedList<>();
        this.outputFile = outputFile;
        clearFile();
    }

    /**
     * Clear the CSV file content when simulation starts
     */
    private void clearFile() {
        if (!fileCleared) {
            try (FileWriter writer = new FileWriter(outputFile, false)) {
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
    public synchronized void log(Statistics stats) {
        history.add(stats);
        writeToFile(stats);
    }

    private void writeToFile(Statistics stats) {
        try (FileWriter writer = new FileWriter(outputFile, true)) {
            if (!headerWritten && history.size() == 1) {
                writer.write(Statistics.getCSVHeader() + "\n");
                headerWritten = true;
            }
            writer.write(stats.toCSV() + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }

    public synchronized LinkedList<Statistics> getHistory() {
        return new LinkedList<>(history);
    }

    public synchronized Statistics getLatest() {
        return history.isEmpty() ? null : history.getLast();
    }

    public void printSummary() {
        if (history.isEmpty()) {
            System.out.println("No data logged.");
            return;
        }

        Statistics first = history.getFirst();
        Statistics last = history.getLast();

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

    public synchronized void clear() {
        history.clear();
    }
}