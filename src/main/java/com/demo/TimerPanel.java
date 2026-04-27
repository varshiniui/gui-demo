package com.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TimerPanel extends JPanel {

    public enum Mode {
        FLOW ("Flow",  25, 5,  "calm · steady · present"),
        GRIND("Grind", 50, 10, "intense · relentless · sharp");

        public final String label;
        public final int workMinutes;
        public final int breakMinutes;
        public final String tagline;

        Mode(String label, int work, int brk, String tagline) {
            this.label = label; this.workMinutes = work;
            this.breakMinutes = brk; this.tagline = tagline;
        }
    }

    public enum Phase { WORK, BREAK }

    private Mode    currentMode  = Mode.FLOW;
    private Phase   currentPhase = Phase.WORK;
    private boolean running      = false;
    private int     secondsLeft;
    private int     totalSeconds;
    private int     sessionCount = 0;

    private float   animatedFill = 0f;
    private float   targetFill   = 0f;

    private double  wave1Offset = 0.0;
    private double  wave2Offset = Math.PI * 0.7;
    private double  wave3Offset = Math.PI * 1.4;

    private static final Color[] WORK_DEEP  = { new Color(0x0D3B47), new Color(0x1A6B6B), new Color(0x3AAFA9) };
    private static final Color[] WORK_MID   = { new Color(0x2B7A78), new Color(0x3AAFA9), new Color(0x56C5BF) };
    private static final Color[] BREAK_DEEP = { new Color(0x7B2D00), new Color(0xBC542A), new Color(0xD97B3A) };
    private static final Color[] BREAK_MID  = { new Color(0xBC542A), new Color(0xD4A855), new Color(0xF0C880) };

    private final List<Bubble> bubbles = new ArrayList<>();
    private final List<Foam>   foams   = new ArrayList<>();
    private final Random rng = new Random();
    private javax.swing.Timer countdownTimer, animTimer;

    private JButton btnStartPause, btnReset, btnFlow, btnGrind;
    private JLabel  lblTagline, lblSessions, lblCurrentTask;
    private OceanCanvas oceanCanvas;
    private Runnable onSessionComplete;

    private static final Color PAUSE_COLOR = new Color(0x1A5C5A);
    private String currentTask = "";

    public TimerPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        resetToMode(currentMode);
        buildUI();
        startAnimLoop();
    }

    public void setOnSessionComplete(Runnable r) { this.onSessionComplete = r; }
    public int   getSessionCount()  { return sessionCount; }
    public Mode  getCurrentMode()   { return currentMode; }
    public Phase getCurrentPhase()  { return currentPhase; }
    public int   getSecondsLeft()   { return secondsLeft; }
    public boolean isRunning()      { return running; }

    public void setCurrentTask(String task) {
        this.currentTask = task;
        if (lblCurrentTask != null) {
            lblCurrentTask.setText(task.isEmpty() ? "" : "↳ " + task);
        }
    }

    public void startTimer() {
        if (!running) {
            toggleStartPause();
        }
    }

    private void buildUI() {
        setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JPanel topWrapper = new JPanel(new BorderLayout(0, 0));
        topWrapper.setOpaque(false);
        
        JPanel modePills = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        modePills.setOpaque(false);
        btnFlow  = buildModePill(Mode.FLOW);
        btnGrind = buildModePill(Mode.GRIND);
        modePills.add(btnFlow);
        modePills.add(btnGrind);
        updateModePills();
        
        lblCurrentTask = new JLabel("");
        lblCurrentTask.setFont(Theme.FONT_BODY.deriveFont(16f));
        lblCurrentTask.setForeground(Theme.TEXT_MUTED);
        lblCurrentTask.setHorizontalAlignment(SwingConstants.CENTER);
        
        topWrapper.add(modePills, BorderLayout.NORTH);
        topWrapper.add(lblCurrentTask, BorderLayout.SOUTH);
        
        add(topWrapper, BorderLayout.NORTH);

        oceanCanvas = new OceanCanvas();
        add(oceanCanvas, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnRow.setOpaque(false);

        btnReset      = buildPillButton("Reset",  Theme.BG_CARD,   Theme.TEXT_SECONDARY, 30);
        btnStartPause = buildPillButton("Start",  Theme.TEAL_MAIN, Color.WHITE,           50);

        btnReset.addActionListener(e -> resetSession());
        btnStartPause.addActionListener(e -> toggleStartPause());

        btnRow.add(btnReset);
        btnRow.add(btnStartPause);

        lblTagline = new JLabel(currentMode.tagline, SwingConstants.CENTER);
        lblTagline.setFont(Theme.FONT_SMALL);
        lblTagline.setForeground(Theme.TEXT_MUTED);

        lblSessions = new JLabel("Sessions today: 0", SwingConstants.CENTER);
        lblSessions.setFont(Theme.FONT_SMALL);
        lblSessions.setForeground(Theme.TEXT_MUTED);

        bottom.add(btnRow);
        bottom.add(Box.createVerticalStrut(15));
        bottom.add(wrap(lblTagline));
        bottom.add(wrap(lblSessions));
        add(bottom, BorderLayout.SOUTH);
    }

    // ─── Ocean Canvas ─────────────────────────────────────────────────────────
    private class OceanCanvas extends JPanel {
        OceanCanvas() {
            setOpaque(false);
            setPreferredSize(new Dimension(340, 340));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth(), h = getHeight();
            int diam = Math.min(w, h) - 60;
            int cx = w / 2, cy = h / 2;
            int ox = cx - diam / 2, oy = cy - diam / 2;

            // Soft outer glow
            for (int i = 6; i >= 1; i--) {
                Color glowCol = currentPhase == Phase.WORK
                    ? new Color(0x3AAFA9) : new Color(0xBC542A);
                int alpha = (int)(25.0 * (6 - i) / 5.0);
                g2.setColor(new Color(glowCol.getRed(), glowCol.getGreen(), glowCol.getBlue(), alpha));
                g2.setStroke(new BasicStroke(i * 2.5f));
                g2.drawOval(ox - i, oy - i, diam + i * 2, diam + i * 2);
            }

            // Drop shadow
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillOval(ox + 4, oy + 8, diam, diam);

            // Clip to circle
            Shape circle = new Ellipse2D.Float(ox, oy, diam, diam);
            g2.setClip(circle);

            // Sky background
            Color skyCol = currentPhase == Phase.WORK ? new Color(0xE8F4F3) : new Color(0xFFF3E0);
            g2.setColor(skyCol);
            g2.fillOval(ox, oy, diam, diam);

            drawAtmosphere(g2, ox, oy, diam, cx, cy);

            float ef = Math.max(0.02f, Math.min(0.98f, animatedFill));
            int waterTopY = oy + diam - (int)(ef * diam);

            // Layer 1: Deep solid base fill
            Color deepCol = lerpColorTriple(currentPhase == Phase.BREAK ? BREAK_DEEP : WORK_DEEP, ef);
            g2.setColor(deepCol);
            g2.fillRect(ox, waterTopY + 14, diam, oy + diam - waterTopY + 10);

            // Layer 2: Back wave (semi-transparent)
            Color midCol = lerpColorTriple(currentPhase == Phase.BREAK ? BREAK_MID : WORK_MID, ef);
            Color midBack = new Color(midCol.getRed(), midCol.getGreen(), midCol.getBlue(), 160);
            g2.setColor(midBack);
            g2.fill(buildOceanWave(ox, waterTopY + 10, diam, wave2Offset, 8f, 1.7, 80));

            // Layer 3: Front wave
            g2.setColor(midCol);
            g2.fill(buildOceanWave(ox, waterTopY, diam, wave1Offset, 11f, 1.5, 90));

            // Layer 4: Smooth foam highlight
            drawSmoothFoam(g2, ox, waterTopY, diam, wave1Offset);

            // Layer 5: Subtle inner surface gradient
            drawSurfaceGradient(g2, ox, waterTopY, diam, midCol);

            // Bubbles
            g2.setColor(new Color(255, 255, 255, 90));
            for (Bubble b : bubbles) {
                if (b.y > waterTopY) {
                    g2.fillOval((int)b.x - (int)b.r / 2, (int)b.y, (int)b.r, (int)b.r);
                }
            }

            g2.setClip(null);

            // Border ring
            g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.drawOval(ox, oy, diam, diam);

            // Time text
            String timeStr = formatTime(secondsLeft);
            Font timeFont = Theme.FONT_HEADING.deriveFont(Font.BOLD, 54f);
            g2.setFont(timeFont);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(timeStr)) / 2;
            int ty = cy + fm.getAscent() / 3;

            g2.setColor(new Color(0, 0, 0, 40));
            g2.drawString(timeStr, tx + 2, ty + 2);

            boolean submerged = cy < waterTopY + 20;
            g2.setColor(submerged ? Color.WHITE : Theme.TEXT_PRIMARY);
            g2.drawString(timeStr, tx, ty);

            String phaseLabel = currentPhase == Phase.WORK ? "WORK" : "BREAK";
            g2.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 11f));
            FontMetrics fm2 = g2.getFontMetrics();
            g2.setColor(submerged
                ? new Color(255, 255, 255, 180)
                : new Color(Theme.TEXT_MUTED.getRed(), Theme.TEXT_MUTED.getGreen(), Theme.TEXT_MUTED.getBlue(), 200));
            g2.drawString(phaseLabel, (w - fm2.stringWidth(phaseLabel)) / 2, ty + 22);

            g2.dispose();
        }

        private void drawAtmosphere(Graphics2D g2, int ox, int oy, int diam, int cx, int cy) {
            Point2D center = new Point2D.Float(cx, oy + diam * 0.3f);
            float radius = diam * 0.6f;
            Color atmCol = currentPhase == Phase.WORK ? new Color(0xDEF2F1) : new Color(0xFFE8C0);
            Color[] colors = { new Color(atmCol.getRed(), atmCol.getGreen(), atmCol.getBlue(), 55), new Color(0, 0, 0, 0) };
            float[] fracts = { 0f, 1f };
            RadialGradientPaint rgp = new RadialGradientPaint(center, radius, fracts, colors);
            g2.setPaint(rgp);
            g2.fillOval(ox, oy, diam, diam);
            g2.setPaint(null);
        }

        private void drawSmoothFoam(Graphics2D g2, int ox, int waterY, int diam, double offset) {
            Path2D foam = new Path2D.Float();
            int steps = diam;
            foam.moveTo(ox, waterY);
            for (int i = 0; i <= steps; i++) {
                double xR = (double) i / steps;
                double waveY = Math.sin(xR * Math.PI * 3.0 + offset) * 11
                             + Math.sin(xR * Math.PI * 5.5 + offset * 0.7) * 4;
                foam.lineTo(ox + i, waterY + waveY);
            }
            for (int i = steps; i >= 0; i--) {
                double xR = (double) i / steps;
                double waveY = Math.sin(xR * Math.PI * 3.0 + offset) * 11
                             + Math.sin(xR * Math.PI * 5.5 + offset * 0.7) * 4;
                foam.lineTo(ox + i, waterY + waveY - 4);
            }
            foam.closePath();
            g2.setColor(new Color(255, 255, 255, 70));
            g2.fill(foam);
        }

        private void drawSurfaceGradient(Graphics2D g2, int ox, int waterY, int diam, Color midCol) {
            int gradH = Math.min(60, diam / 4);
            GradientPaint gp = new GradientPaint(
                ox, waterY,          new Color(255, 255, 255, 50),
                ox, waterY + gradH,  new Color(255, 255, 255, 0)
            );
            g2.setPaint(gp);
            g2.fillRect(ox, waterY, diam, gradH);
            g2.setPaint(null);
        }
    }

    private Path2D buildOceanWave(int ox, int baseY, int width, double offset,
                                   float amp1, double freq, int depth) {
        Path2D p = new Path2D.Float();
        p.moveTo(ox, baseY);
        for (int i = 0; i <= width; i++) {
            double xR = (double) i / width;
            double y = Math.sin(xR * Math.PI * 2 * freq + offset)          * amp1
                     + Math.sin(xR * Math.PI * 2 * freq * 1.7 + offset * 1.3) * (amp1 * 0.4)
                     + Math.sin(xR * Math.PI * 2 * freq * 2.9 + offset * 0.6) * (amp1 * 0.2)
                     + Math.cos(xR * Math.PI * 2 * freq * 0.5 + offset * 0.4) * (amp1 * 0.3);
            p.lineTo(ox + i, baseY + y);
        }
        p.lineTo(ox + width, baseY + depth + 200);
        p.lineTo(ox,         baseY + depth + 200);
        p.closePath();
        return p;
    }

    private void startAnimLoop() {
        animTimer = new javax.swing.Timer(16, e -> {
            wave1Offset += running ? 0.055 : 0.025;
            wave2Offset += running ? 0.035 : 0.015;
            wave3Offset += running ? 0.020 : 0.008;

            float diff = targetFill - animatedFill;
            if (Math.abs(diff) > 0.0005f) animatedFill += diff * 0.04f;

            if (running) {
                if (bubbles.size() < 15 && rng.nextInt(30) == 0) {
                    int cx = oceanCanvas.getWidth() / 2;
                    bubbles.add(new Bubble(
                        cx + (rng.nextFloat() - 0.5f) * 100,
                        oceanCanvas.getHeight() - 20,
                        2 + rng.nextFloat() * 5,
                        0.5f + rng.nextFloat() * 1.2f
                    ));
                }
            }
            bubbles.removeIf(b -> {
                b.y -= b.speed;
                b.x += (float) Math.sin(b.y * 0.05) * 0.5f;
                return b.y < 40;
            });
            oceanCanvas.repaint();
        });
        animTimer.start();
    }

    private void toggleStartPause() {
        running = !running;
        if (running) {
            btnStartPause.setText("Pause");
            btnStartPause.setBackground(PAUSE_COLOR);
            countdownTimer = new javax.swing.Timer(1000, e -> {
                if (secondsLeft > 0) {
                    secondsLeft--;
                    targetFill = 1f - (float) secondsLeft / totalSeconds;
                } else {
                    handleComplete();
                }
            });
            countdownTimer.start();
        } else {
            btnStartPause.setText("Resume");
            btnStartPause.setBackground(Theme.TEAL_MAIN);
            if (countdownTimer != null) countdownTimer.stop();
        }
    }

    private void handleComplete() {
        if (countdownTimer != null) countdownTimer.stop();
        running = false;
        if (currentPhase == Phase.WORK) {
            sessionCount++;
            lblSessions.setText("Sessions today: " + sessionCount);
            if (onSessionComplete != null) SwingUtilities.invokeLater(onSessionComplete);
            currentPhase = Phase.BREAK;
            secondsLeft  = currentMode.breakMinutes * 60;
        } else {
            currentPhase = Phase.WORK;
            secondsLeft  = currentMode.workMinutes * 60;
        }
        totalSeconds = secondsLeft;
        targetFill   = 0f;
        animatedFill = 0f;
        btnStartPause.setText("Start");
        btnStartPause.setBackground(Theme.TEAL_MAIN);
        Toolkit.getDefaultToolkit().beep();
    }

    private void resetSession() {
        if (countdownTimer != null) countdownTimer.stop();
        running = false;
        resetToMode(currentMode);
        btnStartPause.setText("Start");
        btnStartPause.setBackground(Theme.TEAL_MAIN);
        if (lblTagline != null) lblTagline.setText(currentMode.tagline);
        currentTask = "";
        if (lblCurrentTask != null) lblCurrentTask.setText("");
    }

    public void resetToMode(Mode m) {
        currentMode  = m;
        currentPhase = Phase.WORK;
        secondsLeft  = m.workMinutes * 60;
        totalSeconds = secondsLeft;
        targetFill   = 0f;
        animatedFill = 0f;
    }

    private static Color lerpColorTriple(Color[] s, float t) {
        t = Math.max(0f, Math.min(1f, t));
        if (t <= 0.5f) return lerp(s[0], s[1], t * 2f);
        return lerp(s[1], s[2], (t - 0.5f) * 2f);
    }

    private static Color lerp(Color a, Color b, float t) {
        return new Color(
            clamp((int)(a.getRed()   + (b.getRed()   - a.getRed())   * t)),
            clamp((int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t)),
            clamp((int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t))
        );
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
    private String formatTime(int s) { return String.format("%02d:%02d", s / 60, s % 60); }
    private JPanel wrap(JLabel l) { JPanel p = new JPanel(); p.setOpaque(false); p.add(l); return p; }

    private JButton buildModePill(Mode m) {
        JButton b = new JButton(m.label);
        b.setFont(Theme.FONT_BODY);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.addActionListener(e -> {
            if (countdownTimer != null) countdownTimer.stop();
            running = false;
            resetToMode(m);
            updateModePills();
            if (lblTagline != null) lblTagline.setText(m.tagline);
            btnStartPause.setText("Start");
            btnStartPause.setBackground(Theme.TEAL_MAIN);
        });
        return b;
    }

    private void updateModePills() {
        stylePill(btnFlow,  currentMode == Mode.FLOW);
        stylePill(btnGrind, currentMode == Mode.GRIND);
    }

    private void stylePill(JButton b, boolean active) {
        b.setOpaque(active);
        if (active) {
            b.setBackground(Theme.TEXT_PRIMARY);
            b.setForeground(Color.WHITE);
        } else {
            b.setBackground(new Color(0, 0, 0, 0));
            b.setForeground(Theme.TEXT_MUTED);
        }
    }

    private JButton buildPillButton(String text, Color bg, Color fg, int hPad) {
        JButton b = new JButton(text);
        b.setFont(Theme.FONT_BODY);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, hPad, 10, hPad));
        return b;
    }

    private static class Bubble { 
        float x, y, r, speed; 
        Bubble(float x, float y, float r, float s) {
            this.x = x; this.y = y; this.r = r; this.speed = s;
        } 
    }
    
    private static class Foam { 
        float x, y, size, alpha; 
        Foam(float x, float y, float size) {
            this.x = x; this.y = y; this.size = size; this.alpha = 1f;
        } 
    }
}