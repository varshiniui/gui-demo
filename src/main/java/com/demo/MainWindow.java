package com.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainWindow extends JFrame {

    private TimerPanel timerPanel;
    private TaskPanel  taskPanel;
    private StatsPanel statsPanel;
    private WeeklyReportPanel weeklyReportPanel;
    private int currentTaskIndex = -1;

    private JPanel cardPanel;
    private CardLayout cardLayout;

    private JButton btnTimer, btnTasks, btnStats, btnBadges, btnReport;

    public MainWindow() {
        super("FlowState");
        setSize(1200, 820);
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);
        initUI();
        TrayManager.get().init(this); // handles DO_NOTHING_ON_CLOSE internally
    }

    private void initUI() {
        JLayeredPane layered = new JLayeredPane();

        BackgroundCanvas bgCanvas = new BackgroundCanvas();
        bgCanvas.setBounds(0, 0, 1200, 820);
        layered.add(bgCanvas, JLayeredPane.DEFAULT_LAYER);

        JPanel content = buildContentPanel();
        content.setBounds(0, 0, 1200, 820);
        layered.add(content, JLayeredPane.PALETTE_LAYER);

        layered.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                bgCanvas.setBounds(0, 0, layered.getWidth(), layered.getHeight());
                content.setBounds(0, 0, layered.getWidth(), layered.getHeight());
            }
        });

        setContentPane(layered);
    }

    private JPanel buildContentPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);

        root.add(buildHeader(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        timerPanel = new TimerPanel();
        timerPanel.setOpaque(false);

        taskPanel = new TaskPanel();
        taskPanel.setOpaque(false);

        taskPanel.setOnStartFocusingListener((taskIndex, taskTitle, category) -> {
            currentTaskIndex = taskIndex;
            timerPanel.setCurrentTask(taskTitle + " (" + category + ")");
            showTab("TIMER");
            timerPanel.startTimer();
        });

        statsPanel = new StatsPanel();
        statsPanel.setOpaque(false);

        BadgePanel badgePanel = BadgePanel.create();
        badgePanel.setOpaque(false);

        weeklyReportPanel = new WeeklyReportPanel();
        weeklyReportPanel.setOpaque(false);

        cardPanel.add(wrapCentered(badgePanel,        800), "BADGES");
        cardPanel.add(wrapCentered(timerPanel,        560), "TIMER");
        cardPanel.add(wrapCentered(taskPanel,         700), "TASKS");
        cardPanel.add(wrapCentered(statsPanel,        900), "STATS");
        cardPanel.add(wrapCentered(weeklyReportPanel, 900), "REPORT");

        root.add(cardPanel, BorderLayout.CENTER);

        JLabel footer = new JLabel("FlowState · stay in the current", SwingConstants.CENTER);
        footer.setFont(Theme.FONT_SMALL);
        footer.setForeground(Theme.TEXT_MUTED);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 0, 14, 0));
        root.add(footer, BorderLayout.SOUTH);

        timerPanel.setOnSessionComplete(() -> {
            FocusDNADialog dialog = new FocusDNADialog(this);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            String category = taskPanel.getActiveCategory();
            int minutes     = timerPanel.getCurrentMode().workMinutes;
            int rating      = dialog.getSelectedRating();
            DataStore.get().recordSession(category, minutes, rating);

            if (currentTaskIndex >= 0) {
                taskPanel.markTaskDone(currentTaskIndex);
                currentTaskIndex = -1;
            }

            refreshStats();
            showNotification(timerPanel);
            TrayManager.get().notifySessionComplete(
                timerPanel.getCurrentMode().label,
                timerPanel.getCurrentMode().workMinutes
            );
        });

        showTab("TIMER");
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(26, 50, 0, 50));

        JLabel logo = new JLabel("FlowState");
        logo.setFont(Theme.FONT_HEADING.deriveFont(Font.BOLD, 26f));
        logo.setForeground(Theme.TEXT_PRIMARY);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        nav.setOpaque(false);

        btnTimer  = buildNavTab("Timer");
        btnTasks  = buildNavTab("Tasks");
        btnStats  = buildNavTab("Stats");
        btnBadges = buildNavTab("Badges");
        btnReport = buildNavTab("Report");

        btnTimer.addActionListener(e  -> showTab("TIMER"));
        btnTasks.addActionListener(e  -> showTab("TASKS"));
        btnStats.addActionListener(e  -> showTab("STATS"));
        btnBadges.addActionListener(e -> showTab("BADGES"));
        btnReport.addActionListener(e -> showTab("REPORT"));

        nav.add(btnTimer);
        nav.add(btnTasks);
        nav.add(btnStats);
        nav.add(btnBadges);
        nav.add(btnReport);

        JLabel clock = new JLabel();
        clock.setFont(Theme.FONT_BODY);
        clock.setForeground(Theme.TEXT_MUTED);
        new Timer(1000, e ->
            clock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")))
        ).start();

        header.add(logo,  BorderLayout.WEST);
        header.add(nav,   BorderLayout.CENTER);
        header.add(clock, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(header, BorderLayout.CENTER);

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        wrapper.add(sep, BorderLayout.SOUTH);

        return wrapper;
    }

    private JButton buildNavTab(String label) {
        JButton b = new JButton(label);
        b.setFont(Theme.FONT_BODY.deriveFont(13f));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        b.setForeground(Theme.TEXT_MUTED);
        return b;
    }

    private void showTab(String name) {
        cardLayout.show(cardPanel, name);

        for (JButton b : new JButton[]{btnTimer, btnTasks, btnStats, btnBadges, btnReport}) {
            b.setOpaque(false);
            b.setContentAreaFilled(false);
            b.setForeground(Theme.TEXT_MUTED);
            b.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        }

        JButton active = switch (name) {
            case "TIMER"  -> btnTimer;
            case "TASKS"  -> btnTasks;
            case "STATS"  -> btnStats;
            case "BADGES" -> btnBadges;
            case "REPORT" -> btnReport;
            default       -> btnTimer;
        };

        active.setForeground(Theme.TEXT_PRIMARY);
        active.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, Theme.TEAL_MAIN),
            BorderFactory.createEmptyBorder(8, 20, 6, 20)
        ));

        if (name.equals("REPORT")) weeklyReportPanel.updateReport();
    }

    private void refreshStats() {
        DataStore ds = DataStore.get();
        statsPanel.updateStats(
            ds.getTodaySessionCount(),
            ds.getWeekSessionCount(),
            ds.getTodayFocusMinutes()
        );
        statsPanel.updateHeatmap(ds.getHeatmapData(140));
        statsPanel.updateCategoryChart(ds.getCategoryMinutes());
        weeklyReportPanel.updateReport();
    }

    private void showNotification(TimerPanel tp) {
        boolean wasWork = tp.getCurrentPhase() == TimerPanel.Phase.BREAK;
        String title   = wasWork ? "Break time!" : "Back to work!";
        String message = wasWork
            ? "Great focus session! Time to rest."
            : "Break's over — let's get back in flow.";

        Toolkit.getDefaultToolkit().beep();

        JDialog toast = new JDialog(this, false);
        toast.setUndecorated(true);
        toast.setAlwaysOnTop(true);

        JPanel panel = new JPanel(new BorderLayout(12, 8));
        panel.setBackground(Theme.TEXT_PRIMARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.TEAL_MAIN, 2),
            BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Theme.FONT_HEADING.deriveFont(Font.BOLD, 16f));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblMsg = new JLabel(message);
        lblMsg.setFont(Theme.FONT_BODY.deriveFont(13f));
        lblMsg.setForeground(new Color(200, 230, 228));

        JButton btnDismiss = new JButton("Dismiss");
        btnDismiss.setFont(Theme.FONT_SMALL.deriveFont(12f));
        btnDismiss.setBackground(Theme.TEAL_MAIN);
        btnDismiss.setForeground(Color.WHITE);
        btnDismiss.setFocusPainted(false);
        btnDismiss.setBorderPainted(false);
        btnDismiss.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDismiss.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        btnDismiss.addActionListener(e -> toast.dispose());

        JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        textBlock.setOpaque(false);
        textBlock.add(lblTitle);
        textBlock.add(lblMsg);

        panel.add(textBlock,  BorderLayout.CENTER);
        panel.add(btnDismiss, BorderLayout.EAST);

        toast.setContentPane(panel);
        toast.pack();

        Point loc     = getLocation();
        Dimension wSz = getSize();
        Dimension tSz = toast.getPreferredSize();
        toast.setLocation(
            loc.x + wSz.width  - tSz.width  - 30,
            loc.y + wSz.height - tSz.height - 60
        );
        toast.setVisible(true);

        new Timer(6000, e -> toast.dispose()) {{ setRepeats(false); start(); }};
    }

    private JPanel wrapCentered(JPanel inner, int maxWidth) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.add(inner, BorderLayout.CENTER);
        return outer;
    }

    private static class BackgroundCanvas extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.BG_BASE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Theme.TEAL_LIGHT);
            Path2D wave = new Path2D.Float();
            wave.moveTo(0, 140);
            wave.curveTo(getWidth() * 0.3, 100, getWidth() * 0.7, 180, getWidth(), 120);
            wave.lineTo(getWidth(), 0);
            wave.lineTo(0, 0);
            wave.closePath();
            g2.fill(wave);
            g2.dispose();
        }
    }
}