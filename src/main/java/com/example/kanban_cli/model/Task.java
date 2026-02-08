package com.example.kanban_cli.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private String name;
    private String status;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int collectionId;

    public Task(String name) {
        this.name = name;
        this.status = "todo";
        this.createdAt = java.time.LocalDateTime.now();
    }

    // Getters and Setters with validation

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive");
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Task name must not be null or empty");
        }
        this.name = name.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status must not be null or empty");
        } else if (!status.equalsIgnoreCase("todo") &&
                   !status.equalsIgnoreCase("in-progress") &&
                   !status.equalsIgnoreCase("done")) {
            throw new IllegalArgumentException("Status must be one of: todo, in-progress, done");
        }
        this.status = status.trim().toLowerCase();
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        if (dueDate != null && this.createdAt != null && dueDate.isBefore(this.createdAt)) {
            throw new IllegalArgumentException("dueDate cannot be before createdAt");
        }
        this.dueDate = dueDate;
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
            throw new IllegalArgumentException("updatedAt cannot be before createdAt");
        }
        this.updatedAt = updatedAt;
    }

    public int getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        if (collectionId == null || collectionId <= 0)
            throw new IllegalArgumentException("collectionId must be positive");
        this.collectionId = collectionId;
    }

    
    // Override toString for better display in CLI
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("\n[Task: \"").append(name).append("\"]")
                .append("\n ├─ Status: ").append(status.toUpperCase());

        if (dueDate != null) {
            sb.append("\n ├─ Due Date:   ").append(dueDate.format(DATE));
        }

        if (updatedAt != null) {
            sb.append("\n └─ Updated at: ").append(updatedAt.format(DATETIME));
        }

        sb.append("\n └─ Created at: ").append(createdAt.format(DATETIME));


        sb.append("\n");

        return sb.toString();
    }

}
