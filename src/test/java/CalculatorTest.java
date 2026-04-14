package com.demo;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CalculatorTest {

    Calculator calc = new Calculator();

    @Test
    void testAdd() {
        assertEquals(10, calc.add(4, 6));
    }

    @Test
    void testAddNegative() {
        assertEquals(-3, calc.add(-1, -2));
    }

    @Test
    void testSubtract() {
        assertEquals(5, calc.subtract(10, 5));
    }

    @Test
    void testMultiply() {
        assertEquals(12, calc.multiply(3, 4));
    }

    @Test
    void testMultiplyByZero() {
        assertEquals(0, calc.multiply(99, 0));
    }
}