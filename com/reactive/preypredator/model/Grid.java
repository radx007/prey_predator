package com.reactive.preypredator.model;

import com.reactive.preypredator.config.Config;

import java.util.*;

/**
 * Represents the simulation grid/environment with cells and agent positions
 */
public class Grid {
    private int width;
    private int height;
    private Cell[][] cells;
    private Map<String, Position> agentPositions;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];
        this.agentPositions = new HashMap<>();
        initializeGrid();
    }

    /**
     * Initialize the grid with grass, obstacles, and empty cells
     */
    private void initializeGrid() {
        Random rand = new Random();

        // Initialize all cells as empty
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(CellType.EMPTY);
            }
        }

        // Add grass
        int grassCount = (int) (width * height * Config.INITIAL_GRASS_COVERAGE);
        for (int i = 0; i < grassCount; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            cells[x][y] = new Cell(CellType.GRASS);
        }

        // Add obstacles
        int obstacleCount = (int) (width * height * Config.OBSTACLE_COVERAGE);
        for (int i = 0; i < obstacleCount; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            cells[x][y] = new Cell(CellType.OBSTACLE);
        }
    }

    /**
     * Get cell at position
     */
    public Cell getCell(int x, int y) {
        if (isValidPosition(new Position(x, y))) {
            return cells[x][y];
        }
        return null;
    }

    /**
     * Check if position is within grid bounds
     */
    public boolean isValidPosition(Position pos) {
        return pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height;
    }

    /**
     * Check if position is walkable (not an obstacle)
     */
    public boolean isWalkable(Position pos) {
        return isValidPosition(pos) && cells[pos.x][pos.y].isWalkable();
    }

    /**
     * Update grass regrowth for all cells
     */
    public void updateGrassRegrowth() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y].updateGrassRegrowth();
            }
        }
    }

    /**
     * Get agent position by ID
     */
    public Position getAgentPosition(String agentId) {
        return agentPositions.get(agentId);
    }

    /**
     * Set agent position
     */
    public void setAgentPosition(String agentId, Position pos) {
        agentPositions.put(agentId, pos);
    }

    /**
     * Remove agent from grid
     */
    public void removeAgent(String agentId) {
        agentPositions.remove(agentId);
    }

    /**
     * Get all agent positions
     */
    public Map<String, Position> getAgentPositions() {
        return new HashMap<>(agentPositions);
    }

    /**
     * Count total grass cells
     */
    public int countGrassCells() {
        int count = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cells[x][y].hasGrass()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Get grass coverage percentage
     */
    public double getGrassCoverage() {
        return (double) countGrassCells() / (width * height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}