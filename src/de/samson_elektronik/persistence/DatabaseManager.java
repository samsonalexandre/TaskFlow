package de.samson_elektronik.persistence;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:taskflow.db";
    private static volatile DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            this.connection = DriverManager.getConnection(URL);
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
