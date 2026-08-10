package de.samson_elektronik.model;

import java.time.LocalDate;

/**
 * Domänenmodell einer Aufgabe. Reine Datenklasse (POJO) - kennt
 * weder Datenbank noch GUI, das entspricht der Schichtentrennung
 * des Projekts (model / persistence / service / gui).
 */

public class Task {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;

    /**
     * Hauptkonstruktor. Wird u.a. beim Laden bestehender Aufgaben aus
     * der Datenbank genutzt (ID ist bereits bekannt). Enthält die
     * zentrale Validierung, alle anderen Konstruktoren rufen diesen
     * per this(...) auf, damit die Validierung nur einmal existiert.
     */
    public Task(int id, String title, String description, TaskStatus status, Priority priority, LocalDate dueDate) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Titel darf nicht leer sein");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    /**
     * Für Fälle, in denen der Status frei wählbar ist, aber noch keine
     * ID existiert (z.B. GUI-Dialog beim Neuanlegen). Die ID wird
     * später von der Datenbank vergeben (AUTOINCREMENT).
     */
    public Task(String title, String description, TaskStatus status, Priority priority, LocalDate dueDate) {
        this(0, title, description, status, priority, dueDate);
    }

    /**
     * Komfort-Konstruktor für den Standardfall: eine neue Aufgabe
     * beginnt immer mit Status TODO.
     */
    public Task(String title, String description, Priority priority, LocalDate dueDate) {
        this(title, description, TaskStatus.TODO, priority, dueDate);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Adapter Pattern: generische Schnittstelle für die Übersetzung
     * zwischen Task-Objekten und einem externen Textformat. Konkrete
     * Formate (z.B. CSV) implementieren dieses Interface.
     */
    public interface TaskExportAdapter {
        String export(Task task);
        Task importFrom(String line);
    }
}