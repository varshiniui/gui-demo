package com.demo;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FocusDNADialog extends JDialog {
    private int selectedRating = 0;

    public FocusDNADialog(JFrame owner) {
        super(owner, "Session Complete", true);
        setSize(350, 250);
        setUndecorated(true);           // ← moved up, before content

        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 2));

        JLabel lblTitle = new JLabel("How was your focus?", SwingConstants.CENTER);
        lblTitle.setFont(Theme.FONT_HEADING.deriveFont(18f));
        lblTitle.setForeground(Theme.TEXT_PRIMARY);
        lblTitle.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel starPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        starPanel.setOpaque(false);
        for (int i = 1; i <= 5; i++) {
            starPanel.add(createRatingButton(i));
        }

        JLabel lblMsg = new JLabel("Your DNA evolves with every session.", SwingConstants.CENTER);
        lblMsg.setFont(Theme.FONT_SMALL);
        lblMsg.setForeground(Theme.TEXT_MUTED);
        lblMsg.setBorder(new EmptyBorder(0, 0, 20, 0));

        content.add(lblTitle, BorderLayout.NORTH);
        content.add(starPanel, BorderLayout.CENTER);
        content.add(lblMsg, BorderLayout.SOUTH);
        setContentPane(content);

        pack();                              // ← compute actual size first
        setLocationRelativeTo(owner);        // ← NOW center correctly
    }
    

    private JButton createRatingButton(int rating) {
        JButton btn = new JButton(String.valueOf(rating));
        btn.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD));
        btn.setPreferredSize(new Dimension(45, 45));
        btn.setFocusPainted(false);
        btn.setBackground(Theme.TEAL_LIGHT);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setBorder(BorderFactory.createLineBorder(Theme.TEAL_MAIN, 1));
        
        btn.addActionListener(e -> {
            selectedRating = rating;
            System.out.println("Session Focus Rating: " + selectedRating);
            dispose();
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(Theme.TEAL_MAIN);
                btn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Theme.TEAL_LIGHT);
                btn.setForeground(Theme.TEXT_SECONDARY);
            }
        });
        return btn;
    }

    // ── GETTER at class level ─────────────────────────────────────────────
    public int getSelectedRating() { return selectedRating; }
}