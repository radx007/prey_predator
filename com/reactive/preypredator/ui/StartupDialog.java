package com.reactive.preypredator.ui;

import com.reactive.preypredator.config.Config;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Startup dialog for selecting configuration mode
 */
public class StartupDialog extends JDialog {
    private boolean useDefaultConfig = true;
    private boolean startSimulation = false;
    private ConfigurationPanel configPanel;

    public StartupDialog() {
        super((Frame) null, "Predator-Prey Simulation Startup", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Center panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));

        // Tab 1: Quick Start
        JPanel quickStartPanel = createQuickStartPanel();
        tabbedPane.addTab("Quick Start", quickStartPanel);

        // Tab 2: Custom Configuration
        JPanel customConfigPanel = createCustomConfigPanel();
        tabbedPane.addTab("Custom Configuration", customConfigPanel);

        // Tab 3: About
        JPanel aboutPanel = createAboutPanel();
        tabbedPane.addTab("About", aboutPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(33, 150, 243));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Predator-Prey Multi-Agent Simulation");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Lotka-Volterra Dynamics with JADE Framework");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(200, 230, 255));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createQuickStartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Default Configuration"));

        String[] info = {
                "Grid Size: " + Config.GRID_WIDTH + " x " + Config.GRID_HEIGHT,
                "Initial Prey: " + Config.INITIAL_PREY_COUNT,
                "Initial Predators: " + Config.INITIAL_PREDATOR_COUNT,
                "",
                "Prey Parameters:",
                "  - Starting Energy: " + Config.PREY_ENERGY_START,
                "  - Energy from Grass: " + Config.PREY_ENERGY_FROM_GRASS,
                "  - Move Cost: " + Config.PREY_ENERGY_MOVE_COST,
                "  - Reproduction Cooldown: " + Config.PREY_REPRODUCTION_COOLDOWN + " ticks",
                "",
                "Predator Parameters:",
                "  - Starting Energy: " + Config.PREDATOR_ENERGY_START,
                "  - Energy from Prey: " + Config.PREDATOR_ENERGY_FROM_PREY,
                "  - Move Cost: " + Config.PREDATOR_ENERGY_MOVE_COST,
                "  - Reproduction Cooldown: " + Config.PREDATOR_REPRODUCTION_COOLDOWN + " ticks",
                "",
                "Environment:",
                "  - Grass Regrowth: " + Config.GRASS_REGROWTH_TICKS + " ticks",
                "  - Grass Coverage: " + (Config.GRASS_INITIAL_COVERAGE * 100) + "%",
                "  - Obstacle Coverage: " + (Config.OBSTACLE_COVERAGE * 100) + "%",
                "",
                "Simulation:",
                "  - Tick Duration: " + Config.TICK_DURATION_MS + " ms",
                "  - Max Ticks: " + Config.MAX_TICKS
        };

        for (String line : info) {
            JLabel label = new JLabel(line);
            label.setFont(new Font("Monospaced", Font.PLAIN, 12));
            infoPanel.add(label);
        }

        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Description
        JTextArea descArea = new JTextArea(
                "The default configuration is optimized for stable Lotka-Volterra oscillations.\n\n" +
                        "Key Features:\n" +
                        "- Higher initial populations to prevent early extinction\n" +
                        "- Balanced energy costs and gains\n" +
                        "- Moderate reproduction rates\n" +
                        "- Fast grass regrowth to sustain prey population\n\n" +
                        "Click 'Start with Default Config' to begin!"
        );
        descArea.setFont(new Font("Arial", Font.PLAIN, 13));
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createTitledBorder("Description"));
        descArea.setBackground(new Color(240, 248, 255));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, descArea);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.7);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCustomConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel(
                "<html><b>Customize simulation parameters below.</b><br>" +
                        "Adjust values to experiment with different population dynamics.<br>" +
                        "Click 'Start with Custom Config' when ready.</html>"
        );
        infoLabel.setBorder(new EmptyBorder(5, 5, 10, 5));
        panel.add(infoLabel, BorderLayout.NORTH);

        // Configuration panel (no callback needed here)
        configPanel = new ConfigurationPanel(null);
        panel.add(configPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JTextArea aboutText = new JTextArea();
        aboutText.setText(
                "REACTIVE PREDATOR-PREY MULTI-AGENT SIMULATION\n" +
                        "========================================================\n\n" +

                        "ABOUT THIS SIMULATION:\n" +
                        "This is a fully reactive agent-based model implementing Lotka-Volterra\n" +
                        "predator-prey dynamics using the JADE (Java Agent Development Framework).\n\n" +

                        "KEY FEATURES:\n" +
                        "- Fully reactive agent behaviors (no messaging overhead)\n" +
                        "- Real-time visualization with population dynamics curves\n" +
                        "- Configurable parameters for experimentation\n" +
                        "- Energy-based survival mechanics\n" +
                        "- Gender-based reproduction\n" +
                        "- Grass resource management\n" +
                        "- Vision-based perception\n" +
                        "- CSV data export for analysis\n\n" +

                        "LOTKA-VOLTERRA EQUATIONS:\n" +
                        "The classic predator-prey model describes population dynamics:\n" +
                        "  dx/dt = ax - bxy   (Prey growth - predation)\n" +
                        "  dy/dt = dxy - cy   (Predation benefit - predator death)\n\n" +

                        "Where:\n" +
                        "  x = prey population\n" +
                        "  y = predator population\n" +
                        "  a = prey reproduction rate\n" +
                        "  b = predation rate\n" +
                        "  d = predator efficiency\n" +
                        "  c = predator death rate\n\n" +

                        "AGENT BEHAVIORS:\n" +
                        "- Prey: Seek grass, flee from predators, reproduce with mates\n" +
                        "- Predators: Hunt prey, wander when hungry, reproduce with mates\n" +
                        "- Both: Energy-based survival, starvation mechanics\n\n" +

                        "TECHNICAL DETAILS:\n" +
                        "- Framework: JADE (Java Agent Development Framework)\n" +
                        "- Architecture: Fully reactive (no ACL messages)\n" +
                        "- Synchronization: Tick-based with shared environment state\n" +
                        "- UI: Swing with real-time rendering\n\n" +

                        "TIPS FOR STABLE OSCILLATIONS:\n" +
                        "1. Start with more prey than predators (ratio 3:1 to 5:1)\n" +
                        "2. Predators should have higher energy costs than prey\n" +
                        "3. Grass should regrow fast enough to sustain prey recovery\n" +
                        "4. Reproduction rates should favor prey over predators\n\n" +

                        "========================================================\n" +
                        "Version: 1.0\n" +
                        "Framework: JADE 4.x\n" +
                        "Language: Java\n"
        );

        aboutText.setFont(new Font("Monospaced", Font.PLAIN, 11));
        aboutText.setEditable(false);
        aboutText.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(aboutText);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBorder(new EmptyBorder(5, 10, 10, 10));

        JButton defaultButton = new JButton("Start with Default Config");
        defaultButton.setFont(new Font("Arial", Font.BOLD, 14));
        defaultButton.setPreferredSize(new Dimension(250, 40));
        defaultButton.setBackground(new Color(76, 175, 80));
        defaultButton.setForeground(Color.WHITE);
        defaultButton.setFocusPainted(false);
        defaultButton.addActionListener(e -> {
            Config.resetToDefaults();
            useDefaultConfig = true;
            startSimulation = true;
            dispose();
        });

        JButton customButton = new JButton("Start with Custom Config");
        customButton.setFont(new Font("Arial", Font.BOLD, 14));
        customButton.setPreferredSize(new Dimension(250, 40));
        customButton.setBackground(new Color(33, 150, 243));
        customButton.setForeground(Color.WHITE);
        customButton.setFocusPainted(false);
        customButton.addActionListener(e -> {
            // Apply custom config from panel
            if (configPanel != null) {
                configPanel.applyConfigurationSilently();
            }
            useDefaultConfig = false;
            startSimulation = true;
            dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setPreferredSize(new Dimension(150, 40));
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> {
            startSimulation = false;
            dispose();
        });

        panel.add(defaultButton);
        panel.add(customButton);
        panel.add(cancelButton);

        return panel;
    }

    public boolean isUseDefaultConfig() {
        return useDefaultConfig;
    }

    public boolean shouldStartSimulation() {
        return startSimulation;
    }

    /**
     * Show the dialog and wait for user choice
     */
    public static StartupDialog showDialog() {
        StartupDialog dialog = new StartupDialog();
        dialog.setVisible(true);
        return dialog;
    }
}
