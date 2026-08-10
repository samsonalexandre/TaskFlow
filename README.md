# TaskFlow

Eine Desktop-Anwendung zur Aufgabenverwaltung mit Abhängigkeitsgraph und automatischer Berechnung einer sinnvollen Abarbeitungsreihenfolge. Entwickelt als Abschlussprojekt im Rahmen der Umschulung zum Fachinformatiker für Anwendungsentwicklung (Modul Java 2).

## Motivation

Anders als ein einfaches To-do-Tool erlaubt TaskFlow, Abhängigkeiten zwischen Aufgaben abzubilden (z. B. "Aufgabe B kann erst starten, wenn Aufgabe A abgeschlossen ist"). Über eine topologische Sortierung berechnet die Anwendung daraus eine gültige Bearbeitungsreihenfolge und erkennt zirkuläre Abhängigkeiten automatisch.

## Screenshots

<!-- Screenshot der Hauptansicht (Tabelle mit Tasks) hier einfügen -->
<!-- Screenshot des "Aufgabe bearbeiten"-Dialogs hier einfügen -->
<!-- Screenshot der berechneten Reihenfolge hier einfügen -->

## Funktionsumfang

- Aufgaben anlegen, bearbeiten, löschen (CRUD)
- Aufgaben mit Titel, Beschreibung, Status, Priorität und Fälligkeitsdatum
- Abhängigkeiten zwischen Aufgaben definieren
- Automatische Berechnung einer gültigen Abarbeitungsreihenfolge (topologische Sortierung)
- Erkennung zirkulärer Abhängigkeiten mit verständlicher Fehlermeldung
- Sortierung nach Fälligkeitsdatum oder Priorität
- CSV-Export und -Import aller Aufgaben
- Persistente Speicherung in einer lokalen SQLite-Datenbank

## Technologie-Stack

| Bereich | Technologie |
|---|---|
| Sprache | Java 21 (LTS) |
| Build-Tool | Gradle |
| Datenbank | SQLite (über `org.xerial:sqlite-jdbc`) |
| GUI | Java Swing |

## Architektur

Das Projekt ist in vier Schichten gegliedert:

```
de.samson_elektronik.taskflow
├── model         Reine Datenklassen (Task, TaskStatus, Priority)
├── persistence   Datenbankzugriff (DatabaseManager, TaskRepository)
├── service       Fachlogik (DependencyGraph, Sortier-Strategien, Factory, CSV-Adapter)
└── gui           Swing-Oberfläche (MainFrame, TaskDialog, TaskTableModel)
```

Diese Trennung sorgt dafür, dass die Kernlogik (Datenmodell, Graph-Algorithmus) unabhängig von der Benutzeroberfläche und der Persistenz ist.

## Verwendete Design Patterns

| Pattern | Klasse(n) | Einsatzzweck |
|---|---|---|
| **Singleton** | `DatabaseManager` | Stellt sicher, dass zur Laufzeit nur eine Datenbankverbindung existiert (Double-Checked Locking mit `volatile`) |
| **Factory** | `TaskFactory` | Kapselt die Erzeugung von `Task`-Objekten mit sinnvollen Standardwerten |
| **Strategy** | `SortStrategy`, `SortByDueDate`, `SortByPriority` | Austauschbare Sortierkriterien für die Aufgabenliste, zur Laufzeit wählbar |
| **Adapter** | `TaskExportAdapter`, `CsvTaskAdapter` | Übersetzt zwischen dem `Task`-Domänenmodell und dem CSV-Zeilenformat für Export/Import |
| **Observer** | Swing `AbstractTableModel` (`TaskTableModel`) | `fireTableDataChanged()` benachrichtigt die `JTable` automatisch über Datenänderungen — das Standard-Beobachter-Prinzip von Swing |

## Algorithmus: Topologische Sortierung

`DependencyGraph` verwaltet Aufgaben als gerichteten Graphen (Adjazenzliste) und berechnet die Abarbeitungsreihenfolge mit **Kahn's Algorithmus**:

1. Berechnung des In-Degree (Anzahl offener Abhängigkeiten) jedes Knotens
2. Alle Knoten ohne offene Abhängigkeiten werden in eine Warteschlange aufgenommen
3. Abarbeitung der Warteschlange, dabei Verringerung des In-Degree betroffener Nachbarknoten
4. Enthält das Ergebnis am Ende weniger Knoten als der Graph insgesamt hat, liegt ein Zyklus vor — die Anwendung meldet dies statt abzustürzen

## Datenbankschema

```sql
CREATE TABLE task (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    status TEXT NOT NULL,
    priority TEXT NOT NULL,
    due_date TEXT
);

CREATE TABLE task_dependency (
    task_id INTEGER NOT NULL,
    depends_on_id INTEGER NOT NULL,
    FOREIGN KEY (task_id) REFERENCES task(id),
    FOREIGN KEY (depends_on_id) REFERENCES task(id)
);
```

Fremdschlüssel-Constraints sind aktiv (`PRAGMA foreign_keys = ON`), wodurch die Datenintegrität geschützt wird: Eine Aufgabe, von der andere Aufgaben abhängen, kann nicht gelöscht werden, solange die Abhängigkeit besteht.

## Projekt starten

Voraussetzung: Java 21 (JDK) installiert.

```bash
git clone https://github.com/samsonalexandre/TaskFlow.git
cd TaskFlow
./gradlew run
```

Die Anwendung legt beim ersten Start automatisch eine lokale SQLite-Datenbank (`taskflow.db`) im Projektverzeichnis an.

## Mögliche Erweiterungen

- Grafische Darstellung des Abhängigkeitsgraphen (z. B. als Baum- oder Netzdiagramm)
- Mehrere gleichzeitige Abhängigkeiten pro Aufgabe (aktuell: eine Abhängigkeit pro Aufgabe)
- Unit-Tests für `DependencyGraph` und `TaskRepository`

## Autor

Alexandre Samson — Umschulung zum Fachinformatiker für Anwendungsentwicklung