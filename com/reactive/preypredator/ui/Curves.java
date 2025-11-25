package com.reactive.preypredator.ui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Mock simulation with tick speed control and proper scaling
 */
public class MockSimulationPanel extends JPanel {
    private static final int GRID_SIZE = 60;
    private static final int CELL_SIZE = 12;  // Smaller cell size for better scale

    private int currentTick = 0;
    private int maxTicks = 1000;
    private javax.swing.Timer timer;
    private int tickDuration = 120;  // Slower default (was 50ms)

    // Population data
    private List<Integer> preyHistory = new ArrayList<>();
    private List<Integer> predatorHistory = new ArrayList<>();
    private List<Integer> preyTheoryHistory = new ArrayList<>();
    private List<Integer> predatorTheoryHistory = new ArrayList<>();

    // Current populations
    private int currentPreyCount = 0;
    private int currentPredatorCount = 0;

    // Agent data
    private List<AgentData> preyAgents = new ArrayList<>();
    private List<AgentData> predatorAgents = new ArrayList<>();

    // Grass grid
    private int[][] grassGrid = new int[GRID_SIZE][GRID_SIZE];
    private static final int GRASS_REGROWTH_TIME = 25;

    private Random random = new Random();

    // UI components
    private JPanel gridPanel;
    private JPanel curvePanel;
    private BufferedImage gridBuffer;
    private Graphics2D bufferGraphics;
    private JLabel statsLabel;
    private JLabel speedLabel;
    private JButton pauseButton;
    private JSlider speedSlider;
    private boolean paused = false;

    private static class AgentData {
        Point position;
        int energy;
        int maxEnergy;

        AgentData(Point pos, int energy, int maxEnergy) {
            this.position = pos;
            this.energy = energy;
            this.maxEnergy = maxEnergy;
        }
    }

    public MockSimulationPanel(Runnable onBack) {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(250, 250, 250));

        initializeGrass();

        // Main content area
        JPanel mainPanel = createMainPanel(onBack);
        add(mainPanel, BorderLayout.CENTER);

        // Bottom control panel with speed control
        JPanel bottomPanel = createModernControlPanel(onBack);
        add(bottomPanel, BorderLayout.SOUTH);

