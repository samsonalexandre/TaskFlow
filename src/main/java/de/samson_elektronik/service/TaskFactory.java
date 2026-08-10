package de.samson_elektronik.service;

import de.samson_elektronik.model.Priority;
import de.samson_elektronik.model.Task;

import java.time.LocalDate;

public class TaskFactory {
    public static Task createTask(String title, String description, Priority priority, LocalDate dueDate) {
        return new Task(title, description, priority, dueDate);
    }

    public static Task createQuickTask(String title) {
        return createTask(title, "", Priority.MEDIUM, LocalDate.now().plusWeeks(1));
    }
}
