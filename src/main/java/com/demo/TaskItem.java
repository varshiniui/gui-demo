// TaskItem.java
package com.demo;

public class TaskItem {
    public String  title;
    public String  category;
    public boolean done;

    public TaskItem(String title, String category, boolean done) {
        this.title    = title;
        this.category = category;
        this.done     = done;
    }
}