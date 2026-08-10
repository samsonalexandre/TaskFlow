package de.samson_elektronik.service;

import de.samson_elektronik.model.Priority;
import de.samson_elektronik.model.Task;
import de.samson_elektronik.model.TaskStatus;

import java.time.LocalDate;

/**
 * Konkreter Adapter: übersetzt zwischen Task-Objekten und CSV-Zeilen
 * (semikolon-getrennt, da description Kommata enthalten könnte).
 * Weder Task noch die GUI müssen wissen, wie das CSV-Format aussieht.
 */
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

    /**
     * Wandelt eine CSV-Zeile zurück in ein Task-Objekt. Die ID aus
     * der Zeile wird beim anschließenden Speichern (TaskRepository.save)
     * ohnehin ignoriert - SQLite vergibt beim Import immer eine neue,
     * eigene ID.
     */
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
