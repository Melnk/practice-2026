# Melnik User Console Project

Проект сделан по отчету о производственной практике: консольное Java-приложение для регистрации пользователей, входа в систему, поиска, обновления описания профиля и работы с PostgreSQL через JDBC.

## Что сделано более продуманно

- пароль хранится не в открытом виде, а как PBKDF2-хеш;
- используется трехслойная архитектура: console, service, repository;
- DTO скрывает внутренние поля пользователя;
- все SQL-запросы выполняются через `PreparedStatement`;
- параметры подключения вынесены в `application.properties`;
- есть два режима хранения: `jdbc` для PostgreSQL и `file` для запуска без базы;
- добавлены проверки email, пароля и описания профиля;
- добавлены unit-тесты бизнес-логики.

## Структура

```text
src/main/java/ru/itis/practice
├── Main.java
├── app
├── config
├── console
├── dto
├── entity
├── exception
├── mapper
├── repository
├── security
└── service
```

## Настройка PostgreSQL

1. Создайте базу данных:

```bash
createdb -U postgres demo
```

2. Выполните схему:

```bash
psql -U postgres -d demo -f src/main/resources/schema.sql
```

3. Проверьте настройки:

```properties
storage.type=jdbc
db.url=jdbc:postgresql://localhost:5432/demo
db.user=postgres
db.password=postgres
```

## Сборка и тесты

```bash
mvn test
mvn package
```

После сборки готовый файл будет здесь:

```text
target/melnik-user-console-project-2.0.0.jar
```

## Запуск с PostgreSQL

```bash
java -jar target/melnik-user-console-project-2.0.0.jar
```

Параметры можно переопределить:

```bash
java -Ddb.url=jdbc:postgresql://localhost:5432/demo \
     -Ddb.user=postgres \
     -Ddb.password=postgres \
     -jar target/melnik-user-console-project-2.0.0.jar
```

## Запуск без PostgreSQL

Для демонстрации можно использовать файловое хранилище:

```bash
java -Dstorage.type=file -Dfile.path=users.db -jar target/melnik-user-console-project-2.0.0.jar
```
