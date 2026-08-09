package de.samson_elektronik;

import de.samson_elektronik.model.Priority;
import de.samson_elektronik.model.Task;
import de.samson_elektronik.persistence.DatabaseManager;
import de.samson_elektronik.persistence.TaskRepository;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DatabaseManager db = DatabaseManager.getInstance();
        System.out.println("Verbindung erfolgreich: " + (db.getConnection() != null));

        TaskRepository repo = new TaskRepository();
        repo.save(new Task("Testaufgabe", "Beschreibung", Priority.MEDIUM, LocalDate.now()));
        System.out.println("Task gespeichert");

        List<Task> alle = repo.findAll();
        for (Task t : alle) {
            System.out.println(t.getId() + ": " + t.getTitle() + " [" + t.getStatus() + "]");
        }
    }
}
