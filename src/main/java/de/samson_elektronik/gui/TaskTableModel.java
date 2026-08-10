package de.samson_elektronik.gui;

import de.samson_elektronik.model.Task;
import de.samson_elektronik.service.SortStrategy;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * TableModel (MVC) für die JTable in MainFrame. AbstractTableModel
 * nimmt uns die Detailarbeit ab, wir müssen nur sagen, wie viele
 * Zeilen/Spalten es gibt und welcher Wert an welcher Stelle steht.
 *
 * fireTableDataChanged() ist das Observer Pattern in der Praxis:
 * benachrichtigt die JTable automatisch, dass sie sich neu zeichnen
 * soll, sobald sich die zugrunde liegenden Daten ändern.
 */
public class TaskTableModel extends AbstractTableModel {
    private final String[] columnNames = {"ID", "Titel", "Status", "Priorität", "Fällig am"};
    private List<Task> tasks;

    public TaskTableModel(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public int getRowCount() {
        return tasks.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Task task = tasks.get(rowIndex);

        return switch (columnIndex) {
            case 0 -> task.getId();
            case 1 -> task.getTitle();
            case 2 -> task.getStatus();
            case 3 -> task.getPriority();
            case 4 -> task.getDueDate();
            default -> null;
        };
    }

    /** Ersetzt die komplette Datenbasis, z.B. nach dem Neuladen aus der DB. */
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        fireTableDataChanged();
    }

    /** Liefert das Task-Objekt zu einer Tabellenzeile, z.B. für Bearbeiten/Löschen. */
    public Task getTaskAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < tasks.size()) {
            return tasks.get(rowIndex);
        }
        return null;
    }

    /**
     * Ordnet die Tasks in der übergebenen ID-Reihenfolge neu an
     * (genutzt nach der Berechnung der topologischen Sortierung).
     */
    public void applyOrder(List<Integer> orderedIds) {
        List<Task> reordered = new ArrayList<>();

        for (Integer id : orderedIds) {
            for (Task t : tasks) {
                if (t.getId() == id) {
                    reordered.add(t);
                    break;
                }
            }
        }
        this.tasks = reordered;
        fireTableDataChanged();
    }

    /**
     * Sortiert die aktuelle Liste mit der übergebenen Strategie
     * (Strategy Pattern) und aktualisiert die Anzeige.
     */
    public void sortBy(SortStrategy strategy) {
        tasks.sort(strategy.getComparator());
        fireTableDataChanged();
    }
}
