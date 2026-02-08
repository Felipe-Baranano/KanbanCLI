package com.example.kanban_cli;

import com.example.kanban_cli.db.CollectionDAO;
import com.example.kanban_cli.model.Collection;

public class Context {
    private static Collection activeCollection;
    private static final CollectionDAO collectionDAO = new CollectionDAO();

    public static Collection getActiveCollection() {
        if (activeCollection == null) {
            loadActiveCollection();
        }
        return activeCollection;
    }

    public static boolean hasActiveCollection() {
        return activeCollection != null;
    }

    private static void loadActiveCollection() {
        activeCollection = collectionDAO.loadActiveCollection();
    }

    public static void clearActiveCollection() {
        collectionDAO.clearAllActive();
        activeCollection = null;
    }
}
