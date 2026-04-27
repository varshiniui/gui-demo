package com.demo;

import java.awt.*;

public class Theme {
    // Backgrounds
    public static final Color BG_BASE      = new Color(0xF4F7F6); 
    public static final Color BG_CARD      = new Color(0xFFFFFF); // Added this to fix the error
    
    // Brand Colors
    public static final Color TEAL_MAIN    = new Color(0x3AAFA9);
    public static final Color TEAL_DARK    = new Color(0x2B7A78);
    public static final Color TEAL_LIGHT   = new Color(0xDEF2F1);
    public static final Color CORAL        = new Color(0xBC542A);
    
    // Text Colors
    public static final Color TEXT_PRIMARY   = new Color(0x17252A);
    public static final Color TEXT_SECONDARY = new Color(0x2B7A78);
    public static final Color TEXT_MUTED     = new Color(0x8E9A9B);
    public static final Color ACCENT_BLUE   = new Color(100, 180, 220);
    public static final Color ACCENT_GOLD   = new Color(255, 180, 60);
    public static final Color ACCENT_GREEN  = new Color(100, 200, 140);
    public static final Color ACCENT_PURPLE = new Color(180, 120, 220);
    // UI Elements
    public static final Color BORDER       = new Color(0xDAE8EB);

    // Fonts
    public static final Font FONT_HEADING  = new Font("Georgia", Font.BOLD, 22);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);
}