package de.samson_elektronik.service;

import de.samson_elektronik.model.Task;

import java.util.Comparator;

/**
 * Strategy Pattern: gemeinsames Interface für austauschbare
 * Sortierkriterien. Die GUI kennt nur dieses Interface, nicht die
 * konkrete Implementierung - neue Sortierarten lassen sich ergänzen,
 * ohne bestehenden Code zu ändern.
 */
public interface SortStrategy {
    Comparator<Task> getComparator();
}
