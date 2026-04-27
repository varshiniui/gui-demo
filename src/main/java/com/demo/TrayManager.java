package com.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class TrayManager {

    private static TrayManager instance;
    private SystemTray  tray;
    private TrayIcon    trayIcon;
    private JFrame      mainWindow;
    private boolean     supported;

    // ── Singleton ─────────────────────────────────────────────────────────────
    public static TrayManager get() {
        if (instance == null) instance = new TrayManager();
        return instance;
    }

    private TrayManager() {
        supported = SystemTray.isSupported();
    }

    // ── Init — call once from MainWindow after frame is built ─────────────────
    public void init(JFrame frame) {
        this.mainWindow = frame;
        if (!supported) {
            System.out.println("System tray not supported on this platform.");
            return;
        }

        tray     = SystemTray.getSystemTray();
        trayIcon = new TrayIcon(buildTrayImage(), "FlowState", buildPopupMenu());
        trayIcon.setImageAutoSize(true);

        // Double-click tray icon → restore window
        trayIcon.addActionListener(e -> restoreWindow());

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("TrayManager init error: " + e.getMessage());
            supported = false;
        }

        // Override window close → minimize to tray instead of exit
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowIconified(WindowEvent e)  { minimizeToTray(); }
            @Override public void windowClosing(WindowEvent e)    { minimizeToTray(); }
        });
    }

    // ── Minimize to tray ──────────────────────────────────────────────────────
    public void minimizeToTray() {
        if (!supported || mainWindow == null) return;
        mainWindow.setVisible(false);
        showNotification("FlowState", "Running in background. Double-click to open.",
                         TrayIcon.MessageType.INFO);
    }

    // ── Restore window ────────────────────────────────────────────────────────
    public void restoreWindow() {
        if (mainWindow == null) return;
        SwingUtilities.invokeLater(() -> {
            mainWindow.setVisible(true);
            mainWindow.setExtendedState(JFrame.NORMAL);
            mainWindow.toFront();
            mainWindow.requestFocus();
        });
    }

    // ── Desktop notification ──────────────────────────────────────────────────
    public void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (!supported || trayIcon == null) return;
        trayIcon.displayMessage(title, message, type);
    }

    /** Convenience: called after a WORK session ends */
    public void notifySessionComplete(String modeName, int minutes) {
        showNotification(
            "✓ " + modeName + " session done!",
            minutes + " min focused. Time for a break 🌊",
            TrayIcon.MessageType.INFO
        );
    }

    /** Convenience: called when break ends */
    public void notifyBreakComplete() {
        showNotification(
            "Break over — back to flow!",
            "Open FlowState to start your next session.",
            TrayIcon.MessageType.NONE
        );
    }

    // ── Popup menu (right-click tray icon) ────────────────────────────────────
    private PopupMenu buildPopupMenu() {
        PopupMenu menu = new PopupMenu();

        MenuItem itemOpen = new MenuItem("Open FlowState");
        itemOpen.addActionListener(e -> restoreWindow());

        MenuItem itemQuit = new MenuItem("Quit");
        itemQuit.addActionListener(e -> {
            if (tray != null && trayIcon != null) tray.remove(trayIcon);
            System.exit(0);
        });

        menu.add(itemOpen);
        menu.addSeparator();
        menu.add(itemQuit);
        return menu;
    }

    // ── Programmatic tray icon (teal circle with "F") ─────────────────────────
    private Image buildTrayImage() {
        int size = 64;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Circle background
        g2.setColor(new Color(0x3AAFA9));
        g2.fillOval(2, 2, size - 4, size - 4);

        // "F" letter
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Georgia", Font.BOLD, 36));
        FontMetrics fm = g2.getFontMetrics();
        String letter = "F";
        g2.drawString(letter,
            (size - fm.stringWidth(letter)) / 2,
            (size + fm.getAscent() - fm.getDescent()) / 2 - 2
        );

        g2.dispose();
        return img;
    }

    public boolean isSupported() { return supported; }
}