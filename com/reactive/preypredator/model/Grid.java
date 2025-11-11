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
        // Initialize all cells as empty with grass
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(CellType.EMPTY);
            }
        }

        // Add initial grass coverage
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (random.nextDouble() > Config.GRASS_INITIAL_COVERAGE) {
                    cells[x][y].eatGrass();
                }
            }
        }

        // Add obstacles
        for (int i = 0; i < Config.OBSTACLE_COUNT; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            cells[x][y] = new Cell(CellType.OBSTACLE);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell getCell(int x, int y) {
        if (isValidPosition(x, y)) {
            return cells[x][y];
        }
        return null;
    }

    public Cell getCell(Position pos) {
        return getCell(pos.x, pos.y);
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isValidPosition(Position pos) {
        return isValidPosition(pos.x, pos.y);
    }

    public boolean isWalkable(Position pos) {
        Cell cell = getCell(pos);
        return cell != null && cell.isWalkable();
    }

    public synchronized Position getAgentPosition(String agentName) {
        return agentPositions.get(agentName);
    }

    public synchronized void setAgentPosition(String agentName, Position pos) {
        agentPositions.put(agentName, pos);
    }

    public synchronized void removeAgent(String agentName) {
        agentPositions.remove(agentName);
    }

    public synchronized List<String> getAgentsInRange(Position center, int range) {
        List<String> nearby = new ArrayList<>();
        for (Map.Entry<String, Position> entry : agentPositions.entrySet()) {
            if (entry.getValue().manhattanDistance(center) <= range) {
                nearby.add(entry.getKey());
            }
        }
        return nearby;
    }

    public synchronized Map<String, Position> getAllAgentPositions() {
        return new HashMap<>(agentPositions);
    }

    public void updateGrassRegrowth() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y].updateGrassRegrowth(Config.GRASS_REGROWTH_TIME);
            }
        }
    }

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

    public double getGrassCoverage() {
        int totalWalkable = 0;
        int grassCount = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cells[x][y].isWalkable()) {
                    totalWalkable++;
                    if (cells[x][y].hasGrass()) {
                        grassCount++;
                    }
                }
            }
        }
        return totalWalkable > 0 ? (double) grassCount / totalWalkable : 0.0;
    }

    public Position getRandomWalkablePosition() {
        int attempts = 0;
        while (attempts < 1000) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Position pos = new Position(x, y);
            if (isWalkable(pos)) {
                return pos;
            }
            attempts++;
        }
        return new Position(width / 2, height / 2);
    }
}