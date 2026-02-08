package com.example.kanban_cli.command;

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
        name = "list",
        description = "List collections or tasks"
)
public class ListCommand implements Runnable {

    @Parameters(
            index = "0",
            description = "Type: 'collection' or 'task'"
    )
    private String type;

    @Option(
            names = {"-s", "--status"},
            description = "Filter by status (todo ,doing , done)"
    )
    private String status;

    private static final List<String> VALID_STATUSES = List.of("todo", "in-progress", "done");

    @Override
    @SuppressWarnings("ConvertToStringSwitch")
    public void run() {
        try {

            // Validate status if provided
            if (status != null && !VALID_STATUSES.contains(status.toLowerCase().trim())) {
                System.err.println(
                        "Invalid status: " + status
                                + ". Use one of: todo, in-progress, done");
                return;
            }

            // Determine what to list based on the type parameter
            switch (type.toLowerCase().trim()) {
                case "collection" ->
                    handleCollections();
                case "task" ->
                    handleTasks();
                default ->
                    System.err.println("Invalid type. Use 'collection' or 'task'");
            }

        } catch (Exception e) {
            System.err.println("Error listing: " + e.getMessage());
        }
    }

    private void handleCollections() {
        CollectionDAO collectionDAO = new CollectionDAO();
        List<Collection> collections = collectionDAO.getAllCollections();

        if (collections.isEmpty()) {
            System.out.println("No collections found.");
        } else {
            System.out.println("Collections:");
            collections.forEach(System.out::println);
        }
    }

    private void handleTasks() {
        // Check if there is an active collection
        if (!Context.hasActiveCollection()) {
            System.err.println("No active collection. Use 'use <collection-name>' first.");
            return;
        }

        // Fetch tasks from the active collection
        TaskDAO taskDAO = new TaskDAO();
        List<Task> allTasks = taskDAO.getAllTasks();
        List<Task> allTasksByStatus = (status != null && !status.isBlank())
                ? taskDAO.getTasksByStatus(status.toLowerCase().trim())
                : null;

        int taskCount = (allTasksByStatus != null) ? allTasksByStatus.size() : allTasks.size();

        System.out.println(
                "All tasks in collection '" + Context.getActiveCollection().getName()
                + "': (" + taskCount + " Tasks)\n");

        if (status != null) {
            System.out.println("Filtered by status: " + status + "\n");
            printTasksByStatus(allTasks, status,
                    status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase());
        } else {

            // Print tasks grouped by status
            printTasksByStatus(allTasks, "todo", "Todo");
            printTasksByStatus(allTasks, "in-progress", "In-progress");
            printTasksByStatus(allTasks, "done", "Done");
        }
    }

    // Method to print tasks by status
    private void printTasksByStatus(List<Task> tasks, String statusKey, String statusLabel) {
        System.out.println("-- " + statusLabel + ":");
        boolean found = false;
        for (Task task : tasks) {
            if (statusKey.equalsIgnoreCase(task.getStatus())) {
                String indented = task.toString()
                        .replaceAll("(?m)^", "   ");
                System.out.print(indented);
                found = true;
            }
        }
        if (!found) {
            System.out.println("\n   No tasks found.");
        }
        System.out.println();
    }

}
