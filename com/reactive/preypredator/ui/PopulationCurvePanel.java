package com.reactive.preypredator.ui;

import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.statistics.LotkaVolterraCalculator;
import com.reactive.preypredator.statistics.Statistics;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

/**
 * Panel displaying TWO separate graphs with MATCHING scale to mock simulation
 */
public class PopulationCurvePanel extends JPanel {
    private ReactiveEnvironment environment;
    private boolean showLV = true;

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
        int graphHeight = (height - 30) / 2;
        int margin = 50;
        int maxPop = 300; // SAME as mock simulation

        int maxTick = history.getLast().getTick();
        if (maxTick == 0) maxTick = 100;

        // === GRAPH 1: EMPIRICAL DATA (TOP) ===
        drawGraphBackground(g2d, width, graphHeight, margin, 0, "Empirical Data (Simulation)");
        drawEmpiricalCurves(g2d, history, maxTick, maxPop, width, graphHeight, margin, 0);
        drawLabels(g2d, width, graphHeight, margin, maxPop, maxTick, 0, true);
        drawLegend(g2d, margin, 10, false);

        // === GRAPH 2: THEORETICAL LV (BOTTOM) ===
        if (showLV && history.size() > 10) {
            int yOffset = graphHeight + 15;
            drawGraphBackground(g2d, width, graphHeight, margin, yOffset, "Lotka-Volterra Theory");
            drawLVCurves(g2d, history, maxTick, maxPop, width, graphHeight, margin, yOffset);
            drawLabels(g2d, width, graphHeight, margin, maxPop, maxTick, yOffset, false);
            drawLegend(g2d, margin, yOffset + 10, true);
        }
    }

    private void drawGraphBackground(Graphics2D g2d, int width, int height, int margin, int yOffset, String title) {
        // Title
        g2d.setColor(new Color(52, 73, 94));
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(title, margin, yOffset + 15);

        // Axes
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawLine(margin, yOffset + height - margin, width - margin, yOffset + height - margin);
        g2d.drawLine(margin, yOffset + margin + 20, margin, yOffset + height - margin);

        // Grid lines
        g2d.setColor(new Color(240, 240, 240));
        int gridHeight = height - 2 * margin - 20;
        for (int i = 1; i <= 5; i++) {
            int y = yOffset + height - margin - (gridHeight * i) / 5;
            g2d.drawLine(margin, y, width - margin, y);
        }
    }

    private void drawEmpiricalCurves(Graphics2D g2d, LinkedList<Statistics> history, int maxTick, int maxPop,
                                     int width, int height, int margin, int yOffset) {
        int graphWidth = width - 2 * margin;
        int graphHeight = height - 2 * margin - 20;

        // Draw prey curve
        g2d.setColor(new Color(46, 204, 113));
        g2d.setStroke(new BasicStroke(2.5f));

        for (int i = 0; i < history.size() - 1; i++) {
            Statistics current = history.get(i);
            Statistics next = history.get(i + 1);

            int x1 = margin + (graphWidth * current.getTick()) / maxTick;
            int y1 = yOffset + height - margin - (graphHeight * current.getPreyCount()) / maxPop;
            int x2 = margin + (graphWidth * next.getTick()) / maxTick;
            int y2 = yOffset + height - margin - (graphHeight * next.getPreyCount()) / maxPop;

            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw predator curve
        g2d.setColor(new Color(231, 76, 60));

        for (int i = 0; i < history.size() - 1; i++) {
            Statistics current = history.get(i);
            Statistics next = history.get(i + 1);

            int x1 = margin + (graphWidth * current.getTick()) / maxTick;
            int y1 = yOffset + height - margin - (graphHeight * current.getPredatorCount()) / maxPop;
            int x2 = margin + (graphWidth * next.getTick()) / maxTick;
            int y2 = yOffset + height - margin - (graphHeight * next.getPredatorCount()) / maxPop;

            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.setStroke(new BasicStroke(1.0f));
    }

    private void drawLVCurves(Graphics2D g2d, LinkedList<Statistics> history, int maxTick, int maxPop,
                              int width, int height, int margin, int yOffset) {
        int graphWidth = width - 2 * margin;
        int graphHeight = height - 2 * margin - 20;

        // Convert history to array
        double[][] data = new double[history.size()][3];
        for (int i = 0; i < history.size(); i++) {
            Statistics stat = history.get(i);
            data[i][0] = stat.getTick();
            data[i][1] = stat.getPreyCount();
            data[i][2] = stat.getPredatorCount();
        }

        // Estimate parameters and simulate
        double[] params = LotkaVolterraCalculator.estimateParameters(data);
        LotkaVolterraCalculator lv = new LotkaVolterraCalculator(params[0], params[1], params[2], params[3]);
        double x0 = history.getFirst().getPreyCount();
        double y0 = history.getFirst().getPredatorCount();
        double[][] lvData = lv.simulate(x0, y0, maxTick);

        // Draw LV prey
        g2d.setColor(new Color(46, 204, 113));
        g2d.setStroke(new BasicStroke(2.5f));

        for (int i = 0; i < lvData.length - 1; i++) {
            int x1 = margin + (graphWidth * i) / maxTick;
            int y1 = yOffset + height - margin - (int)((graphHeight * lvData[i][0]) / maxPop);
            int x2 = margin + (graphWidth * (i + 1)) / maxTick;
            int y2 = yOffset + height - margin - (int)((graphHeight * lvData[i + 1][0]) / maxPop);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw LV predator
        g2d.setColor(new Color(231, 76, 60));

        for (int i = 0; i < lvData.length - 1; i++) {
            int x1 = margin + (graphWidth * i) / maxTick;
            int y1 = yOffset + height - margin - (int)((graphHeight * lvData[i][1]) / maxPop);
            int x2 = margin + (graphWidth * (i + 1)) / maxTick;
            int y2 = yOffset + height - margin - (int)((graphHeight * lvData[i + 1][1]) / maxPop);
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.setStroke(new BasicStroke(1.0f));
    }

    private void drawLabels(Graphics2D g2d, int width, int height, int margin, int maxPop, int maxTick, int yOffset, boolean includeXLabel) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        int graphHeight = height - 2 * margin - 20;

        // Y-axis labels (0 to 300, same as mock)
        for (int i = 0; i <= 6; i++) {
            int y = yOffset + height - margin - (graphHeight * i) / 6;
            int value = (maxPop * i) / 6;
            g2d.drawString(String.valueOf(value), margin - 35, y + 5);
        }

        // X-axis labels (10 labels, same as mock)
        int labelCount = 10;
        for (int i = 0; i <= labelCount; i++) {
            int x = margin + (width - 2 * margin) * i / labelCount;
            int tickValue = (maxTick * i) / labelCount;

            g2d.setColor(new Color(180, 180, 180));
            g2d.drawLine(x, yOffset + height - margin, x, yOffset + height - margin + 5);

            g2d.setColor(Color.BLACK);
            String label = String.valueOf(tickValue);
            int labelWidth = g2d.getFontMetrics().stringWidth(label);
            g2d.drawString(label, x - labelWidth / 2, yOffset + height - margin + 18);
        }

        // Y-axis title
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        Graphics2D g2dRot = (Graphics2D) g2d.create();
        g2dRot.translate(15, yOffset + height / 2);
        g2dRot.rotate(-Math.PI / 2);
        g2dRot.drawString("Population", -40, 0);
        g2dRot.dispose();

        // X-axis title (only on bottom graph)
        if (includeXLabel) {
            g2d.drawString("Tick", width / 2 - 15, yOffset + height - 10);
        }
    }

    private void drawLegend(Graphics2D g2d, int x, int y, boolean isTheory) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        g2d.setColor(new Color(46, 204, 113));
        g2d.fillRect(x, y, 15, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawString(isTheory ? "Prey (Theory)" : "Prey", x + 20, y + 10);

        g2d.setColor(new Color(231, 76, 60));
        g2d.fillRect(x, y + 18, 15, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawString(isTheory ? "Predator (Theory)" : "Predator", x + 20, y + 28);
    }
}
