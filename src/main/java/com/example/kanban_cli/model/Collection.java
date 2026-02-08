package com.example.kanban_cli.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Collection {

    DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private String name;
    private int tasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;

    public Collection(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters with validations
    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        if (id != null && id <= 0) throw new IllegalArgumentException("Id must be positive");
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Collection name must not be null or empty");
        }
        this.name = name.trim();
    }

    public int getTasks() {
        return tasks;
    }

    public void setTasks(int tasks) {
        if (tasks < 0) throw new IllegalArgumentException("Tasks count cannot be negative");
        this.tasks = tasks;
    }

    public LocalDateTime getCreatedAt() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        if (updatedAt != null && this.createdAt != null && updatedAt.isBefore(this.createdAt)) {
            throw new IllegalArgumentException("Updated at cannot be before created at");
        }
        this.updatedAt = updatedAt;
    }
    
    public boolean getIsActive() {
        return this.isActive;
    }
    
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    // Override toString for better display in CLI
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("\n[Collection: \"").append(name).append("\"]");

        if (isActive) {
            sb.append(" (active)");
        }

        sb.append("\n ├─ Created at: ").append(createdAt.format(DATETIME));

        if (updatedAt != null) {
            sb.append("\n ├─ Updated at: ").append(updatedAt.format(DATETIME));
        }

        sb.append("\n └─ Tasks: ").append(tasks);

        return sb.toString();
    }

}
