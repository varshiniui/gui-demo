package com.demo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Calculator {

    public int add(int a, int b) { return a + b; }
    public int subtract(int a, int b) { return a - b; }
    public int multiply(int a, int b) { return a * b; }

    public void launchGUI() {
        JFrame frame = new JFrame("Simple Calculator");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField num1Field = new JTextField(5);
        JTextField num2Field = new JTextField(5);
        JLabel resultLabel = new JLabel("Result: ");
        JButton addBtn = new JButton("+");
        JButton subBtn = new JButton("-");
        JButton mulBtn = new JButton("*");

        addBtn.addActionListener(e -> {
            int a = Integer.parseInt(num1Field.getText());
            int b = Integer.parseInt(num2Field.getText());
            resultLabel.setText("Result: " + add(a, b));
        });
        subBtn.addActionListener(e -> {
            int a = Integer.parseInt(num1Field.getText());
            int b = Integer.parseInt(num2Field.getText());
            resultLabel.setText("Result: " + subtract(a, b));
        });
        mulBtn.addActionListener(e -> {
            int a = Integer.parseInt(num1Field.getText());
            int b = Integer.parseInt(num2Field.getText());
            resultLabel.setText("Result: " + multiply(a, b));
        });

        JPanel panel = new JPanel();
        panel.add(new JLabel("Num1:")); panel.add(num1Field);
        panel.add(new JLabel("Num2:")); panel.add(num2Field);
        panel.add(addBtn); panel.add(subBtn); panel.add(mulBtn);
        panel.add(resultLabel);

        frame.add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new Calculator().launchGUI();
    }
}