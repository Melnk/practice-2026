package ru.itis.practice.mapper;

import ru.itis.practice.entity.User;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public class UserFileMapper {
    private static final String DELIMITER = "|";
    private static final String DELIMITER_REGEX = "\\|";

    public String toLine(User user) {
        return String.join(DELIMITER,
                user.id().toString(),
                encode(user.email()),
                encode(user.passwordHash()),
                encode(user.profileDescription()),
                user.createdAt().toString(),
                user.updatedAt().toString()
        );
    }

    public User fromLine(String line) {
        String[] parts = line.split(DELIMITER_REGEX, -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("Некорректная строка пользователя: " + line);
        }
        return new User(
                UUID.fromString(parts[0]),
                decode(parts[1]),
                decode(parts[2]),
                decode(parts[3]),
                Instant.parse(parts[4]),
                Instant.parse(parts[5])
        );
    }

    private String encode(String value) {
        String safeValue = value == null ? "" : value;
        return Base64.getEncoder().encodeToString(safeValue.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
