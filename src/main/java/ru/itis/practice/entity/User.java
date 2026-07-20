package ru.itis.practice.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record User(
        UUID id,
        String email,
        String passwordHash,
        String profileDescription,
        Instant createdAt,
        Instant updatedAt
) {
    public User {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(passwordHash, "passwordHash");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        profileDescription = profileDescription == null ? "" : profileDescription;
    }

    public User withProfileDescription(String newDescription, Instant updateTime) {
        return new User(id, email, passwordHash, newDescription, createdAt, updateTime);
    }
}
