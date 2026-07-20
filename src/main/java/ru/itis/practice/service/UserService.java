package ru.itis.practice.service;

import ru.itis.practice.dto.CreateUserRequest;
import ru.itis.practice.dto.UserDto;
import ru.itis.practice.entity.User;
import ru.itis.practice.exception.DuplicateUserException;
import ru.itis.practice.exception.ValidationException;
import ru.itis.practice.repository.UserRepository;
import ru.itis.practice.security.PasswordHasher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class UserService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_DESCRIPTION_LENGTH = 1_000;

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public UserDto register(CreateUserRequest request) {
        String email = normalizeEmail(request.email());
        String password = requirePassword(request.password());
        String profileDescription = normalizeDescription(request.profileDescription());

        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateUserException("Пользователь с таким email уже существует");
        }

        Instant now = Instant.now();
        User user = new User(
                UUID.randomUUID(),
                email,
                passwordHasher.hash(password),
                profileDescription,
                now,
                now
        );

        return toDto(userRepository.save(user));
    }

    public Optional<UserDto> authenticate(String email, String password) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (normalizedEmail.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        return userRepository.findByEmail(normalizedEmail)
                .filter(user -> passwordHasher.matches(password, user.passwordHash()))
                .map(this::toDto);
    }

    public Optional<UserDto> findById(String id) {
        UUID userId = parseUuid(id);
        return userRepository.findById(userId).map(this::toDto);
    }

    public boolean updateProfileDescription(String email, String profileDescription) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedDescription = normalizeDescription(profileDescription);

        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        if (userOptional.isEmpty()) {
            return false;
        }

        User updatedUser = userOptional.get().withProfileDescription(normalizedDescription, Instant.now());
        userRepository.update(updatedUser);
        return true;
    }

    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<UserDto> findAllByProfileDescription(String profileDescription) {
        String normalizedDescription = normalizeDescription(profileDescription);
        return userRepository.findAllByProfileDescription(normalizedDescription)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.id(),
                user.email(),
                user.profileDescription(),
                user.createdAt(),
                user.updatedAt()
        );
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }

        String normalized = email.trim().toLowerCase();
        if (normalized.length() > 320 || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new ValidationException("Некорректный email");
        }
        return normalized;
    }

    private String requirePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("Пароль не может быть пустым");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Пароль должен быть не короче " + MIN_PASSWORD_LENGTH + " символов");
        }
        return password;
    }

    private String normalizeDescription(String profileDescription) {
        String normalized = profileDescription == null ? "" : profileDescription.trim();
        if (normalized.length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Описание профиля слишком длинное");
        }
        return normalized;
    }

    private UUID parseUuid(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidationException("ID не может быть пустым");
        }

        try {
            return UUID.fromString(id.trim());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("ID должен быть корректным UUID");
        }
    }
}
