package de.samson_elektronik.model;

/**
 * Priorität einer Aufgabe. Jede Konstante trägt zusätzlich einen
 * numerischen "level"-Wert, damit Prioritäten sortiert werden können
 * (siehe service.SortByPriority) - ohne if/else-Ketten.
 */

public enum Priority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int level;

    Priority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
