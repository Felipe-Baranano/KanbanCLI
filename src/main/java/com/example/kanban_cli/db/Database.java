package com.example.kanban_cli.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:app.db";
    private static Database instance;
    private Connection connection;

    // Private constructor to prevent instantiation
    private Database() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();

        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
        }
    }

    // Public method to provide access to the singleton instance
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    // Create tables if they don't exist
    private void createTables() {
        String createCollectionsTable = """
            CREATE TABLE IF NOT EXISTS collections (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                created_at TEXT NOT NULL,
                is_active BOOLEAN NOT NULL
            )
            """;

        String createTasksTable = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'todo',
                due_date TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT,
                collection_id INTEGER NOT NULL,
                FOREIGN KEY (collection_id) REFERENCES collections (id)
            )
            """;

        // Execute the table creation statements
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createCollectionsTable);
            stmt.execute(createTasksTable);
            
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    // Method to close the database connection
    public void close() {
        try {
            if (connection != null) {
                connection.close();
                
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}
