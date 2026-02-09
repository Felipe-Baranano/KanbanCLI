package com.example.kanban_cli.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.kanban_cli.Context;
import com.example.kanban_cli.db.CollectionDAO;
import com.example.kanban_cli.db.TaskDAO;
import com.example.kanban_cli.model.Collection;
import com.example.kanban_cli.model.Task;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "new",
        description = "Create a new collection or task"
)
public class NewCommand implements Runnable {

    @Parameters(
            index = "0",
            description = "Type: 'collection' or 'task'"
    )
    private String type;

    @Parameters(
            index = "1",
            arity = "1..*",
            description = "Name"
    )
    private List<String> nameParts;

    @Option(
            names = {"-s", "--status"},
            description = "Task status (todo, in_progress, done)",
            defaultValue = "todo"
    )
    private String status;

    @Option(
            names = {"-d", "--due-date"},
            description = "Due date (format: dd/MM/yyyy)"
    )
    private String dueDate;

    @Override
    public void run() {
        try {
            String name = String.join(" ", nameParts);
            switch (type.toLowerCase().trim()) {
                case "collection" ->
                    handleCollection(name);
                case "task" ->
                    handleTask(name);
                default ->
                    System.err.println("Invalid type. Use 'collection <name>' or 'task <name>'");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Collection creation logic
    private void handleCollection(String collectionName) {
        CollectionDAO collectionDAO = new CollectionDAO();

        // Check if already exists
        if (collectionDAO.getCollectionByName(collectionName) != null) {
            System.err.println("Collection with name '" + collectionName + "' already exists.");
            return;
        }

        Collection collection = new Collection(collectionName);
        collectionDAO.createCollection(collection);
        System.out.println("Collection created successfully!");
        System.out.println(collection);
    }

    // Task creation logic
    private void handleTask(String taskName) {
        // Check if there is an active collection
        if (!Context.hasActiveCollection()) {
            System.err.println("No active collection. Use 'use <collection-name>' first.");
            return;
        }

        TaskDAO taskDAO = new TaskDAO();

        if (taskDAO.getTaskByName(taskName) != null) {
            System.err.println("Task with name '" + taskName + "' already exists in this collection.");
            return;
        }
        
        Task task = new Task(taskName);
        task.setCollectionId(Context.getActiveCollection().getId());

        // Set status if different from default
        if (!"todo".equals(status)) {
            task.setStatus(status);
        }

        // Set due date if provided
        if (dueDate != null) {

            try {
                DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate date = LocalDate.parse(dueDate, DATE);
                LocalDateTime parsedDate = date.atStartOfDay();

                if (parsedDate.isBefore(task.getCreatedAt())) {
                    System.err.println("Due date cannot be before the task creation date.");
                    return;
                }

                task.setDueDate(parsedDate);

            } catch (Exception e) {
                System.err.println("Invalid date format. Use dd/MM/yyyy.");
                return;
            }
        }
        
        // Save task to database
        taskDAO.createTask(task);
        System.out.println("Task created successfully!");
        System.out.println(task);
    }
}
