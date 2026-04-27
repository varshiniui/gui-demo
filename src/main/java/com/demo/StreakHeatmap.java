package com.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StreakHeatmap extends JPanel {
    private final int ROWS = 7;
    private final int COLS = 20;
    private final int BOX_SIZE = 12;
    private final int GAP = 4;
    
    // Real data: Date -> Session Count
    private Map<LocalDate, Integer> activityData = new HashMap<>();  // KEEP ONLY THIS ONE
    
    public StreakHeatmap() {
        setOpaque(false);
        setPreferredSize(new Dimension(COLS * (BOX_SIZE + GAP), ROWS * (BOX_SIZE + GAP) + 4));
    }
    
    // Setter for real data from DataStore
    public void setData(Map<LocalDate, Integer> data) {
        this.activityData = data;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int xOffset = 0;
        int yOffset = 0;
        LocalDate startDate = LocalDate.now().minusDays(ROWS * COLS);
        
        for (int c = 0; c < COLS; c++) {
            for (int r = 0; r < ROWS; r++) {
                LocalDate currentDate = startDate.plusDays(c * ROWS + r);
                int count = activityData.getOrDefault(currentDate, 0);
                
                g2.setColor(getHeatColor(count));
                
                RoundRectangle2D box = new RoundRectangle2D.Float(
                    xOffset + c * (BOX_SIZE + GAP),
                    yOffset + r * (BOX_SIZE + GAP),
                    BOX_SIZE, BOX_SIZE, 4, 4
                );
                g2.fill(box);
            }
        }
        
        g2.dispose();
    }
    
    private Color getHeatColor(int count) {
        if (count == 0) return Theme.BORDER;
        if (count == 1) return Theme.TEAL_LIGHT;
        if (count == 2) return new Color(0x72C2BF);
        if (count == 3) return Theme.TEAL_MAIN;
        return Theme.TEXT_SECONDARY;
    }
}