package com.reactive.preypredator.statistics;

/**
 * Statistics snapshot for a single simulation tick
 */
public class Statistics {
    private int tick;
    private int preyCount;
    private int predatorCount;
    private double avgPreyEnergy;
    private double avgPredatorEnergy;
    private double grassCoverage;

    public Statistics(int tick, int preyCount, int predatorCount,
                      double avgPreyEnergy, double avgPredatorEnergy, double grassCoverage) {
        this.tick = tick;
        this.preyCount = preyCount;
        this.predatorCount = predatorCount;
        this.avgPreyEnergy = avgPreyEnergy;
        this.avgPredatorEnergy = avgPredatorEnergy;
        this.grassCoverage = grassCoverage;
    }

    /**
     * Convert statistics to CSV format
     */
    public String toCSV() {
        return String.format("%d,%d,%d,%.2f,%.2f,%.4f",
                tick, preyCount, predatorCount, avgPreyEnergy, avgPredatorEnergy, grassCoverage);
    }

    /**
     * Get CSV header
     */
    public static String getCSVHeader() {
        return "Tick,PreyCount,PredatorCount,AvgPreyEnergy,AvgPredatorEnergy,GrassCoverage";
    }

    // Getters
    public int getTick() {
        return tick;
    }

    public int getPreyCount() {
        return preyCount;
    }

    public int getPredatorCount() {
        return predatorCount;
    }

    public double getAvgPreyEnergy() {
        return avgPreyEnergy;
    }

    public double getAvgPredatorEnergy() {
        return avgPredatorEnergy;
    }

    public double getGrassCoverage() {
        return grassCoverage;
    }
}