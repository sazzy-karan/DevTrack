package com.devtrack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Database {
    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        Properties configuration = loadLocalConfiguration();
        String url = requiredConfiguration(configuration, "DEVTRACK_DB_URL");
        String user = requiredConfiguration(configuration, "DEVTRACK_DB_USER");
        String password = requiredConfiguration(configuration, "DEVTRACK_DB_PASSWORD");
        return DriverManager.getConnection(url, user, password);
    }

    private static Properties loadLocalConfiguration() throws SQLException {
        Properties properties = new Properties();
        Path envFile = Path.of(".env");
        if (Files.exists(envFile)) {
            try (InputStream input = Files.newInputStream(envFile)) {
                properties.load(input);
            } catch (IOException exception) {
                throw new SQLException("Unable to read the local .env configuration file.", exception);
            }
        }
        return properties;
    }

    private static String requiredConfiguration(Properties properties, String name) throws SQLException {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = properties.getProperty(name);
        }
        if (value == null || value.isBlank()) {
            throw new SQLException("Set " + name + " in the environment or local .env file.");
        }
        return value.trim();
    }
}
