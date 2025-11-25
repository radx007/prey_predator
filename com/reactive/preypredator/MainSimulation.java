package com.reactive.preypredator;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.environment.ReactiveEnvironment;
import com.reactive.preypredator.ui.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main application with startup menu and card layout
 */
public class MainSimulation extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private StartupMenu startupMenu;
    private ConfigEditor configEditor;
    private JPanel executionPanel;

    private GridPanel gridPanel;
    private PopulationCurvePanel curvePanel;
    private JPanel controlPanel;

    private ReactiveEnvironment environment;
    private Thread simulationThread;
    private volatile boolean running = false;
    private volatile boolean paused = false;

    public MainSimulation() {
        setTitle("Lotka-Volterra Multi-Agent Simulation");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        setupPanels();
        add(mainPanel);

        cardLayout.show(mainPanel, "STARTUP");
    }

    private void setupPanels() {
        // 1. Startup Menu
        startupMenu = new StartupMenu(
                this::startWithDefaultConfig,
                this::showConfigEditor
        );
        mainPanel.add(startupMenu, "STARTUP");

        // 2. Config Editor
        configEditor = new ConfigEditor(
                this::startSimulation,
                this::showStartupMenu
        );
        mainPanel.add(configEditor, "CONFIG");

        // 3. Execution Panel
        executionPanel = createExecutionPanel();
        mainPanel.add(executionPanel, "EXECUTION");
    }

    private JPanel createExecutionPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Main content: Grid (left, 60%) + Curve (right, 40%)
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Grid Panel (left, large square)
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        gridPanel = new GridPanel(null);
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(BorderFactory.createTitledBorder("Agent Grid"));
        contentPanel.add(gridPanel, gbc);

        // Curve Panel (right)
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        curvePanel = new PopulationCurvePanel(null);
        curvePanel.setBackground(Color.WHITE);
        curvePanel.setBorder(BorderFactory.createTitledBorder("Population Dynamics"));
        contentPanel.add(curvePanel, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);

        // Control Panel (bottom)
        controlPanel = createControlPanel();
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(new Color(245, 245, 247));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton backBtn = createControlButton("← Back", new Color(149, 165, 166));
        backBtn.addActionListener(e -> {
            stopSimulation();
            showStartupMenu();
        });

        JButton pauseBtn = createControlButton("⏸ Pause", new Color(230, 126, 34));
        pauseBtn.addActionListener(e -> {
            paused = !paused;
            pauseBtn.setText(paused ? "▶ Resume" : "⏸ Pause");
        });

        JButton restartBtn = createControlButton("↻ Restart", new Color(231, 76, 60));
        restartBtn.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, "Restart simulation?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                stopSimulation();
                startSimulation();
            }
        });

        panel.add(backBtn);
        panel.add(pauseBtn);
        panel.add(restartBtn);
        return panel;
    }

    private JButton createControlButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showStartupMenu() {
        cardLayout.show(mainPanel, "STARTUP");
    }

    private void showConfigEditor() {
        cardLayout.show(mainPanel, "CONFIG");
    }

    private void startWithDefaultConfig() {
        Config.resetToDefaults();
        startSimulation();
    }

    private void startSimulation() {
        stopSimulation();

        environment = new ReactiveEnvironment();
        gridPanel.setEnvironment(environment);
        curvePanel.setEnvironment(environment);

        running = true;
        paused = false;

        simulationThread = new Thread(() -> {
            while (running ) {
                if (!paused) {
                    environment.tick();
                    SwingUtilities.invokeLater(() -> {
                        gridPanel.repaint();
                        curvePanel.repaint();
                    });
                }
                try {
                    Thread.sleep(Config.TICK_DURATION_MS);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        simulationThread.start();

        cardLayout.show(mainPanel, "EXECUTION");
    }

    private void stopSimulation() {
        running = false;
        if (simulationThread != null) {
            simulationThread.interrupt();
            simulationThread = null;
        }
        if (environment != null) {
            environment.shutdown();
            environment = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainSimulation().setVisible(true);
        });
    }
}
