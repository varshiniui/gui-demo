package com.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;  
import java.util.Map;  

public class StatsPanel extends JPanel {

    private JLabel lblSessionsVal;
    private JLabel lblWeekVal;
    private JLabel lblFocusVal;
    private DefaultPieDataset pieDataset;
    private StreakHeatmap heatmap;
    private int codingCount = 45, studyCount = 30, designCount = 25;

    public StatsPanel() {
        setOpaque(false);
        // ONE layout only — BorderLayout so the scroll pane fills properly
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));
        buildContent();
    }

    private void buildContent() {
        // Put everything in a scroll-capable inner panel using BoxLayout
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        inner.add(sectionLabel("TODAY"));
        inner.add(Box.createVerticalStrut(10));

        // ── Sessions + This Week row ────────────────────────────────────────
        JPanel metricRow1 = new JPanel(new GridLayout(1, 2, 12, 0));
        metricRow1.setOpaque(false);
        metricRow1.setAlignmentX(Component.LEFT_ALIGNMENT);
        metricRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        lblSessionsVal = new JLabel("0");
        lblWeekVal     = new JLabel("7");
        metricRow1.add(buildMiniCard(lblSessionsVal, "Sessions",  Theme.TEAL_MAIN));
        metricRow1.add(buildMiniCard(lblWeekVal,     "This Week", Theme.TEXT_SECONDARY));
        inner.add(metricRow1);
        inner.add(Box.createVerticalStrut(10));

        // ── Total focus time row ────────────────────────────────────────────
        JPanel metricRow2 = new JPanel(new GridLayout(1, 1));
        metricRow2.setOpaque(false);
        metricRow2.setAlignmentX(Component.LEFT_ALIGNMENT);
        metricRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        lblFocusVal = new JLabel("0m");
        metricRow2.add(buildWideCard(lblFocusVal, "TOTAL FOCUS TIME", Theme.CORAL));
        inner.add(metricRow2);
        inner.add(Box.createVerticalStrut(22));

        // ── Categories ─────────────────────────────────────────────────────
        inner.add(sectionLabel("CATEGORIES"));
        inner.add(Box.createVerticalStrut(8));
        inner.add(buildChartArea());
        inner.add(Box.createVerticalStrut(20));

        // ── Streak history ──────────────────────────────────────────────────
        inner.add(sectionLabel("STREAK HISTORY"));
        inner.add(Box.createVerticalStrut(8));

        heatmap = new StreakHeatmap();  // Just "heatmap =" without the type
        heatmap.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(heatmap);
        inner.add(Box.createVerticalStrut(20));

        // ── Quote card ──────────────────────────────────────────────────────
        inner.add(buildQuoteCard());
        inner.add(Box.createVerticalGlue());

        // Wrap in a scroll pane so it doesn't clip on small windows
        JScrollPane scroll = new JScrollPane(inner);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    public void updateStats(int todaySessions, int weekSessions, int totalFocusMinutes) {
        lblSessionsVal.setText(String.valueOf(todaySessions));
        lblWeekVal.setText(String.valueOf(weekSessions));
        int h = totalFocusMinutes / 60, m = totalFocusMinutes % 60;
        lblFocusVal.setText(h > 0 ? h + "h " + m + "m" : m + "m");
    }

    public void updateCategory(String cat, int delta) {
        switch (cat) {
            case "Coding" -> codingCount = Math.max(0, codingCount + delta);
            case "Study"  -> studyCount  = Math.max(0, studyCount  + delta);
            case "Design" -> designCount = Math.max(0, designCount + delta);
        }
        if (pieDataset != null) {
            pieDataset.setValue("Coding", codingCount);
            pieDataset.setValue("Study",  studyCount);
            pieDataset.setValue("Design", designCount);
        }
    }

    public void updateHeatmap(Map<LocalDate, Integer> data) {
        // pass real data to StreakHeatmap
        heatmap.setData(data);
        heatmap.repaint();
    }

    public void updateCategoryChart(Map<String, Integer> categoryMinutes) {
        if (pieDataset == null) return;
        pieDataset.clear();
        categoryMinutes.forEach((cat, mins) -> pieDataset.setValue(cat, mins));
    }


    private JPanel buildMiniCard(JLabel valLabel, String desc, Color accent) {
        JPanel card = new GlassCard();
        card.setLayout(new BorderLayout(0, 2));
        card.setBorder(BorderFactory.createCompoundBorder(
            new AccentBorder(accent, 3),
            BorderFactory.createEmptyBorder(12, 14, 10, 10)
        ));
        valLabel.setFont(Theme.FONT_HEADING.deriveFont(Font.BOLD, 28f));
        valLabel.setForeground(accent);
        JLabel descLabel = new JLabel(desc.toUpperCase());
        descLabel.setFont(Theme.FONT_SMALL.deriveFont(10f));
        descLabel.setForeground(Theme.TEXT_MUTED);
        card.add(valLabel,  BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);
        return card;
    }

    // ─── Wide metric card ─────────────────────────────────────────────────────
    private JPanel buildWideCard(JLabel valLabel, String desc, Color accent) {
        JPanel card = new GlassCard();
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            new AccentBorder(accent, 3),
            BorderFactory.createEmptyBorder(14, 16, 12, 16)
        ));
        valLabel.setFont(Theme.FONT_HEADING.deriveFont(Font.BOLD, 30f));
        valLabel.setForeground(accent);
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(Theme.FONT_SMALL.deriveFont(10f));
        descLabel.setForeground(Theme.TEXT_MUTED);
        JPanel textGroup = new JPanel();
        textGroup.setOpaque(false);
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));
        textGroup.add(valLabel);
        textGroup.add(descLabel);
        JLabel icon = new JLabel("🔥");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        card.add(textGroup, BorderLayout.CENTER);
        card.add(icon,      BorderLayout.EAST);
        return card;
    }

    // ─── Pie chart ────────────────────────────────────────────────────────────
    private JPanel buildChartArea() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        pieDataset = new DefaultPieDataset();
        pieDataset.setValue("Coding", codingCount);
        pieDataset.setValue("Study",  studyCount);
        pieDataset.setValue("Design", designCount);

        JFreeChart chart = ChartFactory.createPieChart(null, pieDataset, false, false, false);
        chart.setBackgroundPaint(new Color(0, 0, 0, 0));

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(0, 0, 0, 0));
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setInteriorGap(0.04);
        plot.setSectionPaint("Coding", Theme.TEXT_PRIMARY);
        plot.setSectionPaint("Study",  Theme.TEAL_MAIN);
        plot.setSectionPaint("Design", Theme.TEAL_LIGHT);
        plot.setLabelGenerator(null);
        plot.setSectionOutlinesVisible(false);

        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(300, 180));
        cp.setOpaque(false);
        cp.setBackground(new Color(0, 0, 0, 0));
        wrapper.add(cp, BorderLayout.CENTER);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.setOpaque(false);
        legend.add(legendDot(Theme.TEXT_PRIMARY, "Coding"));
        legend.add(legendDot(Theme.TEAL_MAIN,    "Study"));
        legend.add(legendDot(Theme.TEAL_LIGHT,   "Design"));
        wrapper.add(legend, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel legendDot(Color color, String name) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 2, 10, 10);
            }
        };
        dot.setPreferredSize(new Dimension(10, 14));
        dot.setOpaque(false);
        JLabel lbl = new JLabel(name);
        lbl.setFont(Theme.FONT_SMALL.deriveFont(12f));
        lbl.setForeground(Theme.TEXT_MUTED);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    // ─── Quote card ───────────────────────────────────────────────────────────
    private JPanel buildQuoteCard() {
        JPanel card = new GlassCard();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel quote = new JLabel(
            "<html><center><i>\"The ocean does not apologise<br>for its depth.\"</i></center></html>");
        quote.setFont(Theme.FONT_SMALL.deriveFont(Font.ITALIC, 13f));
        quote.setForeground(Theme.TEXT_PRIMARY);
        quote.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel streak = new JLabel("3-day streak", SwingConstants.CENTER);
        streak.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 12f));
        streak.setForeground(Theme.CORAL);

        card.add(quote,  BorderLayout.CENTER);
        card.add(streak, BorderLayout.SOUTH);
        return card;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 12f));
        lbl.setForeground(Theme.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // ─── Glass card ───────────────────────────────────────────────────────────
    private static class GlassCard extends JPanel {
        GlassCard() { setOpaque(false); setAlignmentX(Component.LEFT_ALIGNMENT); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 230));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            g2.setColor(new Color(Theme.BORDER.getRed(), Theme.BORDER.getGreen(), Theme.BORDER.getBlue(), 180));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, 12, 12));
            g2.dispose();
        }
    }

    // ─── Accent border ────────────────────────────────────────────────────────
    private static class AccentBorder implements javax.swing.border.Border {
        private final Color color;
        private final int   thickness;
        AccentBorder(Color c, int t) { color = c; thickness = t; }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            g2.fillRoundRect(x, y + 6, thickness, h - 12, thickness, thickness);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(0, thickness + 2, 0, 0); }
        @Override public boolean isBorderOpaque() { return false; }
    }
}