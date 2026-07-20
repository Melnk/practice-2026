package ru.itis.practice.dto;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String profileDescription,
        Instant createdAt,
        Instant updatedAt
) {
}
