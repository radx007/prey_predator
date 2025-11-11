package com.reactive.preypredator.ui;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.statistics.Statistics;

import javax.swing.*;
import java.awt.*;

/**
 * Main UI window for the reactive predator-prey simulation
 */
public class SimulationUI extends JFrame {
    private ReactiveEnvironment environment;
    private GridPanel gridPanel;
    private PopulationCurvePanel curvePanel;
    private JLabel statsLabel;
    private JButton pauseButton;
    private boolean paused = false;

    public SimulationUI(ReactiveEnvironment environment) {
        this.environment = environment;

        setTitle("REACTIVE Predator-Prey Multi-Agent Simulation");
        setSize(Config.UI_WIDTH + 600, Config.UI_HEIGHT + 39);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel with grid and curves
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Grid panel (left side)
        gridPanel = new GridPanel(environment);
        gridPanel.setPreferredSize(new Dimension(Config.UI_WIDTH, Config.UI_HEIGHT - 150));

        // Population curve panel (right side)
        curvePanel = new PopulationCurvePanel(environment);
        curvePanel.setPreferredSize(new Dimension(580, Config.UI_HEIGHT - 150));
        curvePanel.setBorder(BorderFactory.createTitledBorder("Lotka-Volterra Population Dynamics"));

        // Split pane to hold both
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gridPanel, curvePanel);
        splitPane.setDividerLocation(Config.UI_WIDTH);
        splitPane.setResizeWeight(0.5);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Control panel at bottom
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setPreferredSize(new Dimension(Config.UI_WIDTH + 600, 150));

        // Statistics label
        statsLabel = new JLabel("Tick: 0 | Prey: 0 | Predators: 0");
        statsLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        controlPanel.add(statsLabel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel();
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> togglePause());

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(pauseButton);
        buttonPanel.add(exitButton);
        controlPanel.add(buttonPanel, BorderLayout.CENTER);

        // Legend
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        legendPanel.add(createLegendItem(Color.GREEN, "Grass"));
        legendPanel.add(createLegendItem(Color.BLUE, "Prey"));
        legendPanel.add(createLegendItem(Color.RED, "Predator"));
        legendPanel.add(createLegendItem(Color.GRAY, "Obstacle"));
        controlPanel.add(legendPanel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /**
     * Create a legend item with color box and label
     */
    private JPanel createLegendItem(Color color, String text) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(15, 15));
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        panel.add(colorBox);
        panel.add(new JLabel(text));

        return panel;
    }

    /**
     * Toggle pause/resume
     */
    private void togglePause() {
        paused = !paused;
        pauseButton.setText(paused ? "Resume" : "Pause");
    }

    /**
     * Update all display components
     */
    public void updateDisplay() {
        updateStats();
        gridPanel.repaint();
        curvePanel.repaint();
    }

    /**
     * Update statistics label
     */
    private void updateStats() {
        Statistics latest = environment.getDataLogger().getLatest();
        if (latest != null) {
            String stats = String.format(
                    "Tick: %d | Prey: %d (Avg Energy: %.1f) | Predators: %d (Avg Energy: %.1f) | Grass: %.1f%%",
                    latest.getTick(),
                    latest.getPreyCount(),
                    latest.getAvgPreyEnergy(),
                    latest.getPredatorCount(),
                    latest.getAvgPredatorEnergy(),
                    latest.getGrassCoverage() * 100
            );
            statsLabel.setText(stats);
        }
    }

    public boolean isPaused() {
        return paused;
    }
}