package com.example.kanban_cli.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.example.kanban_cli.model.Collection;

public class CollectionDAO {

    private final Database database;
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TaskDAO taskDAO;

    public CollectionDAO() {
        this.database = Database.getInstance();
        this.taskDAO = new TaskDAO();
    }

    // CRUD operations for Collection
    public void createCollection(Collection collection) {
        String sql = "INSERT INTO collections (name, created_at, is_active, updated_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, collection.getName());
            pstmt.setString(2, collection.getCreatedAt().format(DATETIME));
            pstmt.setBoolean(3, collection.getIsActive());

            if (collection.getUpdatedAt() != null) {
                pstmt.setString(4, collection.getUpdatedAt().format(DATETIME));
            } else {
                pstmt.setString(4, null);
            }

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error creating collection: " + e.getMessage());
        }
    }

    public Collection getCollectionByName(String name) {
        String sql = "SELECT * FROM collections WHERE LOWER(name) = LOWER(?)";

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name.trim().toLowerCase());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCollection(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting collection by name: " + e.getMessage());
        }
        return null;
    }

    public List<Collection> getAllCollections() {
        String sql = "SELECT * FROM collections ORDER BY created_at DESC";
        List<Collection> collections = new ArrayList<>();

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                collections.add(mapResultSetToCollection(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all collections: " + e.getMessage());
        }
        return collections;
    }

    public void deleteCollection(int id) {
        String sql = "DELETE FROM collections WHERE id = ?";

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error deleting collection: " + e.getMessage());
        }
    }

    public void renameCollection(String oldName, String newName) {
        Collection existing = getCollectionByName(newName);
        if (existing != null) {
            System.err.println("Error: A collection with the name \"" + newName + "\" already exists.");
            return;
        }

        String sql = "UPDATE collections SET name = ? WHERE LOWER(name) = LOWER(?)";

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, oldName);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error renaming collection: " + e.getMessage());
        }
    }

    // Set a collection as active (is_active = true) and reset others to inactive
    public void updateIsActive(boolean bool, String name) {
        String resetSql = "UPDATE collections SET is_active = 0";
        String updateSql = "UPDATE collections SET is_active = ? WHERE LOWER(name) = LOWER(?)";

        try (PreparedStatement resetPstmt = database.getConnection().prepareStatement(resetSql); PreparedStatement updatePstmt = database.getConnection().prepareStatement(updateSql)) {

            // First, reset all collections to inactive
            resetPstmt.executeUpdate();

            // Then, set the specified collection to active
            updatePstmt.setBoolean(1, bool);
            updatePstmt.setString(2, name.trim().toLowerCase());
            updatePstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating active collection: " + e.getMessage());
        }
    }

    // Load the active collection (the one with is_active = true)
    public Collection loadActiveCollection() {
        String sql = "SELECT * FROM collections WHERE is_active = 1 LIMIT 1";

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCollection(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error getting active collection: " + e.getMessage());
        }
        return null;
    }

    // Disable all collections (set is_active to false)
    public void clearAllActive() {
        String sql = "UPDATE collections SET is_active = 0";

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error clearing active collections: " + e.getMessage());
        }
    }

    // Update the updated_at timestamp for a collection
    public void updateUpdatedAt(int collectionId, LocalDateTime dateTime) {
        String sql = "UPDATE collections SET updated_at = ? WHERE id = ?";

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, dateTime.format(DATETIME));
            pstmt.setInt(2, collectionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating collection updated_at: " + e.getMessage());
        }
    }

    // Map a ResultSet row to a Collection object
    private Collection mapResultSetToCollection(ResultSet rs) throws SQLException {
        Collection collection = new Collection(rs.getString("name"));
        collection.setId(rs.getInt("id"));
        collection.setCreatedAt(LocalDateTime.parse(rs.getString("created_at"), DATETIME));
        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null) {
            collection.setUpdatedAt(LocalDateTime.parse(updatedAtStr, DATETIME));
        }
        collection.setTasks(taskDAO.getTaskCountByCollectionId(collection.getId()));
        collection.setIsActive(rs.getBoolean("is_active"));
        return collection;
    }
}
