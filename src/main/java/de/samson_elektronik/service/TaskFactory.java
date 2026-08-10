package de.samson_elektronik.service;

import de.samson_elektronik.model.Priority;
import de.samson_elektronik.model.Task;

import java.time.LocalDate;

/**
 * Factory Pattern: kapselt die Erzeugung von Task-Objekten an
 * zentraler Stelle. Statische Methoden, da die Factory keinen
 * eigenen Zustand hat - reine Erzeugungslogik.
 */
public class TaskFactory {
    /** Erstellt eine Aufgabe mit allen Angaben. */
    public static Task createTask(String title, String description, Priority priority, LocalDate dueDate) {
        return new Task(title, description, priority, dueDate);
    }

    /** Erstellt eine Aufgabe nur mit Titel, Rest wird auf Standardwerte gesetzt. */
    public static Task createQuickTask(String title) {
        return createTask(title, "", Priority.MEDIUM, LocalDate.now().plusWeeks(1));
    }
}
