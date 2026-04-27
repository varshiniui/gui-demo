package com.demo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TaskPanel extends JPanel {

    private final DefaultListModel<TaskItem> listModel = new DefaultListModel<>();
    private final JList<TaskItem> taskList   = new JList<>(listModel);
    private final JTextField      taskInput  = new JTextField();

    private final List<String>  categories     = new ArrayList<>(List.of("Coding", "Study", "Design", "Reading"));
    private       String        selectedCategory = "Coding";

    private final List<JButton> catPills    = new ArrayList<>();
    private       JPanel        categoryRow;
    
    private OnStartFocusingListener onStartFocusingListener;

    public TaskPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 10));
        setPreferredSize(new Dimension(300, 0));

        buildHeader();
        buildList();
        buildInputArea();
    }

    private void buildHeader() {
        JLabel lblTitle = new JLabel("TASKS");
        lblTitle.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 13f));
        lblTitle.setForeground(Theme.TEXT_SECONDARY);
        add(lblTitle, BorderLayout.NORTH);
    }

    private void buildList() {
        taskList.setCellRenderer(new TaskCellRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setFixedCellHeight(70);
        taskList.setOpaque(false);
        taskList.setBackground(new Color(0, 0, 0, 0));

        // Mouse listener for checkbox and button clicks
        taskList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int idx = taskList.locationToIndex(e.getPoint());
                if (idx >= 0) {
                    TaskItem item = listModel.get(idx);
                    Rectangle cellBounds = taskList.getCellBounds(idx, idx);
                    if (cellBounds != null) {
                        int clickX = e.getX();
                        
                        // Left side (checkbox) - toggle done
                        if (clickX < 50) {
                            item.done = !item.done;
                            taskList.repaint();
                        }
                        // Right side (button area) - start focusing
                        else if (clickX > cellBounds.width - 180 && !item.done) {
                            if (onStartFocusingListener != null) {
                                onStartFocusingListener.onStartFocusing(idx, item.title, item.category);
                            }
                        }
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(taskList);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        add(scroll, BorderLayout.CENTER);

        listModel.addElement(new TaskItem("Deep focus session", "Coding",  false));
        listModel.addElement(new TaskItem("Review notes",       "Study",   false));
    }

    private void buildInputArea() {
        JPanel container = new JPanel(new BorderLayout(8, 8));
        container.setOpaque(false);

        categoryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 4));
        categoryRow.setOpaque(false);
        rebuildCategoryPills();

        taskInput.setFont(Theme.FONT_BODY.deriveFont(14f));
        taskInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(9, 10, 9, 10)
        ));
        taskInput.addActionListener(e -> addTask());

        JButton btnAdd = buildIconButton("+", Theme.TEAL_MAIN, Color.WHITE);
        btnAdd.addActionListener(e -> addTask());

        JPanel inputRow = new JPanel(new BorderLayout(6, 0));
        inputRow.setOpaque(false);
        inputRow.add(taskInput, BorderLayout.CENTER);
        inputRow.add(btnAdd,    BorderLayout.EAST);

        JTextField newCatField = new JTextField();
        newCatField.setFont(Theme.FONT_BODY.deriveFont(13f));
        newCatField.setToolTipText("New category name…");
        newCatField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JButton btnAddCat = buildIconButton("+ Category", new Color(0xEEF7F6), Theme.TEAL_MAIN);
        btnAddCat.setFont(Theme.FONT_SMALL.deriveFont(12f));

        Runnable doAddCategory = () -> {
            String name = newCatField.getText().trim();
            if (!name.isEmpty() && !categories.contains(name)) {
                categories.add(name);
                selectedCategory = name;
                rebuildCategoryPills();
                revalidate();
                repaint();
                newCatField.setText("");
            }
        };

        newCatField.addActionListener(e -> doAddCategory.run());
        btnAddCat.addActionListener(e -> doAddCategory.run());

        JPanel newCatRow = new JPanel(new BorderLayout(6, 0));
        newCatRow.setOpaque(false);
        newCatRow.add(newCatField, BorderLayout.CENTER);
        newCatRow.add(btnAddCat,   BorderLayout.EAST);

        container.add(categoryRow, BorderLayout.NORTH);
        container.add(inputRow,    BorderLayout.CENTER);
        container.add(newCatRow,   BorderLayout.SOUTH);

        add(container, BorderLayout.SOUTH);
    }

    private void rebuildCategoryPills() {
        categoryRow.removeAll();
        catPills.clear();
        for (String cat : categories) {
            JButton pill = buildCatPill(cat);
            catPills.add(pill);
            categoryRow.add(pill);
        }
        refreshPillStyles();
    }

    private void addTask() {
        String text = taskInput.getText().trim();
        if (!text.isEmpty()) {
            listModel.addElement(new TaskItem(text, selectedCategory, false));
            taskInput.setText("");
        }
    }

    private JButton buildCatPill(String name) {
        JButton b = new JButton(name);
        b.setFont(Theme.FONT_BODY.deriveFont(13f));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.TEAL_MAIN, 1),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        b.addActionListener(e -> {
            selectedCategory = name;
            refreshPillStyles();
        });
        return b;
    }

    private void refreshPillStyles() {
        for (JButton pill : catPills) {
            boolean active = pill.getText().equals(selectedCategory);
            pill.setBackground(active ? Theme.TEAL_MAIN : Color.WHITE);
            pill.setForeground(active ? Color.WHITE     : Theme.TEXT_MUTED);
        }
    }

    private JButton buildIconButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 16f));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return b;
    }

    public String getActiveCategory() { return selectedCategory; }
    
    public void setOnStartFocusingListener(OnStartFocusingListener listener) {
        this.onStartFocusingListener = listener;
    }
    
    public TaskItem getTaskAtIndex(int idx) {
        if (idx >= 0 && idx < listModel.size()) {
            return listModel.get(idx);
        }
        return null;
    }
    
    public void markTaskDone(int idx) {
        TaskItem item = getTaskAtIndex(idx);
        if (item != null) {
            item.done = true;
            taskList.repaint();
        }
    }

    // ─── Cell renderer ────────────────────────────────────────────────────────
    private class TaskCellRenderer extends JPanel implements ListCellRenderer<TaskItem> {
        private final JLabel lblCheck = new JLabel();
        private final JLabel lblText  = new JLabel();
        private final JLabel lblCat   = new JLabel();
        private final JLabel btnLabel = new JLabel("Start Focusing");

        TaskCellRenderer() {
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(8, 14, 8, 14));

            JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 3));
            textGroup.setOpaque(false);

            lblText.setFont(Theme.FONT_BODY.deriveFont(14f));
            lblCat.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 11f));

            textGroup.add(lblText);
            textGroup.add(lblCat);

            lblCheck.setFont(Theme.FONT_BODY.deriveFont(18f));
            lblCheck.setPreferredSize(new Dimension(24, 24));
            
            btnLabel.setFont(Theme.FONT_SMALL.deriveFont(11f));
            btnLabel.setBackground(Theme.TEAL_MAIN);
            btnLabel.setForeground(Color.WHITE);
            btnLabel.setOpaque(true);
            btnLabel.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            btnLabel.setPreferredSize(new Dimension(160, 30));
            btnLabel.setHorizontalAlignment(JLabel.CENTER);

            add(lblCheck,   BorderLayout.WEST);
            add(textGroup,  BorderLayout.CENTER);
            add(btnLabel,   BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends TaskItem> list, TaskItem value,
                int index, boolean isSelected, boolean cellHasFocus) {

            if (value.done) {
                lblText.setText("<html><strike>" + value.title + "</strike></html>");
                lblText.setForeground(Theme.TEXT_MUTED);
                lblCheck.setText("●");
                lblCheck.setForeground(Theme.TEAL_MAIN);
                btnLabel.setVisible(false);
            } else {
                lblText.setText(value.title);
                lblText.setForeground(Theme.TEXT_PRIMARY);
                lblCheck.setText("○");
                lblCheck.setForeground(Theme.TEXT_MUTED);
                btnLabel.setVisible(true);
            }

            lblCat.setText(value.category.toUpperCase());
            lblCat.setForeground(Theme.TEAL_MAIN);

            setBackground(isSelected ? Theme.TEAL_LIGHT : Color.WHITE);
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(8, 14, 8, 14)
            ));

            return this;
        }
    }
    
    // ─── Callback interface ───────────────────────────────────────────────────
    public interface OnStartFocusingListener {
        void onStartFocusing(int taskIndex, String taskTitle, String category);
    }
}