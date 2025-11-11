package com.reactive.preypredator.ui;

import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.statistics.Statistics;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for rendering Lotka-Volterra population dynamics curves
 */
public class PopulationCurvePanel extends JPanel {
    private static final int PADDING = 50;
    private static final int GRAPH_HEIGHT = 350;

    private ReactiveEnvironment environment;

    public PopulationCurvePanel(ReactiveEnvironment environment) {
        this.environment = environment;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        List<Statistics> history = environment.getDataLogger().getHistory();

        if (history.isEmpty()) {
            g2d.drawString("No data yet...", width / 2 - 50, height / 2);
            return;
        }

        // Draw background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        int graphWidth = width - 2 * PADDING;
        int graphTop = PADDING;

        // Find max population for scaling
        int maxPrey = 0;
        int maxPredator = 0;
        for (Statistics stat : history) {
            maxPrey = Math.max(maxPrey, stat.getPreyCount());
            maxPredator = Math.max(maxPredator, stat.getPredatorCount());
        }
        int maxPop = Math.max(Math.max(maxPrey, maxPredator), 10);

        // Draw title
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Population Over Time", width / 2 - 80, 25);

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(PADDING, graphTop, PADDING, graphTop + GRAPH_HEIGHT); // Y-axis
        g2d.drawLine(PADDING, graphTop + GRAPH_HEIGHT, width - PADDING, graphTop + GRAPH_HEIGHT); // X-axis

        // Draw Y-axis labels and grid lines
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int i = 0; i <= 5; i++) {
            int y = graphTop + GRAPH_HEIGHT - (i * GRAPH_HEIGHT / 5);
            int value = (maxPop * i) / 5;
            g2d.drawString(String.valueOf(value), PADDING - 35, y + 5);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawLine(PADDING, y, width - PADDING, y);
            g2d.setColor(Color.BLACK);
        }

        // Draw X-axis labels
        int tickInterval = Math.max(1, history.size() / 10);
        for (int i = 0; i < history.size(); i += tickInterval) {
            int x = PADDING + (i * graphWidth) / Math.max(1, history.size() - 1);
            g2d.drawString(String.valueOf(history.get(i).getTick()), x - 10, graphTop + GRAPH_HEIGHT + 20);
        }

        // Draw axis labels
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Population", 5, graphTop + GRAPH_HEIGHT / 2);
        g2d.drawString("Time (Ticks)", width / 2 - 40, graphTop + GRAPH_HEIGHT + 40);

        // Draw prey curve (BLUE)
        g2d.setColor(new Color(0, 100, 255));
        g2d.setStroke(new BasicStroke(2.5f));
        for (int i = 1; i < history.size(); i++) {
            int x1 = PADDING + ((i - 1) * graphWidth) / Math.max(1, history.size() - 1);
            int x2 = PADDING + (i * graphWidth) / Math.max(1, history.size() - 1);

            int y1 = graphTop + GRAPH_HEIGHT - (int) ((history.get(i - 1).getPreyCount() * GRAPH_HEIGHT) / (double) maxPop);
            int y2 = graphTop + GRAPH_HEIGHT - (int) ((history.get(i).getPreyCount() * GRAPH_HEIGHT) / (double) maxPop);

            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw predator curve (RED)
        g2d.setColor(new Color(255, 50, 50));
        for (int i = 1; i < history.size(); i++) {
            int x1 = PADDING + ((i - 1) * graphWidth) / Math.max(1, history.size() - 1);
            int x2 = PADDING + (i * graphWidth) / Math.max(1, history.size() - 1);

            int y1 = graphTop + GRAPH_HEIGHT - (int) ((history.get(i - 1).getPredatorCount() * GRAPH_HEIGHT) / (double) maxPop);
            int y2 = graphTop + GRAPH_HEIGHT - (int) ((history.get(i).getPredatorCount() * GRAPH_HEIGHT) / (double) maxPop);

            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw legend
        int legendY = graphTop + GRAPH_HEIGHT + 60;
        g2d.setColor(new Color(0, 100, 255));
        g2d.fillRect(width / 2 - 150, legendY, 20, 10);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.drawString("Prey Population", width / 2 - 125, legendY + 10);

        g2d.setColor(new Color(255, 50, 50));
        g2d.fillRect(width / 2 + 20, legendY, 20, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Predator Population", width / 2 + 45, legendY + 10);

        // Current values
        Statistics latest = history.get(history.size() - 1);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.drawString(String.format("Current: Prey=%d | Predators=%d",
                        latest.getPreyCount(), latest.getPredatorCount()),
                width / 2 - 120, legendY + 30);

        // Note about reactive system
        g2d.setFont(new Font("Arial", Font.ITALIC, 10));
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString("REACTIVE AGENTS - No ACL Communication",
                width / 2 - 140, legendY + 50);
    }
}