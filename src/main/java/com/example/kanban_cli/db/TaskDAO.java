package com.example.kanban_cli.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.example.kanban_cli.Context;
import com.example.kanban_cli.model.Task;

public class TaskDAO {

    private final Database database;
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public TaskDAO() {
        this.database = Database.getInstance();
    }

    // CRUD operations for Task
    public void createTask(Task task) {
        String sql = "INSERT INTO tasks (name, status, due_date, created_at, collection_id) VALUES (?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, task.getName());
            pstmt.setString(2, task.getStatus());
            pstmt.setString(3, task.getDueDate() != null ? task.getDueDate().format(DATETIME) : null);
            pstmt.setString(4, now.format(DATETIME));
            pstmt.setInt(5, task.getCollectionId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                CollectionDAO collectionDAO = new CollectionDAO();
                collectionDAO.updateUpdatedAt(task.getCollectionId(), now);

            } else {
                System.err.println("Failed to create task '" + task.getName() + "'.");
            }

        } catch (SQLException e) {
            System.err.println("Error creating task: " + e.getMessage());
        }
    }

    public Task getTaskByName(String name) {
        String sql = "SELECT * FROM tasks WHERE LOWER(name) = LOWER(?) AND collection_id = ?";

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name.trim().toLowerCase());
            pstmt.setInt(2, Context.getActiveCollection().getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToTask(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error getting task by name: " + e.getMessage());
        }
        return null;
    }

    public List<Task> getAllTasks() {
        String sql = "SELECT * FROM tasks WHERE collection_id = ? ORDER BY created_at DESC";
        List<Task> tasks = new ArrayList<>();

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, Context.getActiveCollection().getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all tasks: " + e.getMessage());
        }
        return tasks;
    }

    public List<Task> getTasksByStatus(String status) {
        String sql = "SELECT * FROM tasks WHERE LOWER(status) = LOWER(?) AND collection_id = ? ORDER BY created_at DESC";
        List<Task> tasks = new ArrayList<>();

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, status.trim().toLowerCase());
            pstmt.setInt(2, Context.getActiveCollection().getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting tasks by status: " + e.getMessage());
        }
        return tasks;
    }

    public List<Task> getTasksByCollectionId(int collectionId) {
        String sql = "SELECT * FROM tasks WHERE collection_id = ? ORDER BY created_at DESC";
        List<Task> tasks = new ArrayList<>();

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, collectionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting tasks by collection id: " + e.getMessage());
        }
        return tasks;
    }

    public int getTaskCountByCollectionId(int collectionId) {
        String sql = "SELECT COUNT(*) AS cnt FROM tasks WHERE collection_id = ?";

        try (PreparedStatement stmt = database.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, collectionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("cnt");
            }

        } catch (SQLException e) {
            System.err.println("Error counting tasks: " + e.getMessage());
        }
        return 0;
    }

    public void updateTask(Task task) {
        String sql = "UPDATE tasks SET name = ?, status = ?, due_date = ?, updated_at = ? WHERE id = ?";

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, task.getName());
            pstmt.setString(2, task.getStatus());
            pstmt.setString(3, task.getDueDate() != null ? task.getDueDate().format(DATETIME) : null);
            pstmt.setString(4, LocalDateTime.now().format(DATETIME));
            pstmt.setInt(5, task.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating task: " + e.getMessage());
        }
    }

    public int moveAllByStatus(int collectionId, String fromStatus, String toStatus) {
        String sql = """
        UPDATE tasks
        SET status = ?
        WHERE collection_id = ?
          AND status = ?
    """;

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, toStatus);
            pstmt.setInt(2, collectionId);
            pstmt.setString(3, fromStatus);

            return pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error moving tasks: " + e.getMessage());
            return 0;
        }
    }

    public void deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error deleting task: " + e.getMessage());
        }
    }

    public void deleteByCollectionId(int collectionId) {
        String sql = "DELETE FROM tasks WHERE collection_id = ?";

        try (PreparedStatement pstmt = database.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, collectionId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println(
                    "Error deleting tasks from collection: " + e.getMessage());
        }
    }

    public void deleteTasksByStatus(String status) {
        List<Task> tasks = getTasksByStatus(status);

        for (Task task : tasks) {
            deleteTask(task.getId());
        }
    }

    // Map ResultSet to Task object
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task(rs.getString("name"));

        task.setId(rs.getInt("id"));
        task.setStatus(rs.getString("status"));
        task.setCollectionId(rs.getInt("collection_id"));

        // Parse dates
        if (rs.getString("due_date") != null) {
            task.setDueDate(LocalDateTime.parse(rs.getString("due_date"), DATETIME));
        }

        task.setCreatedAt(LocalDateTime.parse(rs.getString("created_at"), DATETIME));

        if (rs.getString("updated_at") != null) {
            task.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at"), DATETIME));
        }

        return task;
    }
}
