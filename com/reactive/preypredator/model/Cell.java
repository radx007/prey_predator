package com.reactive.preypredator.model;

public class Cell {
    private CellType type;
    private boolean hasGrass;
    private int grassRegrowthTimer;

    public Cell(CellType type) {
        this.type = type;
        this.hasGrass = (type == CellType.GRASS || type == CellType.EMPTY);
        this.grassRegrowthTimer = 0;
    }

    public CellType getType() {
        return type;
    }

    public boolean hasGrass() {
        return hasGrass && type != CellType.OBSTACLE;
    }

    public void eatGrass() {
        if (hasGrass) {
            hasGrass = false;
            grassRegrowthTimer = 0;
        }
    }

    public void updateGrassRegrowth(int regrowthTime) {
        if (!hasGrass && type != CellType.OBSTACLE) {
            grassRegrowthTimer++;
            if (grassRegrowthTimer >= regrowthTime) {
                hasGrass = true;
                grassRegrowthTimer = 0;
            }
        }
    }

    public boolean isWalkable() {
        return type != CellType.OBSTACLE;
    }
}
