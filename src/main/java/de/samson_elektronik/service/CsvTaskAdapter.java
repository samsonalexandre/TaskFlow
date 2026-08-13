package de.samson_elektronik.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import de.samson_elektronik.model.Priority;
import de.samson_elektronik.model.Task;
import de.samson_elektronik.model.TaskStatus;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Konkreter Adapter: übersetzt zwischen Task-Objekten und CSV
 * (semikolon-getrennt). Nutzt die Bibliothek OpenCSV statt manuellem
 * String.split() - aus zwei Gründen:
 *
 * 1. Korrektheit: OpenCSV setzt alle Felder in Anführungszeichen und
 *    escapet Sonderzeichen. Semikolons, Anführungszeichen und sogar
 *    Zeilenumbrüche INNERHALB von Titel/Beschreibung bleiben dadurch
 *    Teil des Feldes, statt die Spalten zu verschieben.
 *
 * 2. Sicherheit: Ohne Quoting könnte ein eingeschleustes Semikolon die
 *    Spaltenstruktur manipulieren (CSV-Injection) und so z.B. einen
 *    manipulierten Status oder eine fremde Priorität "hineinschieben".
 *    Mit Quoting ist der Feldinhalt strikt von der Struktur getrennt.
 *
 * Weder Task noch die GUI müssen wissen, wie das CSV-Format aussieht.
 */
public class CsvTaskAdapter implements Task.TaskExportAdapter {

    // Semikolon statt Komma, damit sich exportierte Dateien in
    // deutschsprachigem Excel direkt öffnen lassen (dort ist ';' Standard)
    private static final char DELIMITER = ';';

    // Anzahl der Spalten: id, title, description, status, priority, due_date
    private static final int COLUMN_COUNT = 6;

    /**
     * Schreibt alle Aufgaben als CSV. CSVWriter übernimmt Quoting und
     * Escaping automatisch - hier muss nur noch jedes Task-Objekt in
     * ein String-Array (eine CSV-Zeile) zerlegt werden.
     */
    @Override
    public void export(List<Task> tasks, Writer writer) throws IOException {
        ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                .withSeparator(DELIMITER)
                .build();

        for (Task task : tasks) {
            String[] row = {
                    String.valueOf(task.getId()),
                    task.getTitle(),
                    task.getDescription() != null ? task.getDescription() : "",
                    task.getStatus().name(),
                    task.getPriority().name(),
                    // leeres Feld statt Text "null", wenn kein Datum gesetzt ist
                    task.getDueDate() != null ? task.getDueDate().toString() : ""
            };
            csvWriter.writeNext(row);
        }
        csvWriter.flush();
    }

    /**
     * Liest alle Aufgaben aus einem CSV-Datenstrom. CSVReader zerlegt die
     * Datensätze korrekt - auch wenn ein Feld Zeilenumbrüche enthält und
     * sich damit über mehrere Textzeilen erstreckt (genau das kann ein
     * simples readLine() + split() nicht leisten).
     *
     * Fehlerhafte Datensätze (falsche Spaltenzahl, unbekannter Status, ...)
     * brechen nicht den ganzen Import ab, sondern werden gezählt und
     * übersprungen - die GUI meldet die Anzahl dem Nutzer.
     */
    @Override
    public Task.ImportResult importFrom(Reader reader) throws IOException {
        List<Task> tasks = new ArrayList<>();
        int skipped = 0;

        try (CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(new CSVParserBuilder().withSeparator(DELIMITER).build())
                .build()) {

            String[] row;
            while (true) {
                try {
                    row = csvReader.readNext();
                } catch (CsvValidationException e) {
                    // strukturell kaputter Datensatz: überspringen, weiterlesen
                    System.err.println("Datensatz übersprungen - Grund: " + e.getMessage());
                    skipped++;
                    continue;
                }
                if (row == null) {
                    break; // Ende der Datei erreicht
                }

                try {
                    Task task = parseRow(row);
                    if (task != null) {
                        tasks.add(task);
                    }
                } catch (Exception e) {
                    // inhaltlich fehlerhafter Datensatz (z.B. ungültiger Enum-Wert)
                    System.err.println("Datensatz übersprungen: " + String.join(";", row)
                            + " - Grund: " + e.getMessage());
                    skipped++;
                }
            }
        }

        return new Task.ImportResult(tasks, skipped);
    }

    /**
     * Wandelt einen bereits zerlegten CSV-Datensatz in ein Task-Objekt um.
     * Die ID aus der Datei wird beim anschließenden Speichern
     * (TaskRepository.save) ohnehin ignoriert - SQLite vergibt beim
     * Import immer eine neue, eigene ID.
     */
    private Task parseRow(String[] row) {
        // komplett leere Zeilen (z.B. am Dateiende) stillschweigend ignorieren
        if (row.length == 1 && row[0].isBlank()) {
            return null;
        }

        if (row.length < COLUMN_COUNT) {
            throw new IllegalArgumentException("Ungültiges CSV-Format: erwartet "
                    + COLUMN_COUNT + " Spalten, gefunden " + row.length);
        }

        int id = Integer.parseInt(row[0].trim());
        String title = row[1];
        String description = row[2];
        TaskStatus status = TaskStatus.valueOf(row[3].trim());
        Priority priority = Priority.valueOf(row[4].trim());

        // Datum ist optional: leeres Feld (neues Format) oder Text "null"
        // (Altformat vor OpenCSV) bedeuten "kein Fälligkeitsdatum"
        LocalDate dueDate = null;
        String dateString = row[5].trim();
        if (!dateString.isBlank() && !dateString.equals("null")) {
            dueDate = LocalDate.parse(dateString);
        }

        return new Task(id, title, description, status, priority, dueDate);
    }
}
