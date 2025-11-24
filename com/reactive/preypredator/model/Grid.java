package com.reactive.preypredator.model;

import com.reactive.preypredator.config.Config;
import java.util.*;

/**
 * Grid environment for the simulation
 */
public class Grid {
    private final int width;
    private final int height;
    private final Cell[][] cells;
    private final Map<String, Position> agentPositions;
    private final Random random;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];
        this.agentPositions = new HashMap<>();
        this.random = new Random();
        initializeGrid();
    }

    private void initializeGrid() {
        // Initialize all cells as empty
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(CellType.EMPTY);
            }
        }

        // Add obstacles (5% of grid)
        int totalCells = width * height;
        int obstacleCells = (int) (totalCells * Config.OBSTACLE_COVERAGE);
        for (int i = 0; i < obstacleCells; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            cells[x][y] = new Cell(CellType.OBSTACLE);
        }

        // Add initial grass coverage (75% of non-obstacle cells)
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cells[x][y].getType() != CellType.OBSTACLE) {
                    // FIXED: Grass should be present when random < coverage
                    if (random.nextDouble() < Config.GRASS_INITIAL_COVERAGE) {
                        cells[x][y] = new Cell(CellType.GRASS);
                    }
                }
            }
        }
    }

    public Cell getCell(int x, int y) {
        if (isWithinBounds(x, y)) {
            return cells[x][y];
        }
        return null;
    }

    public boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public Position getAgentPosition(String agentId) {
        return agentPositions.get(agentId);
    }

    public void setAgentPosition(String agentId, Position position) {
        agentPositions.put(agentId, position);
    }

    public void removeAgent(String agentId) {
        agentPositions.remove(agentId);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void updateGrassRegrowth() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y].updateGrassRegrowth(Config.GRASS_REGROWTH_TICKS);
            }
        }
    }

    /**
     * Get random empty walkable position
     */
    public Position getRandomEmptyPosition() {
        int attempts = 0;
        while (attempts < 100) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Position pos = new Position(x, y);

            if (cells[x][y].isWalkable() && !agentPositions.containsValue(pos)) {
                return pos;
            }
            attempts++;
        }
        return null;
    }

    /**
     * Calculate grass coverage percentage
     */
    public double getGrassCoverage() {
        int grassCount = 0;
        int walkableCells = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cells[x][y].isWalkable()) {
                    walkableCells++;
                    if (cells[x][y].hasGrass()) {
                        grassCount++;
                    }
                }
            }
        }

        return walkableCells > 0 ? (double) grassCount / walkableCells : 0.0;
    }
}
