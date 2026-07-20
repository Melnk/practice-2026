package ru.itis.practice.security;

public interface PasswordHasher {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String storedHash);
}
