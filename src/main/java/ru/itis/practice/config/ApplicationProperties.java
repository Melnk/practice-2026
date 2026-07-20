package ru.itis.practice.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public class ApplicationProperties {
    private static final String RESOURCE_NAME = "/application.properties";

    private final Properties properties;

    private ApplicationProperties(Properties properties) {
        this.properties = properties;
    }

    public static ApplicationProperties load() {
        Properties properties = new Properties();
        try (InputStream inputStream = ApplicationProperties.class.getResourceAsStream(RESOURCE_NAME)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить настройки приложения", e);
        }
        return new ApplicationProperties(properties);
    }

    public StorageType storageType() {
        return StorageType.from(value("storage.type", "jdbc"));
    }

    public String dbUrl() {
        return value("db.url", "jdbc:postgresql://localhost:5432/demo");
    }

    public String dbUser() {
        return value("db.user", "postgres");
    }

    public String dbPassword() {
        return value("db.password", "postgres");
    }

    public Path filePath() {
        return Path.of(value("file.path", "users.db"));
    }

    private String value(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        String envKey = key.toUpperCase(Locale.ROOT).replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return properties.getProperty(key, defaultValue).trim();
    }
}
