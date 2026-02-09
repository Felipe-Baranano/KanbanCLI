package com.example.kanban_cli.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.example.kanban_cli.Context;
import com.example.kanban_cli.db.TaskDAO;
import com.example.kanban_cli.model.Task;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "duedate",
        description = "Set or remove a due date from a task"
)
public class DueDateCommand implements Runnable {

    private static final DateTimeFormatter DATE_FORMAT
            = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Option(
            names = {"-r", "--remove"},
            description = "Remove the due date of the task"
    )
    private boolean removeDueDate;

    @Option(
            names = {"-s", "--set"},
            description = "Set a due date for a task (format: dd/MM/yyyy)"
    )
    private String setDueDate;

    @Parameters(
            index = "0..*",
            description = "Name of the task"
    )
    private List<String> nameParts;

    @Override
    public void run() {

        // Validation checks
        
        if (!Context.hasActiveCollection()) {
            System.err.println("No active collection. Use 'use <collection-name>' first.");
            return;
        }

        if (nameParts == null || nameParts.isEmpty()) {
            System.err.println("No task name specified.");
            return;
        }

        if (removeDueDate && setDueDate != null) {
            System.err.println("Cannot use --set and --remove together.");
            return;
        }

        if (!removeDueDate && setDueDate == null) {
            System.err.println("No changes specified. Use --set or --remove.");
            return;
        }

        // Validate the new due date format if we're setting a new date
        if (setDueDate != null
                && !setDueDate.matches("\\d{2}/\\d{2}/\\d{4}")) {

            System.err.println("""
                               Invalid value for --set.
                               Expected format: dd/MM/yyyy
                               Example: duedate <task-name> -s 12/12/2026""");
            return;
        }
        
        TaskDAO taskDAO = new TaskDAO();
        String taskName = String.join(" ", nameParts);
        Task task = taskDAO.getTaskByName(taskName);

        if (task == null) {
            System.err.println(
                    "Task with name '" + taskName + "' not found in the active collection."
            );
            return;
        }

        boolean updated = false;

        // Handle removing the due date
        if (removeDueDate) {
            if (task.getDueDate() == null) {
                System.out.println(
                        "Task '" + task.getName() + "' does not have a due date.");
                return;
            }

            task.setDueDate(null);
            updated = true;
        }

        
        // Handle setting a new due date
        if (setDueDate != null) {
            try {
                LocalDateTime newDueDate = LocalDate
                        .parse(setDueDate.trim(), DATE_FORMAT)
                        .atStartOfDay();

                if (newDueDate.equals(task.getDueDate())) {
                    System.err.println(
                            "Task already has due date " + setDueDate + ".");
                    return;
                }

                task.setDueDate(newDueDate);
                updated = true;

            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date format. Use dd/MM/yyyy.");
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
            }

        }

        // If we made changes, update the task in the database
        if (updated) {
            taskDAO.updateTask(task);
            System.out.println("Task updated successfully!");
            System.out.println(task);
        }
    }
}
