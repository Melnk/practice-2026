package ru.itis.practice.app;

import ru.itis.practice.config.ApplicationProperties;
import ru.itis.practice.config.SimpleDriverManagerDataSource;
import ru.itis.practice.console.UserConsoleApplication;
import ru.itis.practice.mapper.UserFileMapper;
import ru.itis.practice.mapper.UserRowMapper;
import ru.itis.practice.repository.UserFileRepository;
import ru.itis.practice.repository.UserRepository;
import ru.itis.practice.repository.UserRepositoryJdbc;
import ru.itis.practice.security.Pbkdf2PasswordHasher;
import ru.itis.practice.service.UserService;

import javax.sql.DataSource;

public final class ApplicationFactory {
    private ApplicationFactory() {
    }

    public static UserConsoleApplication create() {
        ApplicationProperties properties = ApplicationProperties.load();
        UserRepository repository = createRepository(properties);
        UserService userService = new UserService(repository, new Pbkdf2PasswordHasher());
        return new UserConsoleApplication(userService);
    }

    private static UserRepository createRepository(ApplicationProperties properties) {
        return switch (properties.storageType()) {
            case FILE -> new UserFileRepository(properties.filePath(), new UserFileMapper());
            case JDBC -> {
                DataSource dataSource = new SimpleDriverManagerDataSource(
                        properties.dbUrl(),
                        properties.dbUser(),
                        properties.dbPassword()
                );
                yield new UserRepositoryJdbc(dataSource, new UserRowMapper());
            }
        };
    }
}
