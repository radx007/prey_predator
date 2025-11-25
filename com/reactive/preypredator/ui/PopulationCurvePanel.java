package com.reactive.preypredator.ui;

import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.statistics.LotkaVolterraCalculator;
import com.reactive.preypredator.statistics.Statistics;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Panel displaying population dynamics curves with LV theoretical overlay
 */
public class PopulationCurvePanel extends JPanel {
    private ReactiveEnvironment environment;
    private boolean showLV = true;  // Toggle for LV overlay

    public PopulationCurvePanel(ReactiveEnvironment environment) {
        this.environment = environment;
        setBackground(Color.WHITE);
    }

    public void setEnvironment(ReactiveEnvironment environment) {
        this.environment = environment;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (environment == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        LinkedList<Statistics> history = environment.getDataLogger().getHistory();
        if (history.size() < 2) return;

        int width = getWidth();
        int height = getHeight();
        int margin = 50;
        int graphWidth = width - 2 * margin;
        int graphHeight = height - 2 * margin;

        // Draw axes
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawLine(margin, height - margin, width - margin, height - margin); // X-axis
        g2d.drawLine(margin, margin, margin, height - margin); // Y-axis

        // Find max values for scaling
        int maxPrey = 0;
        int maxPredator = 0;
        for (Statistics stat : history) {
            maxPrey = Math.max(maxPrey, stat.getPreyCount());
            maxPredator = Math.max(maxPredator, stat.getPredatorCount());
        }
        int maxPop = Math.max(maxPrey, maxPredator);
        if (maxPop == 0) maxPop = 100;

        int maxTick = history.getLast().getTick();
        if (maxTick == 0) maxTick = 100;

        // Draw grid lines
        g2d.setColor(new Color(240, 240, 240));
        for (int i = 1; i <= 5; i++) {
            int y = height - margin - (graphHeight * i) / 5;
            g2d.drawLine(margin, y, width - margin, y);
        }

        // Draw EMPIRICAL curves (your actual simulation)
        drawCurve(g2d, history, maxTick, maxPop, graphWidth, graphHeight, margin,
                new Color(46, 204, 113), true);  // Prey - green
        drawCurve(g2d, history, maxTick, maxPop, graphWidth, graphHeight, margin,
                new Color(231, 76, 60), false);  // Predator - red

        // Draw THEORETICAL LV curves if enabled
        if (showLV && history.size() > 10) {
            drawLVOverlay(g2d, history, maxTick, maxPop, graphWidth, graphHeight, margin);
        }

        // Draw labels
        drawLabels(g2d, width, height, margin, maxPop, maxTick);

        // Draw legend
        drawLegend(g2d, margin, margin);
    }

    private void drawCurve(Graphics2D g2d, LinkedList<Statistics> history,
                           int maxTick, int maxPop, int graphWidth, int graphHeight,
                           int margin, Color color, boolean isPrey) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2.5f));

        for (int i = 0; i < history.size() - 1; i++) {
            Statistics current = history.get(i);
            Statistics next = history.get(i + 1);

            int count1 = isPrey ? current.getPreyCount() : current.getPredatorCount();
            int count2 = isPrey ? next.getPreyCount() : next.getPredatorCount();

            int x1 = margin + (graphWidth * current.getTick()) / maxTick;
            int y1 = getHeight() - margin - (graphHeight * count1) / maxPop;
            int x2 = margin + (graphWidth * next.getTick()) / maxTick;
            int y2 = getHeight() - margin - (graphHeight * count2) / maxPop;

            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.setStroke(new BasicStroke(1.0f));
    }

    private void drawLVOverlay(Graphics2D g2d, LinkedList<Statistics> history,
                               int maxTick, int maxPop, int graphWidth, int graphHeight, int margin) {
        // Convert history to array for parameter estimation
        double[][] data = new double[history.size()][3];
        for (int i = 0; i < history.size(); i++) {
            Statistics stat = history.get(i);
            data[i][0] = stat.getTick();
            data[i][1] = stat.getPreyCount();
            data[i][2] = stat.getPredatorCount();
        }

        // Estimate LV parameters
        double[] params = LotkaVolterraCalculator.estimateParameters(data);
        double alpha = params[0], beta = params[1], gamma = params[2], delta = params[3];

        // Simulate LV
        LotkaVolterraCalculator lv = new LotkaVolterraCalculator(alpha, beta, gamma, delta);
        double x0 = history.getFirst().getPreyCount();
        double y0 = history.getFirst().getPredatorCount();
        double[][] lvData = lv.simulate(x0, y0, maxTick);

        // Draw LV prey (dashed green)
        g2d.setColor(new Color(46, 204, 113, 150));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10.0f, new float[]{10.0f}, 0.0f));
        for (int i = 0; i < lvData.length - 1; i++) {
            int x1 = margin + (graphWidth * i) / maxTick;
            int y1 = getHeight() - margin - (int)((graphHeight * lvData[i][0]) / maxPop);
            int x2 = margin + (graphWidth * (i + 1)) / maxTick;
            int y2 = getHeight() - margin - (int)((graphHeight * lvData[i + 1][0]) / maxPop);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw LV predator (dashed red)
        g2d.setColor(new Color(231, 76, 60, 150));
        for (int i = 0; i < lvData.length - 1; i++) {
            int x1 = margin + (graphWidth * i) / maxTick;
            int y1 = getHeight() - margin - (int)((graphHeight * lvData[i][1]) / maxPop);
            int x2 = margin + (graphWidth * (i + 1)) / maxTick;
            int y2 = getHeight() - margin - (int)((graphHeight * lvData[i + 1][1]) / maxPop);
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.setStroke(new BasicStroke(1.0f));
    }

    private void drawLabels(Graphics2D g2d, int width, int height, int margin, int maxPop, int maxTick) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));

        // Y-axis labels
        for (int i = 0; i <= 5; i++) {
            int y = height - margin - (height - 2 * margin) * i / 5;
            int value = (maxPop * i) / 5;
            g2d.drawString(String.valueOf(value), margin - 35, y + 5);
        }

        // X-axis labels
        for (int i = 0; i <= 5; i++) {
            int x = margin + (width - 2 * margin) * i / 5;
            int value = (maxTick * i) / 5;
            g2d.drawString(String.valueOf(value), x - 10, height - margin + 20);
        }

        // Axis titles
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Tick", width / 2 - 15, height - 10);

        // Rotate for Y-axis label
        g2d.rotate(-Math.PI / 2);
        g2d.drawString("Population", -height / 2 - 30, 15);
        g2d.rotate(Math.PI / 2);
    }

    private void drawLegend(Graphics2D g2d, int x, int y) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));

        // Empirical Prey
        g2d.setColor(new Color(46, 204, 113));
        g2d.fillRect(x, y, 15, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Prey (Empirical)", x + 20, y + 10);

        // Empirical Predator
        g2d.setColor(new Color(231, 76, 60));
        g2d.fillRect(x, y + 20, 15, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Predator (Empirical)", x + 20, y + 30);

        if (showLV) {
            // LV Prey (dashed)
            g2d.setColor(new Color(46, 204, 113, 150));
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10.0f, new float[]{5.0f}, 0.0f));
            g2d.drawLine(x, y + 42, x + 15, y + 42);
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Prey (LV Theory)", x + 20, y + 50);

            // LV Predator (dashed)
            g2d.setColor(new Color(231, 76, 60, 150));
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10.0f, new float[]{5.0f}, 0.0f));
            g2d.drawLine(x, y + 62, x + 15, y + 62);
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Predator (LV Theory)", x + 20, y + 70);
        }
    }
}
