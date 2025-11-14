package ru.hackathon.max.snaptask_bot.domain.model.user;

public enum UserStatus {
    // Пользователь только что написал, нужно спросить часовой пояс
    AWAITING_TIMEZONE,

    // Пользователь полностью зарегистрирован и готов к работе
    REGISTERED,

    // Пользователь заблокировал бота или временно неактивен
    BLOCKED
}