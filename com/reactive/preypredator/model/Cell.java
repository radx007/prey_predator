package com.reactive.preypredator.model;

import com.reactive.preypredator.config.DynamicConfig;

/**
 * Represents a single cell in the grid with grass and dynamic regrowth mechanics
 */
public class Cell {
    private CellType type;
    private boolean hasGrass;
    private int grassRegrowthTimer;

    public Cell(CellType type) {
        this.type = type;
        this.hasGrass = (type == CellType.GRASS);
        this.grassRegrowthTimer = 0;
    }

    /**
     * Check if this cell has grass available for eating
     */
    public boolean hasGrass() {
        return hasGrass && type != CellType.OBSTACLE;
    }

    /**
     * Consume grass from this cell (✨ Uses DYNAMIC regrowth time)
     */
    public void eatGrass() {
        if (hasGrass) {
            hasGrass = false;
            grassRegrowthTimer = DynamicConfig.grassRegrowthTime; // ✨ DYNAMIC
        }
    }

    /**
     * Update grass regrowth timer
     */
    public void updateGrassRegrowth() {
        if (!hasGrass && type != CellType.OBSTACLE) {
            if (grassRegrowthTimer > 0) {
                grassRegrowthTimer--;
            } else {
                hasGrass = true;
            }
        }
    }

    /**
     * Check if this cell is walkable by agents
     */
    public boolean isWalkable() {
        return type != CellType.OBSTACLE;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public int getGrassRegrowthTimer() {
        return grassRegrowthTimer;
    }
}