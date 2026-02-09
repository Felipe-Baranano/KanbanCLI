package com.example.kanban_cli.command;

import java.util.List;

import com.example.kanban_cli.Context;
import com.example.kanban_cli.db.CollectionDAO;
import com.example.kanban_cli.db.TaskDAO;
import com.example.kanban_cli.model.Collection;
import com.example.kanban_cli.model.Task;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "rename",
        description = "Rename a collection or a task"
)
public class RenameCommand implements Runnable {

    @Parameters(
            index = "0",
            description = "Type: 'collection' or 'task'"
    )
    private String type;

    @Parameters(
            index = "1",
            description = "Current name of the collection or task"
    )
    private List<String> currentNameParts;

    @Parameters(
            index = "2..*",
            description = "New name"
    )
    private List<String> newNameParts;

    @Override
    public void run() {

        // Validation checks
        
         if (!Context.hasActiveCollection()) {
            System.err.println("No active collection. Use 'use <collection-name>' first.");
            return;
        }

        if (currentNameParts == null || currentNameParts.isEmpty()) {
            System.err.println("Current name not specified.");
            return;
        }
        if (newNameParts == null || newNameParts.isEmpty()) {
            System.err.println("New name not specified.");
            return;
        }

        String currentName = String.join(" ", currentNameParts);
        String newName = String.join(" ", newNameParts);

        switch (type.toLowerCase().trim()) {
            case "collection" ->
                renameCollection(currentName, newName);
            case "task" ->
                renameTask(currentName, newName);
            default ->
                System.err.println("Invalid type. Use 'collection' or 'task'.");
        }
    }

    public void renameCollection(String oldName, String newName) {
        CollectionDAO collectionDAO = new CollectionDAO();

        // Fetch the collection to be renamed
        Collection collection = collectionDAO.getCollectionByName(oldName);
        if (collection == null) {
            System.err.println("Error: Collection \"" + oldName + "\" not found.");
            return;
        }

        // Check if the new name is already taken by another collection
        Collection existing = collectionDAO.getCollectionByName(newName);
        if (existing != null) {
            System.err.println("Error: A collection with the name \"" + newName + "\" already exists.");
            return;
        }

        // update the collection name and save changes
        collection.setName(newName);
        collectionDAO.updateCollection(collection);

        System.out.println("Collection renamed successfully!");
        System.out.println(collection);
    }

    // Task renaming logic
    private void renameTask(String currentName, String newName) {

        if (!Context.hasActiveCollection()) {
            System.err.println("No active collection. Use 'use <collection-name>' first.");
            return;
        }

        TaskDAO taskDAO = new TaskDAO();
        
        Task task = taskDAO.getTaskByName(currentName);
        if (task == null) {
            System.err.println("Task '" + currentName + "' not found in the active collection.");
            return;
        }

        if (taskDAO.getTaskByName(newName) != null) {
            System.err.println("A task with name '" + newName + "' already exists in this collection.");
            return;
        }

        task.setName(newName);
        taskDAO.updateTask(task);

        System.out.println("Task renamed successfully!");
        System.out.println(task);
    }
}
