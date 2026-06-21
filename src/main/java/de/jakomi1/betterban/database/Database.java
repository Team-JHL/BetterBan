package de.jakomi1.betterban.database;

import de.jakomi1.betterban.BetterBan;
import de.jakomi1.betterban.util.BanUtils;
import de.jakomi1.betterban.util.ChatBanUtils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static de.jakomi1.betterban.BetterBan.plugin;

public final class Database {

    private static final String DB_FILE_NAME = "database.db";
    private static final String JDBC_URL;

    static {
        File dbFile = new File(plugin.getDataFolder(), DB_FILE_NAME);
        JDBC_URL = "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    private Database() {}

    public static String getJdbcUrl() {
        return JDBC_URL;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    public static void init() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("PRAGMA foreign_keys = ON;");

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS schema_version (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT UNIQUE NOT NULL,
                        version INTEGER NOT NULL,
                        applied_at TEXT NOT NULL DEFAULT (datetime('now'))
                    );
                    """);
            BanUtils.init();
            ChatBanUtils.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
