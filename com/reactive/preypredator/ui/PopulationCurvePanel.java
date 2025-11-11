package com.reactive.preypredator.ui;

import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.statistics.Statistics;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Panel displaying population dynamics curves (Lotka-Volterra)
 */
public class PopulationCurvePanel extends JPanel {
    private ReactiveEnvironment environment;

    public PopulationCurvePanel(ReactiveEnvironment environment) {
        this.environment = environment;
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        LinkedList<Statistics> history = environment.getDataLogger().getHistory();
        if (history.size() < 2) return;

        int width = getWidth();
        int height = getHeight();
        int margin = 40;
        int graphWidth = width - 2 * margin;
        int graphHeight = height - 2 * margin;

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(margin, height - margin, width - margin, height - margin); // X-axis
        g2d.drawLine(margin, margin, margin, height - margin); // Y-axis

        // Labels
        g2d.drawString("Time (ticks)", width / 2, height - 10);
        g2d.drawString("Population", 5, margin - 10);

        // Find max population for scaling
        int maxPop = 0;
        for (Statistics stat : history) {
            maxPop = Math.max(maxPop, Math.max(stat.getPreyCount(), stat.getPredatorCount()));
        }
        maxPop = Math.max(maxPop, 10); // Minimum scale

        // Draw grid lines
        g2d.setColor(new Color(200, 200, 200));
        for (int i = 0; i <= 5; i++) {
            int y = margin + (graphHeight * i / 5);
            g2d.drawLine(margin, y, width - margin, y);
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(maxPop - (maxPop * i / 5)), 5, y + 5);
            g2d.setColor(new Color(200, 200, 200));
        }

        // Draw prey population (blue)
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2));
        drawPopulationCurve(g2d, history, true, maxPop, graphWidth, graphHeight, margin);

        // Draw predator population (red)
        g2d.setColor(Color.RED);
        drawPopulationCurve(g2d, history, false, maxPop, graphWidth, graphHeight, margin);

        // Legend
        g2d.setColor(Color.BLUE);
        g2d.fillRect(width - 150, 20, 20, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Prey", width - 120, 30);

        g2d.setColor(Color.RED);
        g2d.fillRect(width - 150, 40, 20, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Predators", width - 120, 50);
    }

    private void drawPopulationCurve(Graphics2D g2d, LinkedList<Statistics> history,
                                     boolean isPrey, int maxPop, int graphWidth,
                                     int graphHeight, int margin) {
        int prevX = -1, prevY = -1;
        int dataPoints = history.size();

        for (int i = 0; i < dataPoints; i++) {
            Statistics stat = history.get(i);
            int population = isPrey ? stat.getPreyCount() : stat.getPredatorCount();

            int x = margin + (i * graphWidth / Math.max(dataPoints - 1, 1));
            int y = margin + graphHeight - (population * graphHeight / maxPop);

            if (prevX != -1) {
                g2d.drawLine(prevX, prevY, x, y);
            }

            prevX = x;
            prevY = y;
        }
    }
}