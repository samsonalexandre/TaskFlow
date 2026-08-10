package de.samson_elektronik.gui;

import de.samson_elektronik.model.Priority;
import de.samson_elektronik.model.TaskStatus;
import de.samson_elektronik.model.Task;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Modaler Dialog zum Anlegen UND Bearbeiten von Aufgaben (gleiche
 * Klasse für beide Fälle, unterschieden über "existingTask == null").
 * Enthält die Formularvalidierung (leerer Titel, ungültiges Datum)
 * sowie die Auswahl einer Abhängigkeit zu einer anderen Aufgabe.
 */
public class TaskDialog extends JDialog {
    private final JTextField titleField = new JTextField(20);
    private final JTextField descriptionField = new JTextField(20);
    private final JComboBox<TaskStatus> statusBox = new JComboBox<>(TaskStatus.values());
    private final JComboBox<Priority> priorityBox = new JComboBox<>(Priority.values());
    private final JTextField dueDateField = new JTextField(LocalDate.now().toString(), 20);

    // Object statt Task, damit auch der String "-- Keine Abhängigkeit --" reinpasst
    private final JComboBox<Object> dependencyBox = new JComboBox<>();

    private boolean confirmed = false;
    private Task task;
    private Task selectedDependency = null;

    public TaskDialog(Frame owner, String dialogTitle, Task existingTask, List<Task> availableTasks) {
        super(owner, dialogTitle, true);
        this.task = existingTask;

        setLayout(new BorderLayout());

        // 6 Zeilen im Grid (Titel, Beschreibung, Status, Priorität, Fälligkeit, Abhängigkeit)
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Abhängigkeiten befüllen
        dependencyBox.addItem("-- Keine Abhängigkeit --");
        for (Task t : availableTasks) {
            // Eine Aufgabe kann nicht von sich selbst abhängen
            if (existingTask == null || t.getId() != existingTask.getId()) {
                dependencyBox.addItem(t);
            }
        }

        // Zeigt in der ComboBox "#id - Titel" statt der internen Objektadresse
        dependencyBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Task t) {
                    setText("#" + t.getId() + " - " + t.getTitle());
                }
                return this;
            }
        });

        formPanel.add(new JLabel("Titel:"));
        formPanel.add(titleField);

        formPanel.add(new JLabel("Beschreibung:"));
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusBox);

        formPanel.add(new JLabel("Priorität:"));
        formPanel.add(priorityBox);

        formPanel.add(new JLabel("Fällig am (YYYY-MM-DD):"));
        formPanel.add(dueDateField);

        formPanel.add(new JLabel("Hängt ab von:"));
        formPanel.add(dependencyBox);

        // Formular mit bestehenden Werten vorbefüllen (Bearbeiten-Fall)
        if (existingTask != null) {
            titleField.setText(existingTask.getTitle());
            descriptionField.setText(existingTask.getDescription());
            statusBox.setSelectedItem(existingTask.getStatus());
            priorityBox.setSelectedItem(existingTask.getPriority());
            if (existingTask.getDueDate() != null) {
                dueDateField.setText(existingTask.getDueDate().toString());
            }
        }

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Speichern");
        JButton cancelButton = new JButton("Abbrechen");

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Validiert die Eingaben und erzeugt bzw. aktualisiert das
     * Task-Objekt. Bei Erfolg wird der Dialog geschlossen und
     * confirmed auf true gesetzt - so weiß der Aufrufer (MainFrame),
     * ob wirklich gespeichert werden soll oder der Dialog abgebrochen wurde.
     */
    private void onSave() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Der Titel darf nicht leer sein.", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate dueDate = null;
        String dateText = dueDateField.getText().trim();
        if (!dateText.isEmpty()) {
            try {
                dueDate = LocalDate.parse(dateText);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Ungültiges Datumsformat (Format: YYYY-MM-DD).", "Eingabefehler", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Ausgewählte Abhängigkeit auslesen
        Object selectedDep = dependencyBox.getSelectedItem();
        if (selectedDep instanceof Task t) {
            this.selectedDependency = t;
        }

        if (task == null) {
            task = new Task(
                    titleField.getText().trim(),
                    descriptionField.getText().trim(),
                    (TaskStatus) statusBox.getSelectedItem(),
                    (Priority) priorityBox.getSelectedItem(),
                    dueDate
            );
        } else {
            task.setTitle(titleField.getText().trim());
            task.setDescription(descriptionField.getText().trim());
            task.setStatus((TaskStatus) statusBox.getSelectedItem());
            task.setPriority((Priority) priorityBox.getSelectedItem());
            task.setDueDate(dueDate);
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Task getTask() {
        return task;
    }

    public Task getSelectedDependency() {
        return selectedDependency;
    }
}