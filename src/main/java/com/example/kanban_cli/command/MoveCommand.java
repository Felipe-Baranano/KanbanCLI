package com.example.kanban_cli.command;

import java.util.List;
import java.util.Scanner;

import com.example.kanban_cli.Context;
import com.example.kanban_cli.db.TaskDAO;
import com.example.kanban_cli.model.Task;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "move",
    description = "Move one task or all tasks from one status to another"
)
public class MoveCommand implements Runnable {

    @Option(
        names = {"-a", "--all"},
        description = "Move all tasks with a given status"
    )
    private boolean all;

    @Parameters(index = "0", description = "Status or new status")
    private String first;

    @Parameters(
        index = "1..*",
        description = "Task name or new status"
    )
    private List<String> rest;

    private static final Scanner SCANNER = new Scanner(System.in);

    @Override
    public void run() {

        if (!Context.hasActiveCollection()) {
            System.err.println("No active collection. Use 'use <collection-name>' first.");
            return;
        }

        TaskDAO taskDAO = new TaskDAO();

        if (all) {
            // move --all <fromStatus> <toStatus>
            if (rest == null || rest.size() != 1) {
                System.err.println("Usage: move --all <fromStatus> <toStatus>");
                return;
            }

            TaskStatus fromStatus = parseStatus(first);
            TaskStatus toStatus = parseStatus(rest.get(0));

            if (fromStatus == null || toStatus == null) {
                System.err.println("Invalid status. Use: todo, in_progress, done.");
                return;
            }

            if (fromStatus == toStatus) {
                System.err.println("Source and destination status are the same.");
                return;
            }

            if (!confirm(
                "Move ALL tasks from '" + fromStatus +
                "' to '" + toStatus + "'? (y/n): "
            )) {
                System.out.println("Operation cancelled.");
                return;
            }

            int count = taskDAO.moveAllByStatus(
                Context.getActiveCollection().getId(),
                fromStatus.name(),
                toStatus.name()
            );

            System.out.println(
                count + " task(s) moved from '" + fromStatus +
                "' to '" + toStatus + "'."
            );

        } else {
            // move <newStatus> <task name...>
            if (rest == null || rest.isEmpty()) {
                System.err.println("Usage: move <newStatus> <task name>");
                return;
            }

            TaskStatus newStatus = parseStatus(first);
            if (newStatus == null) {
                System.err.println("Invalid status. Use: todo, in_progress, done.");
                return;
            }

            String taskName = String.join(" ", rest);

            Task task = taskDAO.getTaskByName(taskName);
            if (task == null) {
                System.err.println(
                    "Task with name '" + taskName + "' not found in the active collection."
                );
                return;
            }

            if (task.getStatus().equalsIgnoreCase(newStatus.name())) {
                System.err.println(
                    "Task '" + taskName + "' is already in status '" + newStatus + "'."
                );
                return;
            }

            try {
                task.setStatus(newStatus.name());
                taskDAO.updateTask(task);

                System.out.println(
                    "Task '" + task.getName() + "' moved to '" + newStatus + "'."
                );
                System.out.println(task);

            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    // Helper methods

    private TaskStatus parseStatus(String value) {
        if (value == null) return null;

        try {
            return TaskStatus.valueOf(value.trim().toLowerCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
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

    public enum TaskStatus {
        todo,
        in_progress,
        done;
    }
}

