package com.reactive.preypredator.ui;

import com.reactive.preypredator.config.Config;
import com.reactive.preypredator.model.PlacementMode;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Modern configuration panel with placement mode selector
 */
public class ConfigurationPanel extends JPanel {
    private Map<String, JSpinner> spinners;
    private JButton applyButton;
    private JButton resetButton;
    private JComboBox<String> placementCombo;
    private Runnable onApplyCallback;

    public ConfigurationPanel(Runnable onApplyCallback) {
        this.onApplyCallback = onApplyCallback;
        this.spinners = new HashMap<>();

        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(63, 81, 181));
        titlePanel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel titleLabel = new JLabel("Configuration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        add(titlePanel, BorderLayout.NORTH);

        // Main panel with sections
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(10, 5, 10, 5));

        // Placement mode selector at top
        mainPanel.add(createPlacementModePanel());
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createModernSection("Population", createPopulationSection()));
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createModernSection("Prey", createPreySection()));
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createModernSection("Predator", createPredatorSection()));
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createModernSection("Environment", createEnvironmentSection()));
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createModernSection("Simulation", createSimulationSection()));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createPlacementModePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel label = new JLabel("Initial Placement:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(80, 80, 80));

        placementCombo = new JComboBox<>(new String[]{"Random", "Fixed Pattern"});
        placementCombo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        placementCombo.setSelectedItem(
                Config.PLACEMENT_MODE == PlacementMode.FIXED_PATTERN ? "Fixed Pattern" : "Random"
        );
        placementCombo.setPreferredSize(new Dimension(150, 28));

        JLabel helpLabel = new JLabel("(Fixed = reproducible testing)");
        helpLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        helpLabel.setForeground(new Color(120, 120, 120));

        panel.add(label);
        panel.add(placementCombo);
        panel.add(helpLabel);

        return panel;
    }

    private JPanel createModernSection(String title, JPanel content) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 224, 224), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(63, 81, 181));
        titleLabel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(titleLabel, BorderLayout.NORTH);
        wrapper.add(content, BorderLayout.CENTER);

        section.add(wrapper);
        return section;
    }

    private JPanel createPopulationSection() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 8));
        panel.setBackground(Color.WHITE);

        addModernSpinner(panel, "Initial Prey", "INITIAL_PREY_COUNT", Config.INITIAL_PREY_COUNT, 10, 500, 10);
        addModernSpinner(panel, "Initial Predators", "INITIAL_PREDATOR_COUNT", Config.INITIAL_PREDATOR_COUNT, 5, 200, 5);

        return panel;
    }

    private JPanel createPreySection() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 8));
        panel.setBackground(Color.WHITE);

        addModernSpinner(panel, "Start Energy", "PREY_ENERGY_START", Config.PREY_ENERGY_START, 50, 300, 10);
        addModernSpinner(panel, "Max Energy", "PREY_ENERGY_MAX", Config.PREY_ENERGY_MAX, 100, 400, 10);
        addModernSpinner(panel, "Energy from Grass", "PREY_ENERGY_FROM_GRASS", Config.PREY_ENERGY_FROM_GRASS, 10, 100, 5);
        addModernDoubleSpinner(panel, "Move Cost", "PREY_ENERGY_MOVE_COST", Config.PREY_ENERGY_MOVE_COST, 0.1, 5.0, 0.1);
        addModernSpinner(panel, "Reproduction Threshold", "PREY_REPRODUCTION_THRESHOLD", Config.PREY_REPRODUCTION_THRESHOLD, 30, 200, 5);
        addModernSpinner(panel, "Reproduction Cooldown", "PREY_REPRODUCTION_COOLDOWN", Config.PREY_REPRODUCTION_COOLDOWN, 1, 20, 1);
        addModernSpinner(panel, "Starvation Threshold", "PREY_STARVATION_THRESHOLD", Config.PREY_STARVATION_THRESHOLD, 0, 50, 5);
        addModernSpinner(panel, "Vision Range", "PREY_VISION_RANGE", Config.PREY_VISION_RANGE, 1, 20, 1);

        return panel;
    }

    private JPanel createPredatorSection() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 8));
        panel.setBackground(Color.WHITE);

        addModernSpinner(panel, "Start Energy", "PREDATOR_ENERGY_START", Config.PREDATOR_ENERGY_START, 50, 300, 10);
        addModernSpinner(panel, "Max Energy", "PREDATOR_ENERGY_MAX", Config.PREDATOR_ENERGY_MAX, 100, 500, 10);
        addModernSpinner(panel, "Energy from Prey", "PREDATOR_ENERGY_FROM_PREY", Config.PREDATOR_ENERGY_FROM_PREY, 20, 150, 5);
        addModernDoubleSpinner(panel, "Move Cost", "PREDATOR_ENERGY_MOVE_COST", Config.PREDATOR_ENERGY_MOVE_COST, 0.1, 10.0, 0.1);
        addModernSpinner(panel, "Reproduction Threshold", "PREDATOR_REPRODUCTION_THRESHOLD", Config.PREDATOR_REPRODUCTION_THRESHOLD, 50, 300, 10);
        addModernSpinner(panel, "Reproduction Cooldown", "PREDATOR_REPRODUCTION_COOLDOWN", Config.PREDATOR_REPRODUCTION_COOLDOWN, 1, 30, 1);
        addModernSpinner(panel, "Starvation Threshold", "PREDATOR_STARVATION_THRESHOLD", Config.PREDATOR_STARVATION_THRESHOLD, 0, 50, 5);
        addModernSpinner(panel, "Vision Range", "PREDATOR_VISION_RANGE", Config.PREDATOR_VISION_RANGE, 1, 30, 1);

        return panel;
    }

    private JPanel createEnvironmentSection() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 8));
        panel.setBackground(Color.WHITE);

        addModernSpinner(panel, "Grass Regrowth Ticks", "GRASS_REGROWTH_TICKS", Config.GRASS_REGROWTH_TICKS, 1, 50, 1);
        addModernDoubleSpinner(panel, "Initial Grass Coverage", "GRASS_INITIAL_COVERAGE", Config.GRASS_INITIAL_COVERAGE, 0.1, 1.0, 0.05);
        addModernDoubleSpinner(panel, "Obstacle Coverage", "OBSTACLE_COVERAGE", Config.OBSTACLE_COVERAGE, 0.0, 0.3, 0.01);

        return panel;
    }

    private JPanel createSimulationSection() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 8));
        panel.setBackground(Color.WHITE);

        addModernSpinner(panel, "Tick Duration (ms)", "TICK_DURATION_MS", Config.TICK_DURATION_MS, 10, 1000, 10);
        addModernSpinner(panel, "Max Ticks", "MAX_TICKS", Config.MAX_TICKS, 100, 10000, 100);

        return panel;
    }

    private void addModernSpinner(JPanel panel, String label, String key, int value, int min, int max, int step) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        jLabel.setForeground(new Color(80, 80, 80));

        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setHorizontalAlignment(JTextField.RIGHT);
        }

        spinners.put(key, spinner);
        panel.add(jLabel);
        panel.add(spinner);
    }

    private void addModernDoubleSpinner(JPanel panel, String label, String key, double value, double min, double max, double step) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        jLabel.setForeground(new Color(80, 80, 80));

        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.0");
        spinner.setEditor(editor);
        editor.getTextField().setHorizontalAlignment(JTextField.RIGHT);

        spinners.put(key, spinner);
        panel.add(jLabel);
        panel.add(spinner);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        applyButton = new JButton("Apply & Restart");
        applyButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        applyButton.setBackground(new Color(76, 175, 80));
        applyButton.setForeground(Color.WHITE);
        applyButton.setFocusPainted(false);
        applyButton.setBorderPainted(false);
        applyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        applyButton.addActionListener(e -> applyConfiguration());

        resetButton = new JButton("Reset to Defaults");
        resetButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resetButton.setBackground(new Color(245, 245, 245));
        resetButton.setForeground(new Color(80, 80, 80));
        resetButton.setFocusPainted(false);
        resetButton.setBorderPainted(false);
        resetButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetButton.addActionListener(e -> resetToDefaults());

        panel.add(applyButton);
        panel.add(resetButton);
        return panel;
    }

    private void applyConfiguration() {
        // Apply placement mode
        String placement = (String) placementCombo.getSelectedItem();
        Config.PLACEMENT_MODE = "Fixed Pattern".equals(placement) ?
                PlacementMode.FIXED_PATTERN : PlacementMode.RANDOM;

        // Apply all other configs
        Config.INITIAL_PREY_COUNT = (int) spinners.get("INITIAL_PREY_COUNT").getValue();
        Config.INITIAL_PREDATOR_COUNT = (int) spinners.get("INITIAL_PREDATOR_COUNT").getValue();
        Config.PREY_ENERGY_START = (int) spinners.get("PREY_ENERGY_START").getValue();
        Config.PREY_ENERGY_MAX = (int) spinners.get("PREY_ENERGY_MAX").getValue();
        Config.PREY_ENERGY_FROM_GRASS = (int) spinners.get("PREY_ENERGY_FROM_GRASS").getValue();
        Config.PREY_ENERGY_MOVE_COST = (double) spinners.get("PREY_ENERGY_MOVE_COST").getValue();
        Config.PREY_REPRODUCTION_THRESHOLD = (int) spinners.get("PREY_REPRODUCTION_THRESHOLD").getValue();
        Config.PREY_REPRODUCTION_COOLDOWN = (int) spinners.get("PREY_REPRODUCTION_COOLDOWN").getValue();
        Config.PREY_STARVATION_THRESHOLD = (int) spinners.get("PREY_STARVATION_THRESHOLD").getValue();
        Config.PREY_VISION_RANGE = (int) spinners.get("PREY_VISION_RANGE").getValue();
        Config.PREDATOR_ENERGY_START = (int) spinners.get("PREDATOR_ENERGY_START").getValue();
        Config.PREDATOR_ENERGY_MAX = (int) spinners.get("PREDATOR_ENERGY_MAX").getValue();
        Config.PREDATOR_ENERGY_FROM_PREY = (int) spinners.get("PREDATOR_ENERGY_FROM_PREY").getValue();
        Config.PREDATOR_ENERGY_MOVE_COST = (double) spinners.get("PREDATOR_ENERGY_MOVE_COST").getValue();
        Config.PREDATOR_REPRODUCTION_THRESHOLD = (int) spinners.get("PREDATOR_REPRODUCTION_THRESHOLD").getValue();
        Config.PREDATOR_REPRODUCTION_COOLDOWN = (int) spinners.get("PREDATOR_REPRODUCTION_COOLDOWN").getValue();
        Config.PREDATOR_STARVATION_THRESHOLD = (int) spinners.get("PREDATOR_STARVATION_THRESHOLD").getValue();
        Config.PREDATOR_VISION_RANGE = (int) spinners.get("PREDATOR_VISION_RANGE").getValue();
        Config.GRASS_REGROWTH_TICKS = (int) spinners.get("GRASS_REGROWTH_TICKS").getValue();
        Config.GRASS_INITIAL_COVERAGE = (double) spinners.get("GRASS_INITIAL_COVERAGE").getValue();
        Config.OBSTACLE_COVERAGE = (double) spinners.get("OBSTACLE_COVERAGE").getValue();
        Config.TICK_DURATION_MS = (int) spinners.get("TICK_DURATION_MS").getValue();
        Config.MAX_TICKS = (int) spinners.get("MAX_TICKS").getValue();

        if (onApplyCallback != null) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Applying new configuration will restart the simulation. Continue?",
                    "Restart Simulation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                onApplyCallback.run();
            }
        }
    }

    public void applyConfigurationSilently() {
        String placement = (String) placementCombo.getSelectedItem();
        Config.PLACEMENT_MODE = "Fixed Pattern".equals(placement) ?
                PlacementMode.FIXED_PATTERN : PlacementMode.RANDOM;

        Config.INITIAL_PREY_COUNT = (int) spinners.get("INITIAL_PREY_COUNT").getValue();
        Config.INITIAL_PREDATOR_COUNT = (int) spinners.get("INITIAL_PREDATOR_COUNT").getValue();
        Config.PREY_ENERGY_START = (int) spinners.get("PREY_ENERGY_START").getValue();
        Config.PREY_ENERGY_MAX = (int) spinners.get("PREY_ENERGY_MAX").getValue();
        Config.PREY_ENERGY_FROM_GRASS = (int) spinners.get("PREY_ENERGY_FROM_GRASS").getValue();
        Config.PREY_ENERGY_MOVE_COST = (double) spinners.get("PREY_ENERGY_MOVE_COST").getValue();
        Config.PREY_REPRODUCTION_THRESHOLD = (int) spinners.get("PREY_REPRODUCTION_THRESHOLD").getValue();
        Config.PREY_REPRODUCTION_COOLDOWN = (int) spinners.get("PREY_REPRODUCTION_COOLDOWN").getValue();
        Config.PREY_STARVATION_THRESHOLD = (int) spinners.get("PREY_STARVATION_THRESHOLD").getValue();
        Config.PREY_VISION_RANGE = (int) spinners.get("PREY_VISION_RANGE").getValue();
        Config.PREDATOR_ENERGY_START = (int) spinners.get("PREDATOR_ENERGY_START").getValue();
        Config.PREDATOR_ENERGY_MAX = (int) spinners.get("PREDATOR_ENERGY_MAX").getValue();
        Config.PREDATOR_ENERGY_FROM_PREY = (int) spinners.get("PREDATOR_ENERGY_FROM_PREY").getValue();
        Config.PREDATOR_ENERGY_MOVE_COST = (double) spinners.get("PREDATOR_ENERGY_MOVE_COST").getValue();
        Config.PREDATOR_REPRODUCTION_THRESHOLD = (int) spinners.get("PREDATOR_REPRODUCTION_THRESHOLD").getValue();
        Config.PREDATOR_REPRODUCTION_COOLDOWN = (int) spinners.get("PREDATOR_REPRODUCTION_COOLDOWN").getValue();
        Config.PREDATOR_STARVATION_THRESHOLD = (int) spinners.get("PREDATOR_STARVATION_THRESHOLD").getValue();
        Config.PREDATOR_VISION_RANGE = (int) spinners.get("PREDATOR_VISION_RANGE").getValue();
        Config.GRASS_REGROWTH_TICKS = (int) spinners.get("GRASS_REGROWTH_TICKS").getValue();
        Config.GRASS_INITIAL_COVERAGE = (double) spinners.get("GRASS_INITIAL_COVERAGE").getValue();
        Config.OBSTACLE_COVERAGE = (double) spinners.get("OBSTACLE_COVERAGE").getValue();
        Config.TICK_DURATION_MS = (int) spinners.get("TICK_DURATION_MS").getValue();
        Config.MAX_TICKS = (int) spinners.get("MAX_TICKS").getValue();
    }

    private void resetToDefaults() {
        Config.resetToDefaults();
        updateSpinnersFromConfig();
        JOptionPane.showMessageDialog(
                this,
                "Configuration reset to default values.\nClick 'Apply & Restart' to use these settings.",
                "Reset Complete",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void updateSpinnersFromConfig() {
        placementCombo.setSelectedItem(
                Config.PLACEMENT_MODE == PlacementMode.FIXED_PATTERN ? "Fixed Pattern" : "Random"
        );
        spinners.get("INITIAL_PREY_COUNT").setValue(Config.INITIAL_PREY_COUNT);
        spinners.get("INITIAL_PREDATOR_COUNT").setValue(Config.INITIAL_PREDATOR_COUNT);
        spinners.get("PREY_ENERGY_START").setValue(Config.PREY_ENERGY_START);
        spinners.get("PREY_ENERGY_MAX").setValue(Config.PREY_ENERGY_MAX);
        spinners.get("PREY_ENERGY_FROM_GRASS").setValue(Config.PREY_ENERGY_FROM_GRASS);
        spinners.get("PREY_ENERGY_MOVE_COST").setValue(Config.PREY_ENERGY_MOVE_COST);
        spinners.get("PREY_REPRODUCTION_THRESHOLD").setValue(Config.PREY_REPRODUCTION_THRESHOLD);
        spinners.get("PREY_REPRODUCTION_COOLDOWN").setValue(Config.PREY_REPRODUCTION_COOLDOWN);
        spinners.get("PREY_STARVATION_THRESHOLD").setValue(Config.PREY_STARVATION_THRESHOLD);
        spinners.get("PREY_VISION_RANGE").setValue(Config.PREY_VISION_RANGE);
        spinners.get("PREDATOR_ENERGY_START").setValue(Config.PREDATOR_ENERGY_START);
        spinners.get("PREDATOR_ENERGY_MAX").setValue(Config.PREDATOR_ENERGY_MAX);
        spinners.get("PREDATOR_ENERGY_FROM_PREY").setValue(Config.PREDATOR_ENERGY_FROM_PREY);
        spinners.get("PREDATOR_ENERGY_MOVE_COST").setValue(Config.PREDATOR_ENERGY_MOVE_COST);
        spinners.get("PREDATOR_REPRODUCTION_THRESHOLD").setValue(Config.PREDATOR_REPRODUCTION_THRESHOLD);
        spinners.get("PREDATOR_REPRODUCTION_COOLDOWN").setValue(Config.PREDATOR_REPRODUCTION_COOLDOWN);
        spinners.get("PREDATOR_STARVATION_THRESHOLD").setValue(Config.PREDATOR_STARVATION_THRESHOLD);
        spinners.get("PREDATOR_VISION_RANGE").setValue(Config.PREDATOR_VISION_RANGE);
        spinners.get("GRASS_REGROWTH_TICKS").setValue(Config.GRASS_REGROWTH_TICKS);
        spinners.get("GRASS_INITIAL_COVERAGE").setValue(Config.GRASS_INITIAL_COVERAGE);
        spinners.get("OBSTACLE_COVERAGE").setValue(Config.OBSTACLE_COVERAGE);
        spinners.get("TICK_DURATION_MS").setValue(Config.TICK_DURATION_MS);
        spinners.get("MAX_TICKS").setValue(Config.MAX_TICKS);
    }
}
