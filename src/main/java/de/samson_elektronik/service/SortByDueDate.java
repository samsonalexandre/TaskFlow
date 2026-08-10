package de.samson_elektronik.service;

import de.samson_elektronik.model.Task;

import java.util.Comparator;

/** Konkrete Strategie: sortiert Aufgaben nach Fälligkeitsdatum, aufsteigend. */
public class SortByDueDate implements SortStrategy {
    @Override
    public Comparator<Task> getComparator() {
        return Comparator.comparing(Task::getDueDate);
    }
}
