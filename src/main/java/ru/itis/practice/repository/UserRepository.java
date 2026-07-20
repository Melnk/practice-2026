package ru.itis.practice.repository;

import ru.itis.practice.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);

    void update(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findAllByProfileDescription(String profileDescription);
}
