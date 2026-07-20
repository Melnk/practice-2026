package ru.itis.practice;

import ru.itis.practice.app.ApplicationFactory;

public class Main {
    public static void main(String[] args) {
        ApplicationFactory.create().run();
    }
}
