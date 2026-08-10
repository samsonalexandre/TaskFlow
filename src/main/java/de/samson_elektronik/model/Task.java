package de.samson_elektronik.model;

import java.time.LocalDate;

public class Task {
    private int id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;

    // 1. Hauptkonstruktor (wird von der DB genutzt – enthält die Validierung)
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

    // 2. NEU: Konstruktor für GUI/Dialoge (ohne ID, aber mit frei wählbarem Status)
    public Task(String title, String description, TaskStatus status, Priority priority, LocalDate dueDate) {
        this(0, title, description, status, priority, dueDate);
    }

    // 3. Komfort-Konstruktor für neue Standard-Tasks (Status wird automatisch auf TODO gesetzt)
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

    public interface TaskExportAdapter {
        String export(Task task);
        Task importFrom(String line);
    }
}