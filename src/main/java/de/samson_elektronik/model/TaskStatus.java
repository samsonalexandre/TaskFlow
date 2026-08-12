package de.samson_elektronik.model;

/**
 * Repräsentiert den Bearbeitungsstatus einer Aufgabe.
 * Als Enum umgesetzt, damit der Compiler nur gültige Werte zulässt
 * (kein Tippfehler wie "TODOO" möglich, anders als bei einem String-Feld).
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
