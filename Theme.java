package com.carparking.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Theme {
    // Color Palette
    public static final Color BG_MAIN = new Color(248, 250, 252);        // Slate 50
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_HEADER = new Color(15, 23, 42);         // Slate 900
    public static final Color BG_SIDEBAR = new Color(30, 41, 59);        // Slate 800

    public static final Color PRIMARY = new Color(37, 99, 235);          // Blue 600
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);    // Blue 700
    public static final Color SUCCESS = new Color(16, 185, 129);         // Emerald 500
    public static final Color SUCCESS_DARK = new Color(5, 150, 105);
    public static final Color SUCCESS_LIGHT = new Color(236, 253, 245);  // Emerald 50
    public static final Color DANGER = new Color(239, 68, 68);           // Red 500
    public static final Color DANGER_DARK = new Color(220, 38, 38);
    public static final Color DANGER_LIGHT = new Color(254, 242, 242);   // Red 50
    public static final Color WARNING = new Color(245, 158, 11);         // Amber 500
    public static final Color WARNING_LIGHT = new Color(254, 252, 232);

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);      // Slate 900
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105);   // Slate 600
    public static final Color TEXT_MUTED = new Color(148, 163, 184);     // Slate 400
    public static final Color BORDER = new Color(226, 232, 240);         // Slate 200

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    public static JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(fgColor);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER, Color.WHITE);
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    public static JLabel createBadge(String text, Color bgColor, Color fgColor) {
        JLabel badge = new JLabel(" " + text + " ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(fgColor);
        badge.setOpaque(false);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }

    public static String formatCurrency(double amount, String symbol) {
        return String.format("%s%.2f", symbol != null ? symbol : "$", amount);
    }
}
