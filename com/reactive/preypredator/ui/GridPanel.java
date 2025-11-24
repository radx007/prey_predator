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
import java.awt.image.BufferedImage;
import java.util.Collection;

/**
 * Optimized and modern grid panel
 */
public class GridPanel extends JPanel {
    private ReactiveEnvironment environment;
    private BufferedImage gridBuffer;
    private Graphics2D bufferGraphics;

    public GridPanel(ReactiveEnvironment environment) {
        this.environment = environment;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        int width = Config.GRID_WIDTH * Config.CELL_SIZE;
        int height = Config.GRID_HEIGHT * Config.CELL_SIZE;

        // Create double buffer
        initializeBuffer(width, height);
    }

    private void initializeBuffer(int width, int height) {
        if (gridBuffer != null && bufferGraphics != null) {
            bufferGraphics.dispose();
        }
        gridBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bufferGraphics = gridBuffer.createGraphics();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bufferGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Grid grid = environment.getGrid();
        if (grid == null) return;

        int cellSize = Config.CELL_SIZE;

        // Clear buffer with white
        bufferGraphics.setColor(Color.WHITE);
        bufferGraphics.fillRect(0, 0, gridBuffer.getWidth(), gridBuffer.getHeight());

        // Draw grid cells with smooth colors
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Cell cell = grid.getCell(x, y);
                if (cell == null) continue;

                Color color;
                if (cell.getType() == CellType.OBSTACLE) {
                    color = new Color(100, 100, 100);
                } else if (cell.hasGrass()) {
                    color = new Color(76, 175, 80); // Material green
                } else {
                    color = new Color(245, 245, 240); // Soft beige
                }

                bufferGraphics.setColor(color);
                bufferGraphics.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                // Subtle grid lines
                bufferGraphics.setColor(new Color(230, 230, 230, 100));
                bufferGraphics.drawRect(x * cellSize, y * cellSize, cellSize - 1, cellSize - 1);
            }
        }

        // Draw prey agents with shadow effect
        Collection<PreyAgent> preyAgents = environment.getPreyAgents();
        for (PreyAgent prey : preyAgents) {
            if (!prey.isAlive()) continue;

            Position pos = prey.getPosition();
            if (pos == null) continue;

            int centerX = pos.x * cellSize + cellSize / 2;
            int centerY = pos.y * cellSize + cellSize / 2;
            int radius = cellSize / 3;

            // Shadow
            bufferGraphics.setColor(new Color(0, 0, 0, 30));
            bufferGraphics.fillOval(centerX - radius + 1, centerY - radius + 2, radius * 2, radius * 2);

            // Agent body
            bufferGraphics.setColor(new Color(33, 150, 243)); // Material blue
            bufferGraphics.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

            // Glossy effect
            bufferGraphics.setColor(new Color(255, 255, 255, 80));
            bufferGraphics.fillOval(centerX - radius / 2, centerY - radius / 2, radius, radius / 2);

            // Energy bar
            drawModernEnergyBar(bufferGraphics, pos, prey.getEnergy(), Config.PREY_ENERGY_MAX, cellSize);
        }

        // Draw predator agents with shadow effect
        Collection<PredatorAgent> predatorAgents = environment.getPredatorAgents();
        for (PredatorAgent predator : predatorAgents) {
            if (!predator.isAlive()) continue;

            Position pos = predator.getPosition();
            if (pos == null) continue;

            int centerX = pos.x * cellSize + cellSize / 2;
            int centerY = pos.y * cellSize + cellSize / 2;
            int radius = cellSize / 3;

            // Shadow
            bufferGraphics.setColor(new Color(0, 0, 0, 40));
            bufferGraphics.fillOval(centerX - radius + 1, centerY - radius + 2, radius * 2, radius * 2);

            // Agent body
            bufferGraphics.setColor(new Color(244, 67, 54)); // Material red
            bufferGraphics.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

            // Glossy effect
            bufferGraphics.setColor(new Color(255, 255, 255, 80));
            bufferGraphics.fillOval(centerX - radius / 2, centerY - radius / 2, radius, radius / 2);

            // Energy bar
            drawModernEnergyBar(bufferGraphics, pos, predator.getEnergy(), Config.PREDATOR_ENERGY_MAX, cellSize);
        }

        // Draw the buffer to screen
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(gridBuffer, 0, 0, null);
    }

    private void drawModernEnergyBar(Graphics2D g2d, Position pos, int energy, int maxEnergy, int cellSize) {
        int barWidth = cellSize - 4;
        int barHeight = 4;
        int energyWidth = (int) ((energy / (double) maxEnergy) * barWidth);
        energyWidth = Math.max(0, Math.min(barWidth, energyWidth));

        int barX = pos.x * cellSize + 2;
        int barY = pos.y * cellSize + cellSize - 6;

        // Background
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 2, 2);

        // Energy fill with gradient
        if (energyWidth > 0) {
            Color energyColor;
            if (energy > maxEnergy * 0.6) {
                energyColor = new Color(76, 175, 80); // Green
            } else if (energy > maxEnergy * 0.3) {
                energyColor = new Color(255, 193, 7); // Amber
            } else {
                energyColor = new Color(244, 67, 54); // Red
            }

            g2d.setColor(energyColor);
            g2d.fillRoundRect(barX, barY, energyWidth, barHeight, 2, 2);
        }
    }

    // Cleanup method (replaces deprecated finalize)
    public void cleanup() {
        if (bufferGraphics != null) {
            bufferGraphics.dispose();
            bufferGraphics = null;
        }
        if (gridBuffer != null) {
            gridBuffer.flush();
            gridBuffer = null;
        }
    }
}
