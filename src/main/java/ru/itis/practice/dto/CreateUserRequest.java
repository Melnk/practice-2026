package ru.itis.practice.dto;

public record CreateUserRequest(
        String email,
        String password,
        String profileDescription
) {
}
