package ru.itis.practice.config;

import java.util.Locale;

public enum StorageType {
    JDBC,
    FILE;

    public static StorageType from(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "FILE" -> FILE;
            case "JDBC", "POSTGRES", "POSTGRESQL", "" -> JDBC;
            default -> throw new IllegalArgumentException("Неизвестный тип хранилища: " + value);
        };
    }
}
