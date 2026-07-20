package ru.itis.practice.repository;

import ru.itis.practice.entity.User;
import ru.itis.practice.mapper.UserFileMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserFileRepository implements UserRepository {
    private final Path path;
    private final UserFileMapper mapper;

    public UserFileRepository(Path path, UserFileMapper mapper) {
        this.path = path;
        this.mapper = mapper;
    }

    @Override
    public synchronized User save(User user) {
        List<User> users = new ArrayList<>(readAll());
        users.add(user);
        writeAll(users);
        return user;
    }

    @Override
    public synchronized void update(User user) {
        List<User> users = readAll()
                .stream()
                .map(existing -> existing.id().equals(user.id()) ? user : existing)
                .toList();
        writeAll(users);
    }

    @Override
    public synchronized Optional<User> findById(UUID id) {
        return readAll()
                .stream()
                .filter(user -> user.id().equals(id))
                .findFirst();
    }

    @Override
    public synchronized Optional<User> findByEmail(String email) {
        return readAll()
                .stream()
                .filter(user -> user.email().equals(email))
                .findFirst();
    }

    @Override
    public synchronized List<User> findAll() {
        return readAll()
                .stream()
                .sorted(Comparator.comparing(User::email))
                .toList();
    }

    @Override
    public synchronized List<User> findAllByProfileDescription(String profileDescription) {
        return readAll()
                .stream()
                .filter(user -> user.profileDescription().equals(profileDescription))
                .sorted(Comparator.comparing(User::email))
                .toList();
    }

    private List<User> readAll() {
        if (!Files.exists(path)) {
            return List.of();
        }

        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8)
                    .stream()
                    .filter(line -> !line.isBlank())
                    .map(mapper::fromLine)
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать файл пользователей", e);
        }
    }

    private void writeAll(List<User> users) {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            List<String> lines = users.stream()
                    .map(mapper::toLine)
                    .toList();
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось записать файл пользователей", e);
        }
    }
}
