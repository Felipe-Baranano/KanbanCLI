package com.example.kanban_cli.command;

import java.util.List;
import java.util.Set;

import com.example.kanban_cli.Context;
import com.example.kanban_cli.db.CollectionDAO;
import com.example.kanban_cli.db.TaskDAO;
import com.example.kanban_cli.model.Collection;
import com.example.kanban_cli.model.Task;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "list",
        description = "List collections or tasks"
)
public class ListCommand implements Runnable {

    private static final Set<String> VALID_STATUSES
            = Set.of("todo", "in_progress", "done");

    @Parameters(
            index = "0",
            description = "Type to list: collection or task"
    )
    private String type;

    @Option(
            names = {"-s", "--status"},
            description = "Filter tasks by status (todo, in_progress, done)"
    )
    private String status;

    @Override
    public void run() {
        try {
            switch (type.toLowerCase().trim()) {
                case "collection" ->
                    listCollections();
                case "task" ->
                    listTasks();
                default ->
                    System.err.println("Invalid type. Use 'collection' or 'task'.");
            }
        } catch (Exception e) {
            System.err.println("Error listing: " + e.getMessage());
        }
    }

    // Collection listing logic
    private void listCollections() {
        CollectionDAO collectionDAO = new CollectionDAO();
        List<Collection> collections = collectionDAO.getAllCollections();

        if (collections.isEmpty()) {
            System.out.println("No collections found.");
            System.out.println("Create one using: kanban new collection <name>");
            return;
        }

        System.out.println("Collections:");
        collections.forEach(c -> System.out.println(" - " + c.getName()));
    }

    // Task listing logic with optional status filtering
    private void listTasks() {
        if (!Context.hasActiveCollection()) {
            System.err.println("No active collection. Use 'use <collection>' first.");
            return;
        }

        TaskDAO taskDAO = new TaskDAO();
        List<Task> tasks = taskDAO.getAllTasks();

        if (tasks.isEmpty()) {
            System.out.println("No tasks found in this collection.");
            return;
        }

        String normalizedStatus = normalizeStatus(status);

        System.out.println(
                "Tasks in collection '" + Context.getActiveCollection().getName() + "':\n"
        );

        if (normalizedStatus != null) {
            printTasksByStatus(tasks, normalizedStatus, formatLabel(normalizedStatus));
            return;
        }

        printTasksByStatus(tasks, "todo", "Todo");
        printTasksByStatus(tasks, "in_progress", "In Progress");
        printTasksByStatus(tasks, "done", "Done");
    }

    // Helper methods
    private String normalizeStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toLowerCase();

        if (!VALID_STATUSES.contains(normalized)) {
            System.err.println("Invalid status. Use: todo, in_progress, done.");
            return null;
        }
        return normalized;
    }

    private void printTasksByStatus(List<Task> tasks, String statusKey, String label) {
        System.out.println("-- " + label + ":");

        boolean found = false;
        for (Task task : tasks) {
            if (statusKey.equalsIgnoreCase(task.getStatus())) {
                System.out.println("   " + task);
                found = true;
            }
        }

        if (!found) {
            System.out.println("   No tasks found.");
        }
        System.out.println();
    }

    private String formatLabel(String status) {
        return switch (status) {
            case "todo" ->
                "Todo";
            case "in_progress" ->
                "In Progress";
            case "done" ->
                "Done";
            default ->
                status;
        };
    }
}
