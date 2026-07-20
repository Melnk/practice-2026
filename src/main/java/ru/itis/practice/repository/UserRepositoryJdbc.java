package ru.itis.practice.repository;

import ru.itis.practice.entity.User;
import ru.itis.practice.mapper.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepositoryJdbc implements UserRepository {
    private static final String SELECT_FIELDS = """
            SELECT id, email, password_hash, profile_description, created_at, updated_at
            FROM users
            """;

    private final DataSource dataSource;
    private final RowMapper<User> userRowMapper;

    public UserRepositoryJdbc(DataSource dataSource, RowMapper<User> userRowMapper) {
        this.dataSource = dataSource;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public User save(User user) {
        String sql = """
                INSERT INTO users (id, email, password_hash, profile_description, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, user);
            statement.executeUpdate();
            return user;
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось сохранить пользователя", e);
        }
    }

    @Override
    public void update(User user) {
        String sql = """
                UPDATE users
                SET email = ?, password_hash = ?, profile_description = ?, updated_at = ?
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.email());
            statement.setString(2, user.passwordHash());
            statement.setString(3, user.profileDescription());
            statement.setObject(4, OffsetDateTime.ofInstant(user.updatedAt(), ZoneOffset.UTC));
            statement.setObject(5, user.id(), Types.OTHER);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось обновить пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        String sql = SELECT_FIELDS + "WHERE id = ?";
        return findOne(sql, statement -> statement.setObject(1, id, Types.OTHER));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = SELECT_FIELDS + "WHERE email = ?";
        return findOne(sql, statement -> statement.setString(1, email));
    }

    @Override
    public List<User> findAll() {
        String sql = SELECT_FIELDS + "ORDER BY email";
        return findMany(sql, statement -> {
        });
    }

    @Override
    public List<User> findAllByProfileDescription(String profileDescription) {
        String sql = SELECT_FIELDS + "WHERE profile_description = ? ORDER BY email";
        return findMany(sql, statement -> statement.setString(1, profileDescription));
    }

    private void fillStatement(PreparedStatement statement, User user) throws SQLException {
        statement.setObject(1, user.id(), Types.OTHER);
        statement.setString(2, user.email());
        statement.setString(3, user.passwordHash());
        statement.setString(4, user.profileDescription());
        statement.setObject(5, OffsetDateTime.ofInstant(user.createdAt(), ZoneOffset.UTC));
        statement.setObject(6, OffsetDateTime.ofInstant(user.updatedAt(), ZoneOffset.UTC));
    }

    private Optional<User> findOne(String sql, StatementBinder binder) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(userRowMapper.map(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось выполнить поиск пользователя", e);
        }
    }

    private List<User> findMany(String sql, StatementBinder binder) {
        List<User> users = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(userRowMapper.map(resultSet));
                }
            }
            return users;
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось получить список пользователей", e);
        }
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
