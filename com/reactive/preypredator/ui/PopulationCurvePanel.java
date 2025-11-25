package com.reactive.preypredator.ui;

import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.statistics.Statistics;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Modern population dynamics curve panel
 */
public class PopulationCurvePanel extends JPanel {
    private ReactiveEnvironment environment;

    public PopulationCurvePanel(ReactiveEnvironment environment) {
        this.environment = environment;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        LinkedList<Statistics> history = environment.getDataLogger().getHistory();
        if (history.size() < 2) {
            drawNoDataMessage(g2d);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int margin = 50;
        int graphWidth = width - 2 * margin;
        int graphHeight = height - 2 * margin;

        // Draw title
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.setColor(new Color(60, 60, 60));
        String title = "Population Dynamics (Lotka-Volterra)";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, (width - fm.stringWidth(title)) / 2, 20);

        // Draw axes with modern style
        g2d.setColor(new Color(120, 120, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(margin, height - margin, width - margin, height - margin); // X-axis
        g2d.drawLine(margin, margin, margin, height - margin); // Y-axis

        // Axis labels
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2d.drawString("Time (ticks)", width / 2 - 30, height - 10);

        // Rotate for Y-axis label
        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        g2dCopy.rotate(-Math.PI / 2);
        g2dCopy.drawString("Population", -height / 2 - 30, 15);
        g2dCopy.dispose();

        // Find max population for scaling
        int maxPop = 10;
        for (Statistics stat : history) {
            maxPop = Math.max(maxPop, Math.max(stat.getPreyCount(), stat.getPredatorCount()));
        }
        maxPop = (int) (maxPop * 1.1); // Add 10% padding

        // Draw grid lines
        g2d.setColor(new Color(240, 240, 240));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= 5; i++) {
            int y = margin + (graphHeight * i / 5);
            g2d.drawLine(margin, y, width - margin, y);

            // Y-axis labels
            g2d.setColor(new Color(100, 100, 100));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            int value = maxPop - (maxPop * i / 5);
            g2d.drawString(String.valueOf(value), 10, y + 4);
            g2d.setColor(new Color(240, 240, 240));
        }

        // Draw prey population curve
        g2d.setColor(new Color(33, 150, 243)); // Material blue
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawPopulationCurve(g2d, history, true, maxPop, graphWidth, graphHeight, margin);

        // Draw predator population curve
        g2d.setColor(new Color(244, 67, 54)); // Material red
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        drawPopulationCurve(g2d, history, false, maxPop, graphWidth, graphHeight, margin);

        // Modern legend with rounded boxes
        drawModernLegend(g2d, width, height);
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
            int y = margin + graphHeight - (population * graphHeight / Math.max(maxPop, 1));

            if (prevX != -1) {
                g2d.drawLine(prevX, prevY, x, y);
            }

            prevX = x;
            prevY = y;
        }
    }

    private void drawModernLegend(Graphics2D g2d, int width, int height) {
        int legendX = width - 140;
        int legendY = 40;

        // Legend background
        g2d.setColor(new Color(255, 255, 255, 240));
        g2d.fillRoundRect(legendX - 10, legendY - 5, 130, 60, 10, 10);
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(legendX - 10, legendY - 5, 130, 60, 10, 10);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        // Prey legend
        g2d.setColor(new Color(33, 150, 243));
        g2d.fillRoundRect(legendX, legendY, 25, 8, 4, 4);
        g2d.setColor(new Color(60, 60, 60));
        g2d.drawString("Prey", legendX + 32, legendY + 8);

        // Predator legend
        g2d.setColor(new Color(244, 67, 54));
        g2d.fillRoundRect(legendX, legendY + 20, 25, 8, 4, 4);
        g2d.setColor(new Color(60, 60, 60));
        g2d.drawString("Predators", legendX + 32, legendY + 28);

        // Current values
        Statistics latest = environment.getDataLogger().getLatest();
        if (latest != null) {
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2d.setColor(new Color(33, 150, 243));
            g2d.drawString("" + latest.getPreyCount(), legendX + 95, legendY + 8);
            g2d.setColor(new Color(244, 67, 54));
            g2d.drawString("" + latest.getPredatorCount(), legendX + 95, legendY + 28);
        }
    }

    private void drawNoDataMessage(Graphics2D g2d) {
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2d.setColor(new Color(150, 150, 150));
        String message = "Collecting data...";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(message)) / 2;
        int y = getHeight() / 2;
        g2d.drawString(message, x, y);
    }
    public void setEnvironment(ReactiveEnvironment environment) {
        this.environment = environment;
    }

}
