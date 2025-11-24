package com.reactive.preypredator.ui;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.statistics.Statistics;

import javax.swing.*;
import java.awt.*;

/**
 * Modern UI with optimized layout
 */
public class SimulationUI extends JFrame {
    private ReactiveEnvironment environment;
    private GridPanel gridPanel;
    private PopulationCurvePanel curvePanel;
    private ConfigurationPanel configPanel;
    private JLabel statsLabel;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton restartButton;
    private JToggleButton configToggleButton;
    private boolean paused = false;
    private boolean stopped = false;
    private Runnable restartCallback;

    public SimulationUI(ReactiveEnvironment environment, Runnable restartCallback) {
        this.environment = environment;
        this.restartCallback = restartCallback;

        setTitle("Predator-Prey Simulation - Lotka-Volterra Dynamics");
        setSize(1600, 900);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleExit();
            }
        });
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(250, 250, 250));

        // Main content area
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        // Control panel at bottom
        JPanel controlPanel = createModernControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left side: Large grid panel
        gridPanel = new GridPanel(environment);
        gridPanel.setPreferredSize(new Dimension(Config.GRID_WIDTH * Config.CELL_SIZE,
                Config.GRID_HEIGHT * Config.CELL_SIZE));

        // Right side: Curve and config
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(new Color(250, 250, 250));
        rightPanel.setPreferredSize(new Dimension(400, 800));

        // Population curve
        curvePanel = new PopulationCurvePanel(environment);
        curvePanel.setPreferredSize(new Dimension(400, 400));

        // Configuration panel (initially hidden)
        configPanel = new ConfigurationPanel(this::handleConfigRestart);
        configPanel.setVisible(false);

        rightPanel.add(curvePanel, BorderLayout.CENTER);
        rightPanel.add(configPanel, BorderLayout.SOUTH);

        panel.add(gridPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createModernControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(224, 224, 224)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        // Stats panel
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(Color.WHITE);

        statsLabel = new JLabel("Initializing...");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLabel.setForeground(new Color(60, 60, 60));
        statsPanel.add(statsLabel, BorderLayout.WEST);

        panel.add(statsPanel, BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttonPanel.setBackground(Color.WHITE);

        pauseButton = createModernButton("Pause", new Color(255, 193, 7), Color.BLACK);
        pauseButton.addActionListener(e -> togglePause());

        stopButton = createModernButton("Stop", new Color(244, 67, 54), Color.WHITE);
        stopButton.addActionListener(e -> stopSimulation());

        restartButton = createModernButton("Restart", new Color(156, 39, 176), Color.WHITE);
        restartButton.addActionListener(e -> handleRestart());

        configToggleButton = new JToggleButton("Configuration");
        styleModernButton(configToggleButton, new Color(63, 81, 181), Color.WHITE);
        configToggleButton.addActionListener(e -> toggleConfigPanel());

        JButton exitButton = createModernButton("Exit", new Color(96, 96, 96), Color.WHITE);
        exitButton.addActionListener(e -> handleExit());

        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(configToggleButton);
        buttonPanel.add(exitButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        // Legend
        JPanel legendPanel = createModernLegend();
        panel.add(legendPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        styleModernButton(button, bg, fg);
        return button;
    }

    private void styleModernButton(AbstractButton button, Color bg, Color fg) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(120, 36));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
    }

    private JPanel createModernLegend() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        panel.setBackground(Color.WHITE);

        panel.add(createLegendItem(new Color(76, 175, 80), "Grass"));
        panel.add(createLegendItem(new Color(33, 150, 243), "Prey"));
        panel.add(createLegendItem(new Color(244, 67, 54), "Predator"));
        panel.add(createLegendItem(new Color(100, 100, 100), "Obstacle"));

        return panel;
    }

    private JPanel createLegendItem(Color color, String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setBackground(Color.WHITE);

        JPanel colorBox = new JPanel();
        colorBox.setBackground(color);
        colorBox.setPreferredSize(new Dimension(16, 16));
        colorBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(new Color(80, 80, 80));

        panel.add(colorBox);
        panel.add(label);
        return panel;
    }

    private void togglePause() {
        paused = !paused;
        if (paused) {
            pauseButton.setText("Resume");
            pauseButton.setBackground(new Color(76, 175, 80));
            pauseButton.setForeground(Color.WHITE);
        } else {
            pauseButton.setText("Pause");
            pauseButton.setBackground(new Color(255, 193, 7));
            pauseButton.setForeground(Color.BLACK);
        }
    }

    private void stopSimulation() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Stop the current simulation?\nYou can restart it afterwards.",
                "Stop Simulation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            stopped = true;
            environment.getDataLogger().printSummary();
        }
    }

    private void handleRestart() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Restart simulation from the beginning?\nCurrent progress will be lost.",
                "Restart Simulation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            stopped = true;
            if (restartCallback != null) {
                SwingUtilities.invokeLater(() -> {
                    gridPanel.cleanup(); // Clean up before shutdown
                    environment.shutdown();
                    restartCallback.run();
                });
            }
        }
    }

    private void handleConfigRestart() {
        stopped = true;
        if (restartCallback != null) {
            SwingUtilities.invokeLater(() -> {
                gridPanel.cleanup();
                environment.shutdown();
                restartCallback.run();
            });
        }
    }

    private void toggleConfigPanel() {
        boolean visible = configToggleButton.isSelected();
        configPanel.setVisible(visible);

        if (visible) {
            configPanel.setPreferredSize(new Dimension(400, 400));
            configToggleButton.setBackground(new Color(255, 87, 34));
        } else {
            configToggleButton.setBackground(new Color(63, 81, 181));
        }

        revalidate();
        repaint();
    }

    private void handleExit() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Exit Simulation",
                JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            gridPanel.cleanup();
            environment.shutdown();
            System.exit(0);
        }
    }

    public void updateDisplay() {
        updateStats();
        gridPanel.repaint();
        curvePanel.repaint();
    }

    private void updateStats() {
        Statistics latest = environment.getDataLogger().getLatest();
        if (latest != null) {
            String stats = String.format(
                    "Tick: %d  |  Prey: %d (Energy: %.1f)  |  Predators: %d (Energy: %.1f)  |  Grass: %.1f%%",
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

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public void showExtinctionDialog(String message) {
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showOptionDialog(
                    this,
                    message + "\n\nWhat would you like to do?",
                    "Simulation Ended",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new String[]{"Restart", "New Config", "Exit"},
                    "Restart"
            );

            if (result == 0) {
                handleRestart();
            } else if (result == 1) {
                stopped = true;
                gridPanel.cleanup();
                environment.shutdown();
                dispose();
            } else {
                handleExit();
            }
        });
    }
}
