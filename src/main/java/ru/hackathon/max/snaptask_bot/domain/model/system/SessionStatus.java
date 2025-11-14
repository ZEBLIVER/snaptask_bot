package ru.hackathon.max.snaptask_bot.domain.model.system;

public enum SessionStatus {
    ACTIVE, // Сессия идет
    PAUSED, // Сессия на паузе
    FINISHED, // Сессия завершена успешно (по таймеру)
    ABANDONED // Сессия прервана пользователем
}