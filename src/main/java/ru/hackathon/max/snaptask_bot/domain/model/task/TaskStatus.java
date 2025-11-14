package ru.hackathon.max.snaptask_bot.domain.model.task;

public enum TaskStatus {
    TODO, // Задача создана, но работа не начата
    IN_PROGRESS, // Работа над задачей идет (возможно, активна Focus Session)
    COMPLETED, // Задача выполнена
    DEFERRED // Отложено (например, на 1 час)
}