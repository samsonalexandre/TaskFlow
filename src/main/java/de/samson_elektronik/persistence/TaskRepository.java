package de.samson_elektronik.persistence;

 import de.samson_elektronik.model.Priority;
 import de.samson_elektronik.model.Task;
 import de.samson_elektronik.model.TaskStatus;

 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.time.LocalDate;
 import java.util.ArrayList;
 import java.util.List;

public class TaskRepository {
        public void save(Task task) {
            String sql = "INSERT INTO task(title, description, status, priority, due_date) VALUES(?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)) {
                stmt.setString(1, task.getTitle());
                stmt.setString(2, task.getDescription());
                stmt.setString(3, task.getStatus().name());
                stmt.setString(4, task.getPriority().name());
                stmt.setString(5, task.getDueDate().toString());
                stmt.executeUpdate();
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

                    LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));

                    Task task = new Task(id, title, description, status, priority, dueDate);
                    tasks.add(task);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Laden fehlgeschlagen");
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
                stmt.setString(5, task.getDueDate().toString());
                stmt.setInt(6, task.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Update fehlgeschlagen");
            }
        }

        public void deleteById(int id) {
            String sql = "DELETE FROM task WHERE id = ?";

            try (PreparedStatement stmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql)){
                // einen setInt(...)-Aufruf
                stmt.setInt(1, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Löschen fehlgeschlagen");
            }
        }
}
