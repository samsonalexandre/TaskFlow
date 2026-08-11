package de.samson_elektronik.service;

import de.samson_elektronik.model.Task;

import java.util.Comparator;

/**
 * Konkrete Strategie: sortiert Aufgaben nach Fälligkeitsdatum, aufsteigend.
 * nullsLast sorgt dafür, dass Aufgaben OHNE Datum ans Ende sortiert werden,
 * statt eine NullPointerException auszulösen (das Datum ist optional).
 */
public class SortByDueDate implements SortStrategy {
    @Override
    public Comparator<Task> getComparator() {
        return Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
