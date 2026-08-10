package de.samson_elektronik.persistence;

import de.samson_elektronik.model.Priority;
import de.samson_elektronik.model.Task;
import de.samson_elektronik.model.TaskStatus;
import de.samson_elektronik.service.DependencyGraph;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    public void save(Task task) {
        String sql = "INSERT INTO task(title, description, status, priority, due_date) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = DatabaseManager.getInstance().getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getStatus().name());
            stmt.setString(4, task.getPriority().name());
            stmt.setString(5, task.getDueDate().toString());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getInt(1)); // Task-Objekt bekommt die echte ID zurück
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Speichern fehlgeschlagen");
        }
    }

    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM task";

        try (Statement stmt = DatabaseManager.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");

                TaskStatus status = TaskStatus.valueOf(rs.getString("status"));
                Priority priority = Priority.valueOf(rs.getString("priority"));

                String dueDateStr = rs.getString("due_date");
                LocalDate dueDate = dueDateStr != null ? LocalDate.parse(dueDateStr) : null;

                Task task = new Task(id, title, description, status, priority, dueDate);
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Laden fehlgeschlagen", e);
        }
        return tasks;
    }

    public void update(Task task) {
        String sql = "UPDATE task SET title = ?, description = ?, status = ?, priority = ?, due_date = ? WHERE id = ?";

        try (PreparedStatement stmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getStatus().name());
            stmt.setString(4, task.getPriority().name());
            stmt.setString(5, task.getDueDate() != null ? task.getDueDate().toString() : null);
            stmt.setInt(6, task.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Update fehlgeschlagen", e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM task WHERE id = ?";

        try (PreparedStatement stmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Löschen fehlgeschlagen", e);
        }
    }

    public void addDependency(int taskId, int dependsOnId) {
        String sql = "INSERT INTO task_dependency(task_id, depends_on_id) VALUES (?, ?)";

        try (PreparedStatement stmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, dependsOnId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Abhängigkeit speichern fehlgeschlagen", e);
        }
    }

    public void removeDependenciesForTask(int taskId) {
        String sql = "DELETE FROM task_dependency WHERE task_id = ?";

        try (PreparedStatement stmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Entfernen der Abhängigkeit fehlgeschlagen");
        }
    }

    public DependencyGraph loadDependencyGraph() {
        DependencyGraph graph = new DependencyGraph();

        String selectTasksSql = "SELECT id FROM task";
        String selectDependenciesSql = "SELECT task_id, depends_on_id FROM task_dependency";

        try (Statement stmt = DatabaseManager.getInstance().getConnection().createStatement()) {

            // 1) Alle Knoten (Task-IDs) laden und dem Graphen hinzufügen
            try (ResultSet rsTasks = stmt.executeQuery(selectTasksSql)) {
                while (rsTasks.next()) {
                    graph.addTask(rsTasks.getInt("id"));
                }
            }

            // 2) Alle Kanten (Abhängigkeiten) laden und verbinden
            try (ResultSet rsDeps = stmt.executeQuery(selectDependenciesSql)) {
                while (rsDeps.next()) {
                    int taskId = rsDeps.getInt("task_id");
                    int dependsOnId = rsDeps.getInt("depends_on_id");
                    graph.addDependency(taskId, dependsOnId);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Laden des Abhängigkeitsgraphen fehlgeschlagen", e);
        }

        return graph;
    }
}