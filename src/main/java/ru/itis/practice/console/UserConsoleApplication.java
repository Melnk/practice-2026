package ru.itis.practice.console;

import ru.itis.practice.dto.CreateUserRequest;
import ru.itis.practice.dto.UserDto;
import ru.itis.practice.exception.DuplicateUserException;
import ru.itis.practice.exception.ValidationException;
import ru.itis.practice.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserConsoleApplication {
    private final UserService userService;
    private final Scanner scanner;

    public UserConsoleApplication(UserService userService) {
        this.userService = userService;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String action = scanner.nextLine().trim();
            try {
                running = handleAction(action);
            } catch (ValidationException | DuplicateUserException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println("Ошибка приложения: " + e.getMessage());
            }
        }
    }

    private boolean handleAction(String action) {
        return switch (action) {
            case "1" -> {
                registerUser();
                yield true;
            }
            case "2" -> {
                authenticateUser();
                yield true;
            }
            case "3" -> {
                findUserById();
                yield true;
            }
            case "4" -> {
                updateProfileDescription();
                yield true;
            }
            case "5" -> {
                showAllUsers();
                yield true;
            }
            case "6" -> {
                findUsersByProfileDescription();
                yield true;
            }
            case "0" -> {
                System.out.println("Работа приложения завершена.");
                yield false;
            }
            default -> {
                System.out.println("Неизвестное действие.");
                yield true;
            }
        };
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Главное меню ===");
        System.out.println("1. Зарегистрировать пользователя");
        System.out.println("2. Войти в систему");
        System.out.println("3. Найти пользователя по ID");
        System.out.println("4. Обновить описание профиля");
        System.out.println("5. Показать всех пользователей");
        System.out.println("6. Найти пользователей по описанию профиля");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
    }

    private void registerUser() {
        System.out.println();
        System.out.println("-- Регистрация пользователя --");
        String email = ask("Введите email: ");
        String password = ask("Введите пароль: ");
        String profileDescription = ask("Введите описание профиля: ");

        UserDto user = userService.register(new CreateUserRequest(email, password, profileDescription));
        System.out.println("Пользователь зарегистрирован.");
        printUser(user);
    }

    private void authenticateUser() {
        System.out.println();
        System.out.println("-- Вход в систему --");
        String email = ask("Введите email: ");
        String password = ask("Введите пароль: ");

        Optional<UserDto> user = userService.authenticate(email, password);
        if (user.isPresent()) {
            System.out.println("Вход выполнен успешно. Добро пожаловать, " + user.get().email());
        } else {
            System.out.println("Неверный email или пароль.");
        }
    }

    private void findUserById() {
        System.out.println();
        System.out.println("-- Поиск пользователя по ID --");
        String id = ask("Введите ID: ");

        Optional<UserDto> user = userService.findById(id);
        if (user.isPresent()) {
            printUser(user.get());
        } else {
            System.out.println("Пользователь не найден.");
        }
    }

    private void updateProfileDescription() {
        System.out.println();
        System.out.println("-- Обновление описания профиля --");
        String email = ask("Введите email: ");
        String profileDescription = ask("Введите новое описание: ");

        boolean updated = userService.updateProfileDescription(email, profileDescription);
        if (updated) {
            System.out.println("Описание профиля обновлено.");
        } else {
            System.out.println("Пользователь с таким email не найден.");
        }
    }

    private void showAllUsers() {
        List<UserDto> users = userService.findAll();
        if (users.isEmpty()) {
            System.out.println("Список пользователей пуст.");
            return;
        }

        System.out.println();
        System.out.println("=== Все пользователи ===");
        users.forEach(this::printUser);
    }

    private void findUsersByProfileDescription() {
        System.out.println();
        System.out.println("-- Поиск пользователей по описанию профиля --");
        String profileDescription = ask("Введите описание профиля: ");

        List<UserDto> users = userService.findAllByProfileDescription(profileDescription);
        if (users.isEmpty()) {
            System.out.println("Пользователи с таким описанием не найдены.");
            return;
        }

        System.out.println("Найденные пользователи:");
        users.forEach(this::printUser);
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private void printUser(UserDto user) {
        System.out.println("ID: " + user.id());
        System.out.println("Email: " + user.email());
        System.out.println("Описание: " + user.profileDescription());
        System.out.println("Создан: " + user.createdAt());
        System.out.println("Обновлен: " + user.updatedAt());
        System.out.println();
    }
}
