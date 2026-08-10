package de.samson_elektronik.service;

import de.samson_elektronik.model.Task;

import java.util.Comparator;

public class SortByPriority implements SortStrategy {
    @Override
    public Comparator<Task> getComparator() {
        return Comparator.comparingInt((Task task) -> task.getPriority().getLevel()).reversed();
    }
}