        startSimulation();
    }

    private void initializeGrass() {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                double r = random.nextDouble();
                if (r < 0.68) grassGrid[x][y] = -1;  // Grass
                else if (r < 0.73) grassGrid[x][y] = -2;  // Obstacle
                else grassGrid[x][y] = 0;  // Empty
            }
        }
    }

    private JPanel createMainPanel(Runnable onBack) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left: Grid Panel
        gridPanel = createGridPanel();

        // Right: Curve Panel
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(new Color(250, 250, 250));
        rightPanel.setPreferredSize(new Dimension(500, 800));

        curvePanel = createCurvePanel();
        curvePanel.setPreferredSize(new Dimension(500, 450));

        rightPanel.add(curvePanel, BorderLayout.CENTER);

        panel.add(gridPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createGridPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGridWithBuffer(g);
            }
        };

        panel.setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Initialize buffer
        int width = GRID_SIZE * CELL_SIZE;
        int height = GRID_SIZE * CELL_SIZE;
        gridBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bufferGraphics = gridBuffer.createGraphics();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bufferGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        return panel;
    }

    private JPanel createCurvePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawPopulationCurves(g);
            }
        };
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JPanel createModernControlPanel(Runnable onBack) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(224, 224, 224)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        // Top: Stats + Speed Control
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        // Stats
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(Color.WHITE);

        statsLabel = new JLabel("Initializing...");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLabel.setForeground(new Color(60, 60, 60));
        statsPanel.add(statsLabel, BorderLayout.WEST);

        topPanel.add(statsPanel, BorderLayout.NORTH);

        // Speed Control
        JPanel speedPanel = createSpeedControlPanel();
        topPanel.add(speedPanel, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);

        // Middle: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        buttonPanel.setBackground(Color.WHITE);

        pauseButton = createModernButton("Pause", new Color(255, 193, 7), Color.BLACK);
        pauseButton.addActionListener(e -> togglePause());

        JButton stopButton = createModernButton("Stop", new Color(244, 67, 54), Color.WHITE);
        stopButton.addActionListener(e -> {
            stopSimulation();
            onBack.run();
        });

        JButton restartButton = createModernButton("Restart", new Color(156, 39, 176), Color.WHITE);
        restartButton.addActionListener(e -> restart());

        JButton exitButton = createModernButton("Exit", new Color(96, 96, 96), Color.WHITE);
        exitButton.addActionListener(e -> {
            stopSimulation();
            onBack.run();
        });

        buttonPanel.add(pauseButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(exitButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        // Bottom: Legend
        JPanel legendPanel = createModernLegend();
        panel.add(legendPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSpeedControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel label = new JLabel("Simulation Speed:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(60, 60, 60));

        speedLabel = new JLabel("Normal");
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        speedLabel.setForeground(new Color(52, 73, 94));
        speedLabel.setPreferredSize(new Dimension(80, 20));

        // Slider: 25ms (fast) to 300ms (slow)
        speedSlider = new JSlider(25, 300, tickDuration);
        speedSlider.setBackground(Color.WHITE);
        speedSlider.setPreferredSize(new Dimension(200, 30));
        speedSlider.setMajorTickSpacing(75);
        speedSlider.setPaintTicks(true);
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                tickDuration = speedSlider.getValue();
                updateSpeedLabel();
                if (timer != null) {
                    timer.setDelay(tickDuration);
                }
            }
        });

        // Quick speed buttons
        JButton slowBtn = new JButton("0.5x");
        JButton normalBtn = new JButton("1x");
        JButton fastBtn = new JButton("2x");

        styleSpeedButton(slowBtn);
        styleSpeedButton(normalBtn);
        styleSpeedButton(fastBtn);

        slowBtn.addActionListener(e -> {
            speedSlider.setValue(200);
        });

        normalBtn.addActionListener(e -> {
            speedSlider.setValue(120);
        });

        fastBtn.addActionListener(e -> {
            speedSlider.setValue(50);
        });

        panel.add(label);
        panel.add(speedSlider);
        panel.add(speedLabel);
        panel.add(slowBtn);
        panel.add(normalBtn);
        panel.add(fastBtn);

        return panel;
    }

    private void styleSpeedButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setPreferredSize(new Dimension(50, 28));
        btn.setBackground(new Color(230, 230, 230));
        btn.setForeground(new Color(60, 60, 60));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void updateSpeedLabel() {
        if (tickDuration <= 50) {
            speedLabel.setText("Very Fast");
            speedLabel.setForeground(new Color(231, 76, 60));
        } else if (tickDuration <= 100) {
            speedLabel.setText("Fast");
            speedLabel.setForeground(new Color(230, 126, 34));
        } else if (tickDuration <= 150) {
            speedLabel.setText("Normal");
            speedLabel.setForeground(new Color(52, 73, 94));
        } else if (tickDuration <= 250) {
            speedLabel.setText("Slow");
            speedLabel.setForeground(new Color(46, 134, 193));
        } else {
            speedLabel.setText("Very Slow");
            speedLabel.setForeground(new Color(142, 68, 173));
        }
    }

    private JButton createModernButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(120, 36));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
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

    private void startSimulation() {
        generateRealisticImperfectData();

        timer = new javax.swing.Timer(tickDuration, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!paused && currentTick < maxTicks) {
                    currentTick++;
                    updatePopulations();
                    updateGrassAndMovement();
                    gridPanel.repaint();
                    curvePanel.repaint();
                    updateStats();
                }
            }
        });
        timer.start();
    }

    private void generateRealisticImperfectData() {
        double prey = 100.0;
        double predator = 20.0;
        double preyTheory = 100.0;
        double predatorTheory = 20.0;

        double alpha = 0.048;
        double beta = 0.0009;
        double gamma = 0.0006;
        double delta = 0.032;

        double preyDrift = 0.0;
        double predatorDrift = 0.0;

        for (int t = 0; t < maxTicks; t++) {
            // Empirical (imperfect)
            double baseNoise = 0.12;
            double preyNoise = 1.0 + (random.nextDouble() - 0.5) * baseNoise;
            double predNoise = 1.0 + (random.nextDouble() - 0.5) * baseNoise;

            if (random.nextDouble() < 0.10) {
                prey *= random.nextBoolean() ? 0.82 : 1.15;
            }

            if (random.nextDouble() < 0.08) {
                predator *= random.nextBoolean() ? 0.85 : 1.12;
            }

            preyDrift += (random.nextDouble() - 0.5) * 0.3;
            predatorDrift += (random.nextDouble() - 0.5) * 0.2;
            prey += preyDrift;
            predator += predatorDrift;

            if (t % 100 == 0) {
                preyDrift *= 0.5;
                predatorDrift *= 0.5;
            }

            double reproductionFactor = random.nextDouble() < 0.15 ? 0.7 + random.nextDouble() * 0.3 : 1.0;
            double dPrey = (alpha * prey - beta * prey * predator) * reproductionFactor;
            double dPredator = gamma * prey * predator - delta * predator;

            if (random.nextDouble() < 0.05) {
                dPrey *= (0.5 + random.nextDouble() * 0.5);
            }

            prey += dPrey;
            predator += dPredator;
            prey = Math.max(8, Math.min(220, prey));
            predator = Math.max(3, Math.min(85, predator));

            if (t % 40 == 0 && random.nextDouble() < 0.25) {
                prey *= 0.80 + random.nextDouble() * 0.15;
            }

            if (t % 50 == 0 && random.nextDouble() < 0.20) {
                predator *= 0.82 + random.nextDouble() * 0.15;
            }

            preyHistory.add((int) (prey * preyNoise));
            predatorHistory.add((int) (predator * predNoise));

            // Theory (smooth)
            preyTheoryHistory.add((int) preyTheory);
            predatorTheoryHistory.add((int) predatorTheory);

            double dPreyT = alpha * preyTheory - beta * preyTheory * predatorTheory;
            double dPredatorT = gamma * preyTheory * predatorTheory - delta * predatorTheory;
            preyTheory += dPreyT;
            predatorTheory += dPredatorT;
            preyTheory = Math.max(10, Math.min(200, preyTheory));
            predatorTheory = Math.max(5, Math.min(80, predatorTheory));
        }
    }

    private void updatePopulations() {
        if (currentTick >= preyHistory.size()) return;

        adjustAgentPopulation(preyAgents, preyHistory.get(currentTick), 100, 120);
        adjustAgentPopulation(predatorAgents, predatorHistory.get(currentTick), 140, 180);

        currentPreyCount = preyAgents.size();
        currentPredatorCount = predatorAgents.size();
    }

    private void adjustAgentPopulation(List<AgentData> agents, int targetCount, int startEnergy, int maxEnergy) {
        while (agents.size() < targetCount) {
            Point pos = new Point(random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE));
            agents.add(new AgentData(pos, startEnergy + random.nextInt(20), maxEnergy));
        }
        while (agents.size() > targetCount) {
            agents.remove(agents.size() - 1);
        }
    }

    private void updateGrassAndMovement() {
        for (AgentData prey : preyAgents) {
            if (grassGrid[prey.position.x][prey.position.y] == -1) {
                grassGrid[prey.position.x][prey.position.y] = GRASS_REGROWTH_TIME;
                prey.energy = Math.min(prey.maxEnergy, prey.energy + 15);
            } else {
                prey.energy = Math.max(5, prey.energy - 2);
            }
        }

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                if (grassGrid[x][y] > 0) {
                    grassGrid[x][y]--;
                    if (grassGrid[x][y] == 0) {
                        grassGrid[x][y] = -1;
                    }
                }
            }
        }

        moveAgents(preyAgents, 0.5);
        moveAgents(predatorAgents, 0.35);

        for (AgentData predator : predatorAgents) {
            predator.energy = Math.max(10, predator.energy - 1);
        }
    }

    private void moveAgents(List<AgentData> agents, double moveProb) {
        for (AgentData agent : agents) {
            if (random.nextDouble() < moveProb) {
                int dx = random.nextInt(3) - 1;
                int dy = random.nextInt(3) - 1;
                int newX = Math.max(0, Math.min(GRID_SIZE - 1, agent.position.x + dx));
                int newY = Math.max(0, Math.min(GRID_SIZE - 1, agent.position.y + dy));
                agent.position.setLocation(newX, newY);
            }
        }
    }

    private void drawGridWithBuffer(Graphics g) {
        bufferGraphics.setColor(Color.WHITE);
        bufferGraphics.fillRect(0, 0, gridBuffer.getWidth(), gridBuffer.getHeight());

        int cellSize = CELL_SIZE;

        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                Color color;
                if (grassGrid[x][y] == -2) {
                    color = new Color(100, 100, 100);
                } else if (grassGrid[x][y] == -1) {
                    color = new Color(76, 175, 80);
                } else if (grassGrid[x][y] > 0) {
                    int progress = (GRASS_REGROWTH_TIME - grassGrid[x][y]) * 100 / GRASS_REGROWTH_TIME;
                    color = new Color(200 + progress / 3, 175 + progress / 2, 150 + progress / 2);
                } else {
                    color = new Color(245, 245, 240);
                }

                bufferGraphics.setColor(color);
                bufferGraphics.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                bufferGraphics.setColor(new Color(230, 230, 230, 100));
                bufferGraphics.drawRect(x * cellSize, y * cellSize, cellSize - 1, cellSize - 1);
            }
        }

        for (AgentData prey : preyAgents) {
            drawAgent(bufferGraphics, prey, new Color(33, 150, 243), cellSize, 30);
        }

        for (AgentData predator : predatorAgents) {
            drawAgent(bufferGraphics, predator, new Color(244, 67, 54), cellSize, 40);
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(gridBuffer, 0, 0, null);
    }

    private void drawAgent(Graphics2D g2d, AgentData agent, Color color, int cellSize, int shadowAlpha) {
        int centerX = agent.position.x * cellSize + cellSize / 2;
        int centerY = agent.position.y * cellSize + cellSize / 2;
        int radius = cellSize / 3;

        g2d.setColor(new Color(0, 0, 0, shadowAlpha));
        g2d.fillOval(centerX - radius + 1, centerY - radius + 2, radius * 2, radius * 2);

        g2d.setColor(color);
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.fillOval(centerX - radius / 2, centerY - radius / 2, radius, radius / 2);

        drawModernEnergyBar(g2d, agent, cellSize);
    }

    private void drawModernEnergyBar(Graphics2D g2d, AgentData agent, int cellSize) {
        int barWidth = cellSize - 4;
        int barHeight = 3;
        int energyWidth = (int) ((agent.energy / (double) agent.maxEnergy) * barWidth);
        energyWidth = Math.max(0, Math.min(barWidth, energyWidth));

        int barX = agent.position.x * cellSize + 2;
        int barY = agent.position.y * cellSize + cellSize - 5;

        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 2, 2);

        if (energyWidth > 0) {
            Color energyColor;
            if (agent.energy > agent.maxEnergy * 0.6) {
                energyColor = new Color(76, 175, 80);
            } else if (agent.energy > agent.maxEnergy * 0.3) {
                energyColor = new Color(255, 193, 7);
            } else {
                energyColor = new Color(244, 67, 54);
            }

            g2d.setColor(energyColor);
            g2d.fillRoundRect(barX, barY, energyWidth, barHeight, 2, 2);
        }
    }

    private void drawPopulationCurves(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = curvePanel.getWidth();
        int height = curvePanel.getHeight();

        if (width <= 0 || height <= 0 || currentTick < 2) return;

        int graphHeight = (height - 30) / 2; // Split into two graphs
        int margin = 50;
        int maxPop = 300;

        // === GRAPH 1: EMPIRICAL DATA (TOP) ===
        drawSeparateGraphBackground(g2d, width, graphHeight, margin, maxPop, 0, "Empirical Data (Simulation)");
        drawSeparateGraph(g2d, preyHistory, predatorHistory, width, graphHeight, margin, maxPop, 0, false);
        drawSeparateLabels(g2d, width, graphHeight, margin, maxPop, 0, true);
        drawSeparateLegend(g2d, margin, 10, false);

        // === GRAPH 2: THEORETICAL LV (BOTTOM) ===
        int yOffset = graphHeight + 15;
        drawSeparateGraphBackground(g2d, width, graphHeight, margin, maxPop, yOffset, "Lotka-Volterra Theory");
        drawSeparateGraph(g2d, preyTheoryHistory, predatorTheoryHistory, width, graphHeight, margin, maxPop, yOffset, true);
        drawSeparateLabels(g2d, width, graphHeight, margin, maxPop, yOffset, false);
        drawSeparateLegend(g2d, margin, yOffset + 10, true);
    }

    private void drawSeparateGraphBackground(Graphics2D g2d, int width, int height, int margin, int maxPop, int yOffset, String title) {
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
        for (int i = 1; i <= 5; i++) {
            int y = yOffset + height - margin - (height - 2 * margin - 20) * i / 5;
            g2d.drawLine(margin, y, width - margin, y);
        }
    }

    private void drawSeparateGraph(Graphics2D g2d, List<Integer> preyData, List<Integer> predData,
                                   int width, int height, int margin, int maxPop, int yOffset, boolean isTheory) {
        int graphWidth = width - 2 * margin;
        int graphHeight = height - 2 * margin - 20;

        // Draw prey
        g2d.setColor(new Color(46, 204, 113));
        g2d.setStroke(new BasicStroke(2.5f));
        for (int i = 0; i < Math.min(currentTick - 1, preyData.size() - 1); i++) {
            int x1 = margin + (graphWidth * i) / maxTicks;
            int y1 = yOffset + height - margin - (graphHeight * preyData.get(i)) / maxPop;
            int x2 = margin + (graphWidth * (i + 1)) / maxTicks;
            int y2 = yOffset + height - margin - (graphHeight * preyData.get(i + 1)) / maxPop;
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Draw predator
        g2d.setColor(new Color(231, 76, 60));
        for (int i = 0; i < Math.min(currentTick - 1, predData.size() - 1); i++) {
            int x1 = margin + (graphWidth * i) / maxTicks;
            int y1 = yOffset + height - margin - (graphHeight * predData.get(i)) / maxPop;
            int x2 = margin + (graphWidth * (i + 1)) / maxTicks;
            int y2 = yOffset + height - margin - (graphHeight * predData.get(i + 1)) / maxPop;
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.setStroke(new BasicStroke(1.0f));
    }

    private void drawSeparateLabels(Graphics2D g2d, int width, int height, int margin, int maxPop, int yOffset, boolean includeXLabel) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        // Y-axis labels
        for (int i = 0; i <= 5; i++) {
            int y = yOffset + height - margin - (height - 2 * margin - 20) * i / 5;
            int value = (maxPop * i) / 5;
            g2d.drawString(String.valueOf(value), margin - 35, y + 5);
        }

        // X-axis labels
        int labelCount = 10;
        for (int i = 0; i <= labelCount; i++) {
            int x = margin + (width - 2 * margin) * i / labelCount;
            int tickValue = (maxTicks * i) / labelCount;
            g2d.drawString(String.valueOf(tickValue), x - 10, yOffset + height - margin + 18);
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

    private void drawSeparateLegend(Graphics2D g2d, int x, int y, boolean isTheory) {
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

    private void drawCurveData(Graphics2D g2d, List<Integer> data, int margin, int graphWidth, int graphHeight, int maxPop, int height) {
        for (int i = 0; i < Math.min(currentTick - 1, data.size() - 1); i++) {
            int x1 = margin + (graphWidth * i) / maxTicks;
            int y1 = height - margin - (graphHeight * data.get(i)) / maxPop;
            int x2 = margin + (graphWidth * (i + 1)) / maxTicks;
            int y2 = height - margin - (graphHeight * data.get(i + 1)) / maxPop;
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawCurveLabels(Graphics2D g2d, int width, int height, int margin, int maxPop) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));

        // Y-axis labels (smaller scale - show up to 300)
        int yScale = 300;  // Increased from 250
        for (int i = 0; i <= 6; i++) {
            int y = height - margin - (height - 2 * margin) * i / 6;
            int value = (yScale * i) / 6;
            g2d.drawString(String.valueOf(value), margin - 35, y + 5);
        }

        // X-axis labels - SMALLER SCALE (more spread out numbers)
        int graphWidth = width - 2 * margin;
        int labelCount = 10;  // Show 10 labels across the graph

        for (int i = 0; i <= labelCount; i++) {
            int x = margin + (graphWidth * i) / labelCount;
            int tickValue = (maxTicks * i) / labelCount;

            // Draw tick mark
            g2d.setColor(new Color(180, 180, 180));
            g2d.drawLine(x, height - margin, x, height - margin + 5);

            // Draw label
            g2d.setColor(Color.BLACK);
            String label = String.valueOf(tickValue);
            int labelWidth = g2d.getFontMetrics().stringWidth(label);
            g2d.drawString(label, x - labelWidth / 2, height - margin + 18);
        }

        // Axis titles
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.drawString("Tick", width / 2 - 15, height - 10);

        Graphics2D g2dRot = (Graphics2D) g2d.create();
        g2dRot.translate(15, height / 2);
        g2dRot.rotate(-Math.PI / 2);
        g2dRot.drawString("Population", -40, 0);
        g2dRot.dispose();
    }

    private void drawCurveLegend(Graphics2D g2d, int x, int y) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));

        g2d.setColor(new Color(46, 204, 113));
        g2d.fillRect(x, y, 15, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Prey (Empirical)", x + 20, y + 10);

        g2d.setColor(new Color(231, 76, 60));
        g2d.fillRect(x, y + 20, 15, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Predator (Empirical)", x + 20, y + 30);

        g2d.setColor(new Color(46, 204, 113, 150));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f));
        g2d.drawLine(x, y + 42, x + 15, y + 42);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawString("Prey (LV Theory)", x + 20, y + 50);

        g2d.setColor(new Color(231, 76, 60, 150));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f));
        g2d.drawLine(x, y + 62, x + 15, y + 62);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawString("Predator (LV Theory)", x + 20, y + 70);
    }

    private void updateStats() {
        int grassCount = 0;
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                if (grassGrid[x][y] == -1) grassCount++;
            }
        }

        double avgPreyEnergy = preyAgents.stream().mapToInt(a -> a.energy).average().orElse(0);
        double avgPredEnergy = predatorAgents.stream().mapToInt(a -> a.energy).average().orElse(0);

        statsLabel.setText(String.format(
                "Tick: %d  |  Prey: %d (Energy: %.1f)  |  Predators: %d (Energy: %.1f)  |  Grass: %.1f%%",
                currentTick, currentPreyCount, avgPreyEnergy, currentPredatorCount, avgPredEnergy,
                (grassCount / (double)(GRID_SIZE * GRID_SIZE)) * 100
        ));
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

    private void restart() {
        currentTick = 0;
        preyAgents.clear();
        predatorAgents.clear();
        preyHistory.clear();
        predatorHistory.clear();
        preyTheoryHistory.clear();
        predatorTheoryHistory.clear();
        initializeGrass();
        generateRealisticImperfectData();
        gridPanel.repaint();
        curvePanel.repaint();
    }

    private void stopSimulation() {
        if (timer != null) {
            timer.stop();
        }
        if (bufferGraphics != null) {
            bufferGraphics.dispose();
        }
        if (gridBuffer != null) {
            gridBuffer.flush();
        }
    }
}
