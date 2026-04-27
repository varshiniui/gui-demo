package com.demo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class BadgePanel extends JPanel {

    private BadgePanel() {
        setOpaque(false);
        setLayout(new GridLayout(2, 2, 24, 24));
        setBorder(new EmptyBorder(60, 100, 60, 100));
        buildBadges();
    }

    private void buildBadges() {
        List<BadgeEngine.Badge> badges = BadgeEngine.evaluateBadges();
        for (BadgeEngine.Badge badge : badges) {
            add(new BadgeCard(badge));
        }
    }

    public void refresh() {
        removeAll();
        buildBadges();
        revalidate();
        repaint();
    }

    public static BadgePanel create() {
        return new BadgePanel();
    }

    // ────────────────────────────────────────────────────────────────────
    private static class BadgeCard extends JPanel {
        private final BadgeEngine.Badge badge;

        BadgeCard(BadgeEngine.Badge badge) {
            this.badge = badge;
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(28, 20, 20, 20));

            // Icon
            JLabel emoji = new JLabel(badge.type.emoji, SwingConstants.CENTER);
            emoji.setFont(new Font("Segoe UI Symbol, Apple Color Emoji, Noto Color Emoji", Font.PLAIN, 64));
            emoji.setAlignmentX(Component.CENTER_ALIGNMENT);
            emoji.setPreferredSize(new Dimension(100, 100));

            // Name
            JLabel name = new JLabel(badge.type.name);
            name.setFont(Theme.FONT_HEADING.deriveFont(Font.BOLD, 20f));
            name.setForeground(badge.unlocked ? Theme.TEXT_PRIMARY : new Color(180, 180, 180));
            name.setAlignmentX(Component.CENTER_ALIGNMENT);
            name.setBorder(new EmptyBorder(12, 0, 0, 0));

            // Description
            JLabel desc = new JLabel(badge.type.description);
            desc.setFont(Theme.FONT_SMALL.deriveFont(13f));
            desc.setForeground(badge.unlocked ? Theme.TEXT_SECONDARY : new Color(160, 160, 160));
            desc.setAlignmentX(Component.CENTER_ALIGNMENT);
            desc.setBorder(new EmptyBorder(6, 0, 0, 0));

            // Status
            JLabel status = new JLabel();
            status.setAlignmentX(Component.CENTER_ALIGNMENT);
            status.setBorder(new EmptyBorder(16, 0, 0, 0));
            status.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 12f));
            
            if (badge.unlocked) {
                status.setText("✓ UNLOCKED");
                status.setForeground(Theme.TEAL_MAIN);
            } else {
                status.setText("Locked");
                status.setForeground(new Color(160, 160, 160));
            }

            add(emoji);
            add(name);
            add(desc);
            add(Box.createVerticalGlue());
            add(status);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 18;

            // Shadow
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fillRoundRect(3, 5, w - 4, h - 4, arc, arc);

            // Background
            Color bg = badge.unlocked ? Color.WHITE : new Color(252, 252, 252);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // Border
            Color border = badge.unlocked ? Theme.TEAL_MAIN : new Color(220, 220, 220);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(badge.unlocked ? 2f : 1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            super.paintComponent(g);
        }
    }
}