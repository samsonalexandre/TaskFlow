package de.samson_elektronik.service;

import de.samson_elektronik.model.Task;

import java.util.Comparator;

public interface SortStrategy {
    Comparator<Task> getComparator();
}
