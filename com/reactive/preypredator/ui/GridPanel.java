package com.reactive.preypredator.ui;

import com.reactive.preypredator.agents.PreyAgent;
import com.reactive.preypredator.agents.PredatorAgent;
import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.model.Cell;
import com.reactive.preypredator.model.CellType;
import com.reactive.preypredator.model.Grid;
import com.reactive.preypredator.model.Position;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for rendering the 2D grid with agents, grass, and obstacles
 */
public class GridPanel extends JPanel {
    private ReactiveEnvironment environment;

    public GridPanel(ReactiveEnvironment environment) {
        this.environment = environment;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Grid grid = environment.getGrid();
        if (grid == null) return;

        int cellSize = Config.CELL_SIZE;

        // Draw grid cells (grass, obstacles, empty)
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                if (cell == null) continue;

                Color color = Color.WHITE;

                // FIXED: Check cell type properly
                if (cell.getType() == CellType.OBSTACLE) {
                    color = Color.GRAY;
                } else if (cell.hasGrass()) {
                    // Green for grass
                    color = new Color(50, 180, 50);
                } else {
                    // Light brown/tan for eaten grass (dirt)
                    color = new Color(210, 180, 140);
                }

                g2d.setColor(color);
                g2d.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                // Grid lines
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }

        // Draw prey agents
        for (PreyAgent prey : environment.getPreyAgents()) {
            if (!prey.isAlive()) continue;
            Position pos = prey.getPosition();
            int centerX = pos.x * cellSize + cellSize / 2;
            int centerY = pos.y * cellSize + cellSize / 2;
            int radius = cellSize / 3;

            // Blue circle for prey
            g2d.setColor(new Color(30, 144, 255));  // Dodger blue
            g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

            // Draw energy bar
            drawEnergyBar(g2d, pos, prey.getEnergy(), Config.PREY_ENERGY_MAX, cellSize);
        }

        // Draw predator agents
        for (PredatorAgent predator : environment.getPredatorAgents()) {
            if (!predator.isAlive()) continue;
            Position pos = predator.getPosition();
            int centerX = pos.x * cellSize + cellSize / 2;
            int centerY = pos.y * cellSize + cellSize / 2;
            int radius = cellSize / 3;

            // Red circle for predator
            g2d.setColor(new Color(220, 20, 60));  // Crimson
            g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

            // Draw energy bar
            drawEnergyBar(g2d, pos, predator.getEnergy(), Config.PREDATOR_ENERGY_MAX, cellSize);
        }
    }

    /**
     * Draw energy bar above agent
     */
    private void drawEnergyBar(Graphics2D g2d, Position pos, int energy, int maxEnergy, int cellSize) {
        int barWidth = cellSize - 4;
        int barHeight = 3;
        int energyWidth = (int) ((energy / (double) maxEnergy) * barWidth);
        energyWidth = Math.max(0, Math.min(barWidth, energyWidth));  // Clamp value

        // Background (black border)
        g2d.setColor(Color.BLACK);
        g2d.drawRect(pos.x * cellSize + 2, pos.y * cellSize + 2, barWidth, barHeight);

        // Energy bar (colored based on energy level)
        if (energy > maxEnergy * 0.5) {
            g2d.setColor(new Color(0, 200, 0));  // Green
        } else if (energy > maxEnergy * 0.25) {
            g2d.setColor(new Color(255, 200, 0));  // Yellow/Orange
        } else {
            g2d.setColor(new Color(255, 0, 0));  // Red
        }

        if (energyWidth > 0) {
            g2d.fillRect(pos.x * cellSize + 2, pos.y * cellSize + 2, energyWidth, barHeight);
        }
    }
}