package jm.task.core.jdbc;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.service.UserServiceImpl;

import java.util.List;

public class Main {
    private static UserServiceImpl service;

    public static void main(String[] args) {
        service = new UserServiceImpl();

        createTable();
        addUsers();
        printAllUsers();
        cleanTable();
        dropTable();
    }

    private static void createTable() {
        service.createUsersTable();
    }

    private static void addUsers() {
        List<User> users = List.of(
                new User("Тест 1", "Тестов 1", (byte) 25),
                new User("Тест 2", "Тестов 2", (byte) 26),
                new User("Тест 3", "Тестов 3", (byte) 27),
                new User("Тест 4", "Тестов 4", (byte) 28)
        );

        for (User user : users) {
            service.saveUser(user.getName(), user.getLastName(), user.getAge());
            System.out.printf("User с именем — %s добавлен в базу данных%n", user.getName());
        }
    }

    private static void printAllUsers() {
        List<User> users = service.getAllUsers();

        for (User user : users) {
            System.out.println(user.toString());
        }
    }

    private static void cleanTable() {
        service.cleanUsersTable();
    }

    private static void dropTable() {
        service.dropUsersTable();
    }
}
