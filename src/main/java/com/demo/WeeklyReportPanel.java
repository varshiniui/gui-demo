package com.demo;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.time.*;
import java.util.*;

public class WeeklyReportPanel extends JPanel {

    private JPanel reportCardPanel;

    public WeeklyReportPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(30, 40, 30, 40));
        buildContent();
    }

    private void buildContent() {
        // ── Header ────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 28, 0));

        JLabel title = new JLabel("Weekly Report");
        title.setFont(Theme.FONT_HEADING.deriveFont(Font.BOLD, 30f));
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel weekRange = new JLabel(getWeekRangeText());
        weekRange.setFont(Theme.FONT_BODY.deriveFont(13f));
        weekRange.setForeground(Theme.TEXT_MUTED);

        // Decorative wave underline
        JPanel underline = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(Theme.TEAL_MAIN);
                Path2D wave = new Path2D.Float();
                int w = getWidth(), h = getHeight() / 2;
                wave.moveTo(0, h);
                for (int x = 0; x < w; x += 20) {
                    wave.curveTo(x + 5, h - 4, x + 10, h + 4, x + 20, h);
                }
                g2.draw(wave);
                g2.dispose();
            }
        };
        underline.setOpaque(false);
        underline.setPreferredSize(new Dimension(160, 10));

        titleRow.add(title, BorderLayout.WEST);

        JPanel subtitleCol = new JPanel();
        subtitleCol.setOpaque(false);
        subtitleCol.setLayout(new BoxLayout(subtitleCol, BoxLayout.Y_AXIS));
        subtitleCol.add(weekRange);
        subtitleCol.add(Box.createVerticalStrut(4));

        header.add(titleRow,    BorderLayout.NORTH);
        header.add(subtitleCol, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // ── Cards grid ────────────────────────────────────────────
        reportCardPanel = new JPanel(new GridLayout(2, 2, 18, 18));
        reportCardPanel.setOpaque(false);

        buildCards();

        JScrollPane scroll = new JScrollPane(reportCardPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // ── Footer motivational strip ──────────────────────────────
        JPanel footer = buildFooterStrip();
        add(footer, BorderLayout.SOUTH);
    }

    private void buildCards() {
        reportCardPanel.removeAll();

        reportCardPanel.add(buildStatCard(
            "Total Focus",
            formatHours(DataStore.get().getWeekFocusMinutes()),
            "hours this week",
            Theme.TEAL_MAIN,
            "⏱",
            buildMiniBarChart()
        ));

        reportCardPanel.add(buildStatCard(
            "Best Day",
            getBestDayText(),
            "highest session day",
            Theme.CORAL,
            "🏆",
            buildStreakDots()
        ));

        reportCardPanel.add(buildStatCard(
            "Top Category",
            DataStore.get().getTopCategory().isEmpty() ? "—" : DataStore.get().getTopCategory(),
            "most focused category",
            new Color(0x5B8CDB),
            "📂",
            buildCategoryBar()
        ));

        reportCardPanel.add(buildStatCard(
            "Badges Earned",
            String.valueOf(getBadgesEarnedThisWeek()),
            "achievements this week",
            new Color(0x9B72CF),
            "✦",
            buildBadgePips()
        ));

        reportCardPanel.revalidate();
        reportCardPanel.repaint();
    }

    // ── Individual stat card ──────────────────────────────────────
    private JPanel buildStatCard(String label, String value, String sub,
                                  Color accent, String icon, JPanel miniWidget) {
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background — soft white
                g2.setColor(new Color(255, 255, 255, 230));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                // Subtle border
                g2.setColor(new Color(Theme.BORDER.getRed(), Theme.BORDER.getGreen(),
                                      Theme.BORDER.getBlue(), 160));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 20, 20));

                // Accent color wash — top-left corner blob
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 18));
                g2.fillOval(-20, -20, 120, 120);

                // Bottom-right accent dot
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 12));
                g2.fillOval(getWidth() - 60, getHeight() - 60, 100, 100);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(22, 22, 18, 22));

        // ── Top row: icon pill + label ─────────────────────────────
        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setOpaque(false);

        // Icon pill
        JLabel iconPill = new JLabel(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconPill.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconPill.setOpaque(false);
        iconPill.setBorder(new EmptyBorder(4, 8, 4, 8));
        iconPill.setPreferredSize(new Dimension(40, 34));
        iconPill.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 10f));
        lbl.setForeground(Theme.TEXT_MUTED);

        topRow.add(iconPill, BorderLayout.WEST);
        topRow.add(lbl,      BorderLayout.CENTER);

        // ── Value ──────────────────────────────────────────────────
        JLabel valLabel = new JLabel(value);
        valLabel.setFont(Theme.FONT_HEADING.deriveFont(Font.BOLD, 38f));
        valLabel.setForeground(accent);
        valLabel.setBorder(new EmptyBorder(10, 0, 2, 0));

        // ── Sub text ───────────────────────────────────────────────
        JLabel subLabel = new JLabel(sub);
        subLabel.setFont(Theme.FONT_SMALL.deriveFont(12f));
        subLabel.setForeground(Theme.TEXT_MUTED);

        // ── Thin accent bar ────────────────────────────────────────
        JPanel accentBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 3, 3);
                g2.setColor(accent);
                // Fill ~60% as a visual indicator
                int filled = (int)(getWidth() * 0.6f);
                g2.fillRoundRect(0, 0, filled, getHeight(), 3, 3);
                g2.dispose();
            }
        };
        accentBar.setOpaque(false);
        accentBar.setPreferredSize(new Dimension(0, 4));

        // ── Centre content ─────────────────────────────────────────
        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.add(valLabel);
        centre.add(subLabel);
        centre.add(Box.createVerticalStrut(10));
        centre.add(accentBar);

        // ── Bottom mini-widget ─────────────────────────────────────
        if (miniWidget != null) {
            miniWidget.setBorder(new EmptyBorder(8, 0, 0, 0));
            centre.add(miniWidget);
        }

        card.add(topRow,  BorderLayout.NORTH);
        card.add(centre,  BorderLayout.CENTER);
        return card;
    }

    // ── Mini bar chart (focus hours per day) ──────────────────────
    private JPanel buildMiniBarChart() {
        Map<LocalDate, Integer> heatmap = DataStore.get().getHeatmapData(7);
        int[] vals = new int[7];
        int max = 1;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.minusDays(6 - i);
            vals[i] = heatmap.getOrDefault(d, 0);
            max = Math.max(max, vals[i]);
        }
        final int[] fVals = vals;
        final int   fMax  = max;
        final String[] days = {"M","T","W","T","F","S","S"};

        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight() - 14;
                int barW = (w - 6 * 4) / 7;
                for (int i = 0; i < 7; i++) {
                    int bh = fVals[i] == 0 ? 3 : (int)(((float)fVals[i] / fMax) * h);
                    int bx = i * (barW + 4);
                    int by = h - bh;
                    // Track
                    g2.setColor(new Color(Theme.BORDER.getRed(), Theme.BORDER.getGreen(), Theme.BORDER.getBlue(), 100));
                    g2.fillRoundRect(bx, 0, barW, h, 4, 4);
                    // Fill
                    boolean isToday = (i == 6);
                    g2.setColor(isToday ? Theme.TEAL_MAIN : new Color(Theme.TEAL_MAIN.getRed(), Theme.TEAL_MAIN.getGreen(), Theme.TEAL_MAIN.getBlue(), 140));
                    g2.fillRoundRect(bx, by, barW, bh, 4, 4);
                    // Day label
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    g2.setColor(Theme.TEXT_MUTED);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(days[i], bx + (barW - fm.stringWidth(days[i])) / 2, h + 12);
                }
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, 55));
        return p;
    }

    // ── Streak dots (7-day presence indicators) ───────────────────
    private JPanel buildStreakDots() {
        Map<LocalDate, Integer> heatmap = DataStore.get().getHeatmapData(7);
        boolean[] active = new boolean[7];
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            active[i] = heatmap.getOrDefault(today.minusDays(6 - i), 0) > 0;
        }
        final boolean[] fActive = active;

        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = 9, gap = 6;
                int totalW = 7 * (r * 2) + 6 * gap;
                int startX = (getWidth() - totalW) / 2;
                int cy = getHeight() / 2;
                final String[] days = {"M","T","W","T","F","S","S"};
                for (int i = 0; i < 7; i++) {
                    int cx = startX + i * (r * 2 + gap) + r;
                    if (fActive[i]) {
                        g2.setColor(Theme.CORAL);
                        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(days[i], cx - fm.stringWidth(days[i])/2, cy + fm.getAscent()/2 - 1);
                    } else {
                        g2.setColor(new Color(Theme.BORDER.getRed(), Theme.BORDER.getGreen(), Theme.BORDER.getBlue(), 150));
                        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                        g2.setColor(Theme.TEXT_MUTED);
                        g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(days[i], cx - fm.stringWidth(days[i])/2, cy + fm.getAscent()/2 - 1);
                    }
                }
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, 30));
        return p;
    }

    // ── Category horizontal bar ───────────────────────────────────
    private JPanel buildCategoryBar() {
        Map<String, Integer> cats = DataStore.get().getCategoryMinutes();
        int total = cats.values().stream().mapToInt(Integer::intValue).sum();
        final int fTotal = Math.max(total, 1);

        // Fixed 3 colours for up to 3 categories
        Color[] palette = {
            Theme.TEAL_MAIN,
            new Color(0x5B8CDB),
            new Color(0x9B72CF)
        };

        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), barH = 10;
                int y = (getHeight() - barH) / 2;

                // Background track
                g2.setColor(new Color(Theme.BORDER.getRed(), Theme.BORDER.getGreen(), Theme.BORDER.getBlue(), 120));
                g2.fillRoundRect(0, y, w, barH, barH, barH);

                // Fill segments
                int x = 0;
                int ci = 0;
                for (Map.Entry<String, Integer> e : cats.entrySet()) {
                    int segW = (int)((float)e.getValue() / fTotal * w);
                    g2.setColor(ci < palette.length ? palette[ci] : Theme.TEAL_LIGHT);
                    if (ci == 0) {
                        g2.fillRoundRect(x, y, segW, barH, barH, barH);
                    } else {
                        g2.fillRect(x, y, segW, barH);
                    }
                    x += segW;
                    ci++;
                    if (ci >= 3) break;
                }
                // Right-cap the last segment
                if (x > 0) {
                    g2.setColor(ci <= 1 ? palette[0] : palette[Math.min(ci-1, palette.length-1)]);
                    g2.fillRoundRect(x - barH, y, barH, barH, barH, barH);
                }
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, 22));
        return p;
    }

    // ── Badge pip row ─────────────────────────────────────────────
    private JPanel buildBadgePips() {
        int earned = getBadgesEarnedThisWeek();
        int total  = 4; // Early Bird, On Fire, Zen Master, No Mercy

        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = 8, gap = 8;
                int startX = 0;
                int cy = getHeight() / 2;
                String[] labels = {"🌅","🔥","🧘","⚡"};
                for (int i = 0; i < total; i++) {
                    int cx = startX + i * (r * 2 + gap) + r;
                    boolean has = i < earned;
                    if (has) {
                        // Glowing filled pip
                        g2.setColor(new Color(0x9B72CF, false));
                        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                        // Tiny inner white glow
                        g2.setColor(new Color(255, 255, 255, 80));
                        g2.fillOval(cx - r/2, cy - r/2 - 2, r - 2, r - 4);
                    } else {
                        g2.setColor(new Color(Theme.BORDER.getRed(), Theme.BORDER.getGreen(), Theme.BORDER.getBlue(), 130));
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                    }
                }
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, 26));
        return p;
    }

    // ── Motivational footer strip ─────────────────────────────────
    private JPanel buildFooterStrip() {
        JPanel strip = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(Theme.TEAL_MAIN.getRed(), Theme.TEAL_MAIN.getGreen(), Theme.TEAL_MAIN.getBlue(), 25),
                    getWidth(), 0, new Color(Theme.CORAL.getRed(), Theme.CORAL.getGreen(), Theme.CORAL.getBlue(), 15)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        strip.setOpaque(false);
        strip.setBorder(new EmptyBorder(14, 20, 14, 20));

        String[] quotes = {
            "\"The ocean does not apologise for its depth.\"",
            "\"Focus is the art of knowing what to ignore.\"",
            "\"Small sessions, massive results.\"",
            "\"Consistency beats intensity, every time.\""
        };
        int idx = (int)(Math.random() * quotes.length);

        JLabel quote = new JLabel("<html><i>" + quotes[idx] + "</i></html>");
        quote.setFont(Theme.FONT_SMALL.deriveFont(Font.ITALIC, 12f));
        quote.setForeground(Theme.TEXT_SECONDARY);
        quote.setHorizontalAlignment(SwingConstants.CENTER);

        // Streak pill
        int streak = DataStore.get().getWeekSessionCount();
        JLabel streakPill = new JLabel("  " + streak + " sessions this week  ");
        streakPill.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 11f));
        streakPill.setForeground(Color.WHITE);
        streakPill.setOpaque(true);
        streakPill.setBackground(Theme.TEAL_MAIN);
        streakPill.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.TEAL_DARK, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));

        strip.add(quote,      BorderLayout.CENTER);
        strip.add(streakPill, BorderLayout.EAST);
        strip.setBorder(new EmptyBorder(12, 0, 0, 0));
        return strip;
    }

    // ── Public refresh ────────────────────────────────────────────
    public void updateReport() {
        if (reportCardPanel != null) buildCards();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private String formatHours(int minutes) {
        int h = minutes / 60, m = minutes % 60;
        if (h == 0) return m + "m";
        return m == 0 ? h + "h" : h + "h " + m + "m";
    }

    private String getBestDayText() {
        Map<LocalDate, Integer> data = DataStore.get().getHeatmapData(7);
        if (data.isEmpty()) return "—";
        LocalDate best = LocalDate.now();
        int max = 0;
        for (Map.Entry<LocalDate, Integer> e : data.entrySet()) {
            if (e.getValue() > max) { max = e.getValue(); best = e.getKey(); }
        }
        // Short day name
        return switch (best.getDayOfWeek()) {
            case MONDAY    -> "Mon";
            case TUESDAY   -> "Tue";
            case WEDNESDAY -> "Wed";
            case THURSDAY  -> "Thu";
            case FRIDAY    -> "Fri";
            case SATURDAY  -> "Sat";
            case SUNDAY    -> "Sun";
        };
    }

    private int getBadgesEarnedThisWeek() {
        int count = 0;
        for (BadgeEngine.Badge b : BadgeEngine.evaluateBadges()) {
            if (b.unlocked) count++;
        }
        return count;
    }

    private String getWeekRangeText() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate end   = start.plusDays(6);
        var fmt = java.time.format.DateTimeFormatter.ofPattern("MMM d");
        return start.format(fmt) + " – " + end.format(fmt);
    }
}