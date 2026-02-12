package com.example.kanban_cli.command;

import java.util.List;
import java.util.Scanner;
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
        name = "delete",
        description = "Delete tasks or collections"
)
public class DeleteCommand implements Runnable {

    private static final Set<String> VALID_STATUSES
            = Set.of("todo", "in_progress", "done");

    @Option(
            names = {"-a", "--all"},
            description = "Delete all items of the given type"
    )
    private boolean all;

    @Option(
            names = {"-s", "--status"},
            description = "Filter tasks by status (todo, in_progress, done). Only valid with 'task --all'."
    )
    private String status;

    @Parameters(
            index = "0",
            description = "Type to delete: task or collection"
    )
    private String type;

    @Parameters(
            index = "1",
            arity = "0..*",
            description = "Name of the task or collection"
    )
    private List<String> nameParts;

    private static final Scanner SCANNER = new Scanner(System.in);

    @Override
    public void run() {
        try {
            switch (type.toLowerCase()) {
                case "task" ->
                    deleteTask();
                case "collection" ->
                    deleteCollection();
                default ->
                    System.err.println("Invalid type. Use 'task' or 'collection'.");
            }
        } catch (Exception e) {
            System.err.println("Error deleting: " + e.getMessage());
        }
    }

    // collection deletion logic
    private void deleteCollection() {
        CollectionDAO collectionDAO = new CollectionDAO();
        TaskDAO taskDAO = new TaskDAO();

        // Validation checks
        if (status != null) {
            System.err.println("Option --status is not applicable when deleting collections.");
            return;
        }

        if (all) {
            if (!confirm("Delete ALL collections and their tasks? (y/n): ")) {
                System.out.println("Operation cancelled.");
                return;
            }

            for (Collection c : collectionDAO.getAllCollections()) {
                taskDAO.deleteByCollectionId(c.getId());
                collectionDAO.deleteCollection(c.getId());
            }

            System.out.println("All collections deleted.");
            return;
        }

        String name = getNameOrFail("Collection name is required.");
        if (name == null) {
            return;
        }

        Collection collection = collectionDAO.getCollectionByName(name);
        if (collection == null) {
            System.err.println("Collection '" + name + "' not found.");
            return;
        }

        if (!confirm("Delete collection '" + name + "' and all its tasks? (y/n): ")) {
            System.out.println("Operation cancelled.");
            return;
        }

        taskDAO.deleteByCollectionId(collection.getId());
        collectionDAO.deleteCollection(collection.getId());
        System.out.println("Collection '" + name + "' deleted.");
    }

    // task deletion logic
    private void deleteTask() {

        // Validation checks
        if (!Context.hasActiveCollection()) {
            System.err.println("No active collection. Use 'use <collection>' first.");
            return;
        }

        TaskDAO taskDAO = new TaskDAO();

        if (all) {
            deleteAllTasks(taskDAO);
            return;
        }

        String name = getNameOrFail("Task name is required.");
        if (name == null) {
            return;
        }

        Task task = taskDAO.getTaskByName(name);
        if (task == null) {
            System.err.println("Task with name '" + name + "' not found in the active collection.");
            return;
        }

        taskDAO.deleteTask(task.getId());
        System.out.println("Task '" + name + "' deleted.");
    }

    // Delete all tasks or all tasks with a specific status
    private void deleteAllTasks(TaskDAO taskDAO) {
        if (status != null) {
            String normalizedStatus = normalizeStatus(status);
            if (normalizedStatus == null) {
                return;
            }

            if (!confirm("Delete ALL tasks with status " + normalizedStatus + "? (y/n): ")) {
                System.out.println("Operation cancelled.");
                return;
            }

            taskDAO.deleteTasksByStatus(normalizedStatus);
            System.out.println("All tasks with status " + normalizedStatus + " deleted.");
            return;
        }

        if (!confirm("Delete ALL tasks in the current collection? (y/n): ")) {
            System.out.println("Operation cancelled.");
            return;
        }

        taskDAO.deleteByCollectionId(Context.getActiveCollection().getId());
        System.out.println("All tasks deleted.");
    }

    // Helper methods
    
    private String normalizeStatus(String value) {
        String normalized = value.trim().toLowerCase();

        if (!VALID_STATUSES.contains(normalized)) {
            System.err.println("Invalid status. Use: todo, in_progress, done.");
            return null;
        }
        return normalized;
    }

    private String getNameOrFail(String errorMessage) {
        if (nameParts == null || nameParts.isEmpty()) {
            System.err.println(errorMessage);
            return null;
        }
        return String.join(" ", nameParts);
    }

    private boolean confirm(String message) {
        while (true) {
            System.out.print(message);
            String input = SCANNER.nextLine().trim().toLowerCase();

            if (input.isEmpty() || input.equals("n") || input.equals("no")) {
                return false;
            }
            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
        }
    }
}
