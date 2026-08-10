package de.samson_elektronik.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Singleton Pattern: stellt sicher, dass zur Laufzeit nur EINE
 * Datenbankverbindung existiert. Nutzt Double-Checked Locking mit
 * "volatile", damit die Instanz auch bei gleichzeitigem Zugriff aus
 * mehreren Threads sicher nur einmal erzeugt wird.
 *
 * Legt beim ersten Start außerdem automatisch die benötigten Tabellen an
 * (CREATE TABLE IF NOT EXISTS), damit die Anwendung ohne manuelles
 * Datenbank-Setup lauffähig ist.
 */
public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:taskflow.db";
    private static volatile DatabaseManager instance;
    private Connection connection;

    /**
     * Privater Konstruktor - verhindert, dass Aufrufer außerhalb
     * dieser Klasse selbst "new DatabaseManager()" aufrufen können.
     * Einziger Zugang ist getInstance().
     */
    private DatabaseManager() {
        try {
            this.connection = DriverManager.getConnection(URL);


            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS task(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL," +
                        "description TEXT," +
                        "status TEXT NOT NULL," +
                        "priority TEXT NOT NULL," +
                        "due_date TEXT)"
                );

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS task_dependency(task_id INTEGER NOT NULL," +
                        "depends_on_id INTEGER NOT NULL," +
                        "FOREIGN KEY (task_id) REFERENCES task(id)," +
                        "FOREIGN KEY (depends_on_id) REFERENCES task(id))"
                );

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Datenbankverbindung fehlgeschlagen.");
        }
    }

    /**
     * Liefert die einzige Instanz. Double-Checked Locking:
     * der äußere null-Check vermeidet unnötiges Synchronisieren im
     * Normalfall (Instanz existiert meist schon), der innere Check
     * verhindert, dass zwei Threads gleichzeitig zwei Instanzen anlegen.
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return this.connection;
    }
}
