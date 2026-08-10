package de.samson_elektronik.gui;

import de.samson_elektronik.model.Task;
import de.samson_elektronik.service.SortStrategy;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

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

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        fireTableDataChanged();
    }

    public Task getTaskAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < tasks.size()) {
            return tasks.get(rowIndex);
        }
        return null;
    }

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

    public void sortBy(SortStrategy strategy) {
        tasks.sort(strategy.getComparator());
        fireTableDataChanged();
    }
}
