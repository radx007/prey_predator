package com.reactive.preypredator.ui;

import com.reactive.preypredator.config.Config;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration editor panel with form and start button
 * FIXED: Spinner ranges for high reproduction cooldown values
 */
public class ConfigEditor extends JPanel {
    private Map<String, JSpinner> spinners;

    public ConfigEditor(Runnable onStart, Runnable onBack) {
        this.spinners = new HashMap<>();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(52, 73, 94));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Edit Configuration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // Form content
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        formContainer.add(createSection("Population", createPopulationPanel()));
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(createSection("Prey Parameters", createPreyPanel()));
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(createSection("Predator Parameters", createPredatorPanel()));
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(createSection("Environment", createEnvironmentPanel()));

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(245, 245, 247));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton backBtn = new JButton("← Back");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backBtn.setPreferredSize(new Dimension(100, 38));
        backBtn.setBackground(new Color(149, 165, 166));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> onBack.run());

        JButton resetBtn = new JButton("Reset Defaults");
        resetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        resetBtn.setPreferredSize(new Dimension(140, 38));
        resetBtn.setBackground(new Color(230, 126, 34));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.setBorderPainted(false);
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> {
            Config.resetToDefaults();
            refreshSpinners();
            JOptionPane.showMessageDialog(this, "Configuration reset to defaults");
        });

        JButton startBtn = new JButton("Start Simulation →");
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        startBtn.setPreferredSize(new Dimension(160, 38));
        startBtn.setBackground(new Color(46, 204, 113));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFocusPainted(false);
        startBtn.setBorderPainted(false);
        startBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> {
            applyConfiguration();
            onStart.run();
        });

        buttonPanel.add(backBtn);
        buttonPanel.add(resetBtn);
        buttonPanel.add(startBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSection(String title, JPanel content) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(titleLabel, BorderLayout.NORTH);
        wrapper.add(content, BorderLayout.CENTER);

        section.add(wrapper);
        return section;
    }

    private JPanel createPopulationPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 15, 8));
        panel.setBackground(Color.WHITE);
        addSpinner(panel, "Initial Prey", "INITIAL_PREY_COUNT", Config.INITIAL_PREY_COUNT, 10, 500, 10);
        addSpinner(panel, "Initial Predators", "INITIAL_PREDATOR_COUNT", Config.INITIAL_PREDATOR_COUNT, 5, 200, 5);
        return panel;
    }

    private JPanel createPreyPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 15, 8));
        panel.setBackground(Color.WHITE);
        addSpinner(panel, "Start Energy", "PREY_ENERGY_START", Config.PREY_ENERGY_START, 20, 300, 10);
        addSpinner(panel, "Max Energy", "PREY_ENERGY_MAX", Config.PREY_ENERGY_MAX, 50, 400, 10);
        addSpinner(panel, "Energy from Grass", "PREY_ENERGY_FROM_GRASS", Config.PREY_ENERGY_FROM_GRASS, 5, 150, 5);
        addDoubleSpinner(panel, "Move Cost", "PREY_ENERGY_MOVE_COST", Config.PREY_ENERGY_MOVE_COST, 0.1, 10.0, 0.1);
        addSpinner(panel, "Reproduction Threshold", "PREY_REPRODUCTION_THRESHOLD", Config.PREY_REPRODUCTION_THRESHOLD, 30, 200, 5);
        addSpinner(panel, "Reproduction Cooldown", "PREY_REPRODUCTION_COOLDOWN", Config.PREY_REPRODUCTION_COOLDOWN, 1, 1000, 10);  // FIXED: max 1000
        return panel;
    }

    private JPanel createPredatorPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 15, 8));
        panel.setBackground(Color.WHITE);
        addSpinner(panel, "Start Energy", "PREDATOR_ENERGY_START", Config.PREDATOR_ENERGY_START, 20, 400, 10);  // FIXED: max 400
        addSpinner(panel, "Max Energy", "PREDATOR_ENERGY_MAX", Config.PREDATOR_ENERGY_MAX, 50, 500, 10);
        addSpinner(panel, "Energy from Prey", "PREDATOR_ENERGY_FROM_PREY", Config.PREDATOR_ENERGY_FROM_PREY, 10, 200, 5);
        addDoubleSpinner(panel, "Move Cost", "PREDATOR_ENERGY_MOVE_COST", Config.PREDATOR_ENERGY_MOVE_COST, 0.1, 20.0, 0.1);
        addSpinner(panel, "Reproduction Threshold", "PREDATOR_REPRODUCTION_THRESHOLD", Config.PREDATOR_REPRODUCTION_THRESHOLD, 50, 400, 10);  // FIXED: max 400
        addSpinner(panel, "Reproduction Cooldown", "PREDATOR_REPRODUCTION_COOLDOWN", Config.PREDATOR_REPRODUCTION_COOLDOWN, 1, 1500, 10);  // FIXED: max 1500
        return panel;
    }

    private JPanel createEnvironmentPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 15, 8));
        panel.setBackground(Color.WHITE);
        addSpinner(panel, "Grass Regrowth Ticks", "GRASS_REGROWTH_TICKS", Config.GRASS_REGROWTH_TICKS, 1, 100, 1);
        addSpinner(panel, "Tick Duration (ms)", "TICK_DURATION_MS", Config.TICK_DURATION_MS, 10, 1000, 10);
        return panel;
    }

    private void addSpinner(JPanel panel, String label, String key, int value, int min, int max, int step) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        spinners.put(key, spinner);
        panel.add(jLabel);
        panel.add(spinner);
    }

    private void addDoubleSpinner(JPanel panel, String label, String key, double value, double min, double max, double step) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
        JSpinner spinner = new JSpinner(model);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.0");
        spinner.setEditor(editor);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        spinners.put(key, spinner);
        panel.add(jLabel);
        panel.add(spinner);
    }

    private void applyConfiguration() {
        Config.INITIAL_PREY_COUNT = (int) spinners.get("INITIAL_PREY_COUNT").getValue();
        Config.INITIAL_PREDATOR_COUNT = (int) spinners.get("INITIAL_PREDATOR_COUNT").getValue();
        Config.PREY_ENERGY_START = (int) spinners.get("PREY_ENERGY_START").getValue();
        Config.PREY_ENERGY_MAX = (int) spinners.get("PREY_ENERGY_MAX").getValue();
        Config.PREY_ENERGY_FROM_GRASS = (int) spinners.get("PREY_ENERGY_FROM_GRASS").getValue();
        Config.PREY_ENERGY_MOVE_COST = (double) spinners.get("PREY_ENERGY_MOVE_COST").getValue();
        Config.PREY_REPRODUCTION_THRESHOLD = (int) spinners.get("PREY_REPRODUCTION_THRESHOLD").getValue();
        Config.PREY_REPRODUCTION_COOLDOWN = (int) spinners.get("PREY_REPRODUCTION_COOLDOWN").getValue();
        Config.PREDATOR_ENERGY_START = (int) spinners.get("PREDATOR_ENERGY_START").getValue();
        Config.PREDATOR_ENERGY_MAX = (int) spinners.get("PREDATOR_ENERGY_MAX").getValue();
        Config.PREDATOR_ENERGY_FROM_PREY = (int) spinners.get("PREDATOR_ENERGY_FROM_PREY").getValue();
        Config.PREDATOR_ENERGY_MOVE_COST = (double) spinners.get("PREDATOR_ENERGY_MOVE_COST").getValue();
        Config.PREDATOR_REPRODUCTION_THRESHOLD = (int) spinners.get("PREDATOR_REPRODUCTION_THRESHOLD").getValue();
        Config.PREDATOR_REPRODUCTION_COOLDOWN = (int) spinners.get("PREDATOR_REPRODUCTION_COOLDOWN").getValue();
        Config.GRASS_REGROWTH_TICKS = (int) spinners.get("GRASS_REGROWTH_TICKS").getValue();
        Config.TICK_DURATION_MS = (int) spinners.get("TICK_DURATION_MS").getValue();
    }

    private void refreshSpinners() {
        spinners.get("INITIAL_PREY_COUNT").setValue(Config.INITIAL_PREY_COUNT);
        spinners.get("INITIAL_PREDATOR_COUNT").setValue(Config.INITIAL_PREDATOR_COUNT);
        spinners.get("PREY_ENERGY_START").setValue(Config.PREY_ENERGY_START);
        spinners.get("PREY_ENERGY_MAX").setValue(Config.PREY_ENERGY_MAX);
        spinners.get("PREY_ENERGY_FROM_GRASS").setValue(Config.PREY_ENERGY_FROM_GRASS);
        spinners.get("PREY_ENERGY_MOVE_COST").setValue(Config.PREY_ENERGY_MOVE_COST);
        spinners.get("PREY_REPRODUCTION_THRESHOLD").setValue(Config.PREY_REPRODUCTION_THRESHOLD);
        spinners.get("PREY_REPRODUCTION_COOLDOWN").setValue(Config.PREY_REPRODUCTION_COOLDOWN);
        spinners.get("PREDATOR_ENERGY_START").setValue(Config.PREDATOR_ENERGY_START);
        spinners.get("PREDATOR_ENERGY_MAX").setValue(Config.PREDATOR_ENERGY_MAX);
        spinners.get("PREDATOR_ENERGY_FROM_PREY").setValue(Config.PREDATOR_ENERGY_FROM_PREY);
        spinners.get("PREDATOR_ENERGY_MOVE_COST").setValue(Config.PREDATOR_ENERGY_MOVE_COST);
        spinners.get("PREDATOR_REPRODUCTION_THRESHOLD").setValue(Config.PREDATOR_REPRODUCTION_THRESHOLD);
        spinners.get("PREDATOR_REPRODUCTION_COOLDOWN").setValue(Config.PREDATOR_REPRODUCTION_COOLDOWN);
        spinners.get("GRASS_REGROWTH_TICKS").setValue(Config.GRASS_REGROWTH_TICKS);
        spinners.get("TICK_DURATION_MS").setValue(Config.TICK_DURATION_MS);
    }
}
