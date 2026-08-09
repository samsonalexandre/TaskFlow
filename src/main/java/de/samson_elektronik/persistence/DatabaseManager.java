package de.samson_elektronik.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:taskflow.db";
    private static volatile DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            this.connection = DriverManager.getConnection(URL);

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS task(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL," +
                        "description TEXT," +
                        "status TEXT NOT NULL," +
                        "priority TEXT NOT NULL," +
                        "due_date TEXT)"
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Datenbankverbindung fehlgeschlagen.");
        }
    }

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
