package de.samson_elektronik.service;

import de.samson_elektronik.model.Priority;
import de.samson_elektronik.model.Task;
import de.samson_elektronik.model.TaskStatus;

import java.time.LocalDate;

public class CsvTaskAdapter implements Task.TaskExportAdapter {

    private static final String DELIMITER = ";";

    @Override
    public String export(Task task) {
        return task.getId() + DELIMITER +
                task.getTitle() + DELIMITER +
                task.getDescription() + DELIMITER +
                task.getStatus().name() + DELIMITER +
                task.getPriority().name() + DELIMITER +
                task.getDueDate();
    }

    @Override
    public Task importFrom(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }

        String[] parts = line.split(DELIMITER, -1);

        if (parts.length < 6) {
            throw new IllegalArgumentException("Ungültiges CSV-Format: " + line);
        }

        int id = Integer.parseInt(parts[0]);
        String title = parts[1];
        String description = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        Priority priority = Priority.valueOf(parts[4]);

        LocalDate dueDate = null;
        String dateString = parts[5];
        if (!dateString.equals("null") && !dateString.isBlank()) {
            dueDate = LocalDate.parse(dateString);
        }

        return new Task(id, title, description, status, priority, dueDate);
    }
}
