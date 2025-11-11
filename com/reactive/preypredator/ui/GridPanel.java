package com.reactive.preypredator.ui;

import com.reactive.preypredator.agents.PreyAgent;
import com.reactive.preypredator.agents.PredatorAgent;
import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.model.Cell;
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

                switch (cell.getType()) {
                    case GRASS:
                        color = cell.hasGrass() ? new Color(34, 139, 34) : new Color(200, 230, 200);
                        break;
                    case OBSTACLE:
                        color = Color.GRAY;
                        break;
                    case EMPTY:
                        color = cell.hasGrass() ? new Color(34, 139, 34) : Color.WHITE;
                        break;
                }

                g2d.setColor(color);
                g2d.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

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

            g2d.setColor(Color.BLUE);
            g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            g2d.setColor(Color.BLACK);
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

            g2d.setColor(Color.RED);
            g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            g2d.setColor(Color.BLACK);
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

        g2d.setColor(Color.BLACK);
        g2d.drawRect(pos.x * cellSize + 2, pos.y * cellSize + 2, barWidth, barHeight);

        g2d.setColor(energy > maxEnergy * 0.5 ? Color.GREEN :
                energy > maxEnergy * 0.25 ? Color.YELLOW : Color.RED);
        g2d.fillRect(pos.x * cellSize + 2, pos.y * cellSize + 2, energyWidth, barHeight);
    }
}