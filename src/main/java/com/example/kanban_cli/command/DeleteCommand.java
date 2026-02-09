package com.example.kanban_cli.command;

import java.util.List;
import java.util.Scanner;

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

    @Option(
            names = {"-a", "--all"},
            description = "Delete all items of the given type"
    )
    private boolean all;

    @Option(
            names = {"-s", "--status"},
            description = "Delete tasks with the given status (TODO, IN_PROGRESS, DONE). Only applies when type is 'task' and --all is specified."
    )
    private TaskStatus status;

    @Parameters(
            index = "0",
            description = "Type: task or collection"
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
                    handleTaskDelete();
                case "collection" ->
                    handleCollectionDelete();
                default ->
                    System.err.println("Invalid type. Use 'task' or 'collection'.");
            }
        } catch (Exception e) {
            System.err.println("Error deleting: " + e.getMessage());
        }
    }

    private void handleCollectionDelete() {
        CollectionDAO collectionDAO = new CollectionDAO();
        TaskDAO taskDAO = new TaskDAO();

        // If --all is specified, confirm and delete all collections and their tasks
        if (all) {

            if (status != null) {
                System.err.println("Status filter is not applicable when deleting collections. Ignoring --status option.");
            }
            if (!confirm("Delete ALL collections and their tasks? (y/n): ")) {
                System.out.println("Operation cancelled.");
                return;
            }

            List<Collection> collections = collectionDAO.getAllCollections();
            for (Collection c : collections) {
                taskDAO.deleteByCollectionId(c.getId());
                collectionDAO.deleteCollection(c.getId());
            }

            System.out.println("All collections deleted.");
            return;
        }

        // If not deleting all, collection name is required
        if (!all && (nameParts == null || nameParts.isEmpty())) {
            System.err.println("Collection name is required.");
            return;
        }

        String name = String.join(" ", nameParts);
        Collection collection = collectionDAO.getCollectionByName(name);

        if (collection == null) {
            System.err.println("Collection '" + name + "' not found.");
            return;
        }

        // If the collection is active, warn the user that it will be deleted
        if (!confirm("Delete collection '" + name + "' and all its tasks? (y/n): ")) {
            System.out.println("Operation cancelled.");
            return;
        }

        taskDAO.deleteByCollectionId(collection.getId());
        collectionDAO.deleteCollection(collection.getId());
        System.out.println("Collection '" + name + "' deleted.");
    }

    private void handleTaskDelete() {
        if (!Context.hasActiveCollection()) {
            System.err.println("No active collection. Use 'use <collection>' first.");
            return;
        }

        TaskDAO taskDAO = new TaskDAO();

        if (all) {

            // If status filter is provided, confirm and delete tasks with that status
            if (status != null) {
                if (!confirm("Delete ALL tasks with status " + status + "? (y/n): ")) {
                    return;
                }
                taskDAO.deleteTasksByStatus(status.name());
                System.out.println("All tasks with status " + status + " deleted.");
            } else { // If no status filter, confirm and delete all tasks in the active collection
                if (!confirm("Delete ALL tasks? (y/n): ")) {
                    return;
                }
                taskDAO.deleteByCollectionId(Context.getActiveCollection().getId());
                System.out.println("All tasks deleted.");
            }
            return;
        }

        // If not deleting all, task name is required
        if (!all && (nameParts == null || nameParts.isEmpty())) {
            System.err.println("Task name is required.");
            return;
        }

        String name = String.join(" ", nameParts);
        Task task = taskDAO.getTaskByName(name);

        if (task == null) {
            System.err.println("Task '" + name + "' not found.");
            return;
        }

        taskDAO.deleteTask(task.getId());
        System.out.println("Task '" + name + "' deleted.");
    }

    // Helper method to confirm destructive actions
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

    public enum TaskStatus {
        TODO,
        IN_PROGRESS,
        DONE;
    }
}
