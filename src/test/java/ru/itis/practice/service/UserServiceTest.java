package ru.itis.practice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.itis.practice.dto.CreateUserRequest;
import ru.itis.practice.dto.UserDto;
import ru.itis.practice.entity.User;
import ru.itis.practice.exception.DuplicateUserException;
import ru.itis.practice.exception.ValidationException;
import ru.itis.practice.repository.UserRepository;
import ru.itis.practice.security.PasswordHasher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {
    private InMemoryUserRepository repository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        userService = new UserService(repository, new TestPasswordHasher());
    }

    @Test
    void registerNormalizesEmailAndDoesNotExposePassword() {
        UserDto user = userService.register(new CreateUserRequest(
                "  OLEG@example.COM  ",
                "java2026",
                " Люблю Java "
        ));

        User storedUser = repository.findById(user.id()).orElseThrow();

        assertEquals("oleg@example.com", user.email());
        assertEquals("Люблю Java", user.profileDescription());
        assertNotEquals("java2026", storedUser.passwordHash());
        assertTrue(storedUser.passwordHash().startsWith("hash:"));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        userService.register(new CreateUserRequest("oleg@example.com", "java2026", "Java"));

        assertThrows(DuplicateUserException.class, () ->
                userService.register(new CreateUserRequest("OLEG@example.com", "another2026", "PostgreSQL"))
        );
    }

    @Test
    void authenticateChecksPassword() {
        userService.register(new CreateUserRequest("oleg@example.com", "java2026", "Java"));

        assertTrue(userService.authenticate("oleg@example.com", "java2026").isPresent());
        assertFalse(userService.authenticate("oleg@example.com", "wrongpass").isPresent());
    }

    @Test
    void updateProfileDescriptionChangesExistingUser() {
        userService.register(new CreateUserRequest("oleg@example.com", "java2026", "Java"));

        boolean updated = userService.updateProfileDescription("oleg@example.com", "Java и PostgreSQL");
        UserDto user = userService.authenticate("oleg@example.com", "java2026").orElseThrow();

        assertTrue(updated);
        assertEquals("Java и PostgreSQL", user.profileDescription());
    }

    @Test
    void findByIdRejectsInvalidUuid() {
        assertThrows(ValidationException.class, () -> userService.findById("not-a-uuid"));
    }

    private static class TestPasswordHasher implements PasswordHasher {
        @Override
        public String hash(String rawPassword) {
            return "hash:" + rawPassword;
        }

        @Override
        public boolean matches(String rawPassword, String storedHash) {
            return storedHash.equals(hash(rawPassword));
        }
    }

    private static class InMemoryUserRepository implements UserRepository {
        private final Map<UUID, User> users = new LinkedHashMap<>();

        @Override
        public User save(User user) {
            users.put(user.id(), user);
            return user;
        }

        @Override
        public void update(User user) {
            users.put(user.id(), user);
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return users.values()
                    .stream()
                    .filter(user -> user.email().equals(email))
                    .findFirst();
        }

        @Override
        public List<User> findAll() {
            return users.values()
                    .stream()
                    .sorted(Comparator.comparing(User::email))
                    .toList();
        }

        @Override
        public List<User> findAllByProfileDescription(String profileDescription) {
            return users.values()
                    .stream()
                    .filter(user -> user.profileDescription().equals(profileDescription))
                    .sorted(Comparator.comparing(User::email))
                    .toList();
        }
    }
}
