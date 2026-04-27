package com.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TimerTest {

    @Test
    void flowModeWorkDuration() {
        assertEquals(25, TimerPanel.Mode.FLOW.workMinutes);
    }

    @Test
    void flowModeBreakDuration() {
        assertEquals(5, TimerPanel.Mode.FLOW.breakMinutes);
    }

    @Test
    void grindModeWorkDuration() {
        assertEquals(50, TimerPanel.Mode.GRIND.workMinutes);
    }

    @Test
    void grindModeBreakDuration() {
        assertEquals(10, TimerPanel.Mode.GRIND.breakMinutes);
    }

    @Test
    void timeFormatTwoDigits() {
        // 90 seconds => "01:30"
        int s = 90;
        String result = String.format("%02d:%02d", s / 60, s % 60);
        assertEquals("01:30", result);
    }

    @Test
    void timeFormatZero() {
        String result = String.format("%02d:%02d", 0, 0);
        assertEquals("00:00", result);
    }
}