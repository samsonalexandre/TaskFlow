package de.samson_elektronik.gui;

import de.samson_elektronik.model.Task;
import de.samson_elektronik.persistence.TaskRepository;
import de.samson_elektronik.service.CsvTaskAdapter;
import de.samson_elektronik.service.DependencyGraph;
import de.samson_elektronik.service.SortByDueDate;
import de.samson_elektronik.service.SortByPriority;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Hauptfenster der Anwendung (Controller-Teil des MVC-Prinzips).
 * Zeigt Aufgaben in einer JTable an (View: TaskTableModel) und
 * verkabelt alle Nutzerinteraktionen (Buttons, Doppelklick) mit
 * TaskRepository und den Service-Klassen.
 */
public class MainFrame extends JFrame {
    private final TaskRepository repository = new TaskRepository();
    private TaskTableModel tableModel;
    private JTable table;

    public MainFrame() {
        setTitle("TaskFlow");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 500);
        setLocationRelativeTo(null);

        initComponents();
        loadTasks();
    }

    private void initComponents() {
        getContentPane().setBackground(new Color(2, 242, 245));
        List<Task> tasks = repository.findAll();
        tableModel = new TaskTableModel(tasks);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Doppelklick auf eine Zeile öffnet den Bearbeiten-Dialog
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int selectedRow = table.getSelectedRow();
                    Task taskToEdit = tableModel.getTaskAt(selectedRow);

                    // Bestehende Abhängigkeit mitgeben, damit der Dialog sie vorselektieren kann
                    Integer currentDependencyId = repository.findDependencyIdForTask(taskToEdit.getId());
                    TaskDialog dialog = new TaskDialog(MainFrame.this, "Aufgabe bearbeiten", taskToEdit, repository.findAll(), currentDependencyId);
                    dialog.setVisible(true);

                    if (dialog.isConfirmed()) {
                        repository.update(dialog.getTask());
                        // alte Abhängigkeit immer zuerst entfernen, dann ggf. neue setzen -
                        // verhindert, dass eine entfernte Abhängigkeit in der DB "hängen bleibt"
                        repository.removeDependenciesForTask(dialog.getTask().getId());

                        if (dialog.getSelectedDependency() != null) {
                            repository.addDependency(dialog.getTask().getId(), dialog.getSelectedDependency().getId());
                        }

                        loadTasks();
                    }
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Hinzufügen");
        JButton deleteButton = new JButton("Löschen");
        JButton refreshButton = new JButton("Aktualisieren");
        JButton calcOrderButton = new JButton("Reihenfolge berechnen");
        JButton sortByDateButton = new JButton("Nach Datum sortieren");
        JButton sortByPriorityButton = new JButton("Nach Priorität sortieren");
        JButton exportButton = new JButton("CSV exportieren");
        JButton importButton = new JButton("CSV importieren");

        // 1. Neue Aufgabe anlegen
        addButton.addActionListener(e -> {
            // Beim Neuanlegen gibt es noch keine bestehende Abhängigkeit -> null
            TaskDialog dialog = new TaskDialog(this, "Neue Aufgabe erstellen", null, repository.findAll(), null);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                Task newTask = dialog.getTask();
                repository.save(newTask);
                if (dialog.getSelectedDependency() != null) {
                    repository.addDependency(newTask.getId(), dialog.getSelectedDependency().getId());
                }

                loadTasks();
            }
        });

        // 2. Ausgewählte Aufgabe löschen (mit Bestätigung + Fehlerbehandlung
        // für den Fall, dass andere Aufgaben von ihr abhängen)
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Bitte wähle zuerst eine Aufgabe aus der Tabelle aus.", "Keine Auswahl", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Task taskToDelete = tableModel.getTaskAt(selectedRow);
            int confirm = JOptionPane.showConfirmDialog(this, "Möchtest du die Aufgabe \"" + taskToDelete.getTitle() + "\" wirklich löschen?", "Löschen bestätigen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    repository.deleteById(taskToDelete.getId());
                    loadTasks();
                } catch (RuntimeException ex) {
                    // FOREIGN KEY constraint failed: andere Aufgaben hängen noch von dieser ab
                    JOptionPane.showMessageDialog(this,
                                    "Diese Aufgabe kann nicht gelöscht werden, da andere Aufgaben von ihr abhängen.\n" +
                                    "Lösche zuerst die abhängigen Aufgaben oder entferne die Abhängigkeit.",
                                    "Löschen nicht möglich",
                                    JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // 3. Aktualisieren-Button
        refreshButton.addActionListener(e -> loadTasks());

        // 4. Reihenfolge berechnen (Topologische Sortierung)
        calcOrderButton.addActionListener(e -> calculateExecutionOrder());

        // 5. Nach Datum sortieren
        sortByDateButton.addActionListener(e -> tableModel.sortBy(new SortByDueDate()));

        // 6. Nach Priorität sortieren
        sortByPriorityButton.addActionListener(e -> tableModel.sortBy(new SortByPriority()));

        // 7. CSV exportieren
        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("taskflow_export.csv"));

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                CsvTaskAdapter adapter = new CsvTaskAdapter();

                try (PrintWriter writer = new PrintWriter(new FileWriter(file))){
                    for (Task task : repository.findAll()) {
                        writer.println(adapter.export(task));
                    }
                    JOptionPane.showMessageDialog(this, "Export erfolgreich");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Export fehlgeschlagen: " + ex.getMessage(),
                            "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 8. CSV importieren
        importButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                CsvTaskAdapter adapter = new CsvTaskAdapter();
                int importedCount = 0;   // erfolgreich importierte Zeilen
                int skippedCount = 0;    // fehlerhafte, übersprungene Zeilen

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        try {
                            Task task = adapter.importFrom(line);
                            if (task != null) {
                                repository.save(task);
                                importedCount++;
                            }
                        } catch (Exception ex) {
                            // Fehlerhafte Zeile ignorieren, aber für uns im Hintergrund protokollieren
                            System.err.println("Zeile übersprungen: " + line + " - Grund: " + ex.getMessage());
                            skippedCount++;
                        }
                    }

                    // Dynamische Erfolgsmeldung zusammenbauen
                    String message = importedCount + " Aufgabe(n) erfolgreich importiert!";
                    if (skippedCount > 0) {
                        message += "\n" + skippedCount + " fehlerhafte Zeile(n) wurden übersprungen.";
                    }

                    JOptionPane.showMessageDialog(this, message, "Import abgeschlossen", JOptionPane.INFORMATION_MESSAGE);
                    loadTasks(); // Tabelle nach dem Import direkt neu zeichnen

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Import fehlgeschlagen: " + ex.getMessage(),
                            "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(calcOrderButton);
        buttonPanel.add(sortByDateButton);
        buttonPanel.add(sortByPriorityButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(importButton);

        add(buttonPanel, BorderLayout.SOUTH);


        addButton.setBackground(new Color(76, 175, 80));
        deleteButton.setBackground(new Color(244, 67, 54));
        buttonPanel.setBackground(new Color(3, 169, 244));
        refreshButton.setBackground(new Color(3, 169, 244));
        calcOrderButton.setBackground(new Color(3, 169, 244));
        sortByDateButton.setBackground(new Color(3, 169, 244));
        sortByPriorityButton.setBackground(new Color(3, 169, 244));
        exportButton.setBackground(new Color(255, 152, 0));
        importButton.setBackground(new Color(230, 126, 0));

        buttonPanel.setBackground(new Color(230, 240, 250));
        table.setSelectionBackground(new Color(187, 222, 251));
    }

    /**
     * Lädt den Abhängigkeitsgraphen aus der DB, berechnet die
     * topologische Sortierung und zeigt sie an. Fängt zwei erwartbare
     * Fehlerfälle sauber ab: Zyklen (IllegalStateException aus
     * DependencyGraph) und "keine Aufgaben vorhanden".
     */
    private void calculateExecutionOrder() {
        DependencyGraph graph = repository.loadDependencyGraph();
        List<Integer> sortedIds;

        try {
            sortedIds = graph.topologicalSort();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this,
                    "Es liegt ein zyklischer Konflikt (Kreisabhängigkeit) vor – Reihenfolge kann nicht berechnet werden!",
                    "Ablaufplan nicht möglich",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (sortedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keine Aufgaben vorhanden.", "Hinweis", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Map für schnellen Zugriff auf Task-Titel via ID
        Map<Integer, Task> taskMap = repository.findAll().stream()
                .collect(Collectors.toMap(Task::getId, Function.identity()));

        StringBuilder sb = new StringBuilder("Empfohlene Abarbeitungsreihenfolge:\n\n");
        int step = 1;
        for (Integer id : sortedIds) {
            Task task = taskMap.get(id);
            if (task != null) {
                sb.append(step++).append(". [ID ").append(task.getId()).append("] ")
                        .append(task.getTitle())
                        .append(" (Status: ").append(task.getStatus()).append(")\n");
            }
        }

        JTextArea textArea = new JTextArea(sb.toString(), 12, 35);
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Abarbeitungsreihenfolge", JOptionPane.INFORMATION_MESSAGE);
        // Tabelle in der berechneten Reihenfolge anzeigen
        tableModel.applyOrder(sortedIds);
    }

    private void loadTasks() {
        tableModel.setTasks(repository.findAll());
    }

    public static void main(String[] args) {
        // GUI-Code muss auf dem Event Dispatch Thread laufen (Swing ist nicht thread-safe)
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}