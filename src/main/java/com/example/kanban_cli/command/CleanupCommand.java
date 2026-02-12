package com.example.kanban_cli.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.example.kanban_cli.Context;
import com.example.kanban_cli.db.TaskDAO;
import com.example.kanban_cli.model.Task;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "cleanup",
        description = "Remove expired tasks"
)
public class CleanupCommand implements Runnable {

    private static final Scanner SCANNER = new Scanner(System.in);

    private static final Set<String> VALID_STATUSES =
            Set.of("todo", "in_progress", "done");

    @Option(
            names = {"-a", "--all"},
            description = "Remove expired tasks from all statuses"
    )
    private boolean all;

    @Option(
            names = {"-s", "--status"},
            description = "Remove expired tasks from a specific status (todo, in_progress, done)"
    )
    private String status;

    @Override
    public void run() {

        if (!Context.hasActiveCollection()) {
            System.err.println("No active collection. Use 'use <collection>' first.");
            return;
        }

        if (!all && (status == null || status.isBlank())) {
            System.err.println("Specify --all or --status.");
            return;
        }

        if (all && status != null) {
            System.err.println("Cannot use --all and --status together.");
            return;
        }

        TaskDAO dao = new TaskDAO();
        int removed = 0;

        if (all) {

            if (!confirm("Remove ALL expired tasks from this collection? (y/n): ")) {
                System.out.println("Operation cancelled.");
                return;
            }

            for (String s : VALID_STATUSES) {
                removed += removeExpiredByStatus(dao, s);
            }

        } else {

            String normalized = status.trim().toLowerCase();

            if (!VALID_STATUSES.contains(normalized)) {
                System.err.println(
                        "Invalid status: " + status +
                        ". Valid values: todo, in_progress, done."
                );
                return;
            }

            if (!confirm("Remove expired tasks with status '" + normalized + "'? (y/n): ")) {
                System.out.println("Operation cancelled.");
                return;
            }

            removed = removeExpiredByStatus(dao, normalized);
        }

        System.out.println(removed + " expired task(s) removed.");
    }

    private int removeExpiredByStatus(TaskDAO dao, String status) {

        List<Task> tasks = dao.getTasksByStatus(status);
        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Task task : tasks) {
            if (task.getDueDate() != null &&
                now.isAfter(task.getDueDate())) {

                dao.deleteTask(task.getId());
                count++;
            }
        }

        return count;
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
