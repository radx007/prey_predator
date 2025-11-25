package com.reactive.preypredator.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Startup menu for selecting configuration mode
 */
public class StartupMenu extends JPanel {

    public StartupMenu(Runnable onDefaultConfig, Runnable onEditConfig) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 247));

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(52, 73, 94));
        titlePanel.setPreferredSize(new Dimension(0, 150));

        JPanel titleContent = new JPanel();
        titleContent.setLayout(new BoxLayout(titleContent, BoxLayout.Y_AXIS));
        titleContent.setOpaque(false);

        JLabel mainTitle = new JLabel("Lotka-Volterra Simulation");
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 36));
        mainTitle.setForeground(Color.WHITE);
        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Reactive Multi-Agent System");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle.setForeground(new Color(200, 200, 200));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleContent.add(Box.createVerticalGlue());
        titleContent.add(mainTitle);
        titleContent.add(Box.createRigidArea(new Dimension(0, 10)));
        titleContent.add(subtitle);
        titleContent.add(Box.createVerticalGlue());

        titlePanel.add(titleContent);
        add(titlePanel, BorderLayout.NORTH);

        // Center Panel with buttons
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(245, 245, 247));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(20, 0, 20, 0);

        JLabel promptLabel = new JLabel("How would you like to start?");
        promptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        promptLabel.setForeground(new Color(80, 80, 80));
        gbc.gridy = 0;
        centerPanel.add(promptLabel, gbc);

        // Default Config Button
        JButton defaultBtn = createMenuButton(
                "Start with Default Configuration",
                "Run immediately with pre-tuned parameters",
                new Color(46, 204, 113)
        );
        defaultBtn.addActionListener(e -> onDefaultConfig.run());
        gbc.gridy = 1;
        centerPanel.add(defaultBtn, gbc);

        // Edit Config Button
        JButton editBtn = createMenuButton(
                "Edit Configuration",
                "Customize parameters before running",
                new Color(52, 152, 219)
        );
        editBtn.addActionListener(e -> onEditConfig.run());
        gbc.gridy = 2;
        centerPanel.add(editBtn, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("JADE Framework | Fully Reactive Agents");
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footer.setForeground(new Color(140, 140, 140));
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(footer, BorderLayout.SOUTH);
    }

    private JButton createMenuButton(String mainText, String subText, Color bgColor) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(15, 10));
        button.setPreferredSize(new Dimension(450, 90));
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel main = new JLabel(mainText);
        main.setFont(new Font("Segoe UI", Font.BOLD, 16));
        main.setForeground(Color.WHITE);
        main.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel(subText);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(255, 255, 255, 220));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(main);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(sub);

        button.add(textPanel, BorderLayout.CENTER);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}
