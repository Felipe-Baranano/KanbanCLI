package com.example.kanban_cli.command;

import java.util.List;

import com.example.kanban_cli.db.CollectionDAO;
import com.example.kanban_cli.model.Collection;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
        name = "use",
        description = "Select a collection to work with"
)
public class UseCommand implements Runnable {

    @Parameters(
            index = "0",
            arity = "1..*",
            description = "Collection name"
    )
    private List<String> collectionNameParts;

    @Override
    public void run() {
        try {
            String collectionName = String.join(" ", collectionNameParts);
            CollectionDAO collectionDAO = new CollectionDAO();
            Collection collection = collectionDAO.getCollectionByName(collectionName);

            if (collection == null) {
                System.err.println("Collection '" + collectionName + "' not found.");
                System.out.println();
                System.out.println("Available collections:");
                collectionDAO.getAllCollections().forEach(System.out::println);

                if (collectionDAO.getAllCollections().isEmpty()) {
                    System.out.println("No collections found. Create a new collection using the 'new' command.");
                }

            } else if (collection.getIsActive()) {
                System.out.println("Collection '" + collection.getName() + "' is already active.");
                System.out.println(collection);
            } else {
                collectionDAO.clearAllActive();
                collectionDAO.updateIsActive(true, collection.getName());
                System.out.println("Now working with collection: " + collection.getName());
                System.out.println(collection);
            }

        } catch (Exception e) {
            System.err.println("Error selecting collection: " + e.getMessage());
        }
    }
}
