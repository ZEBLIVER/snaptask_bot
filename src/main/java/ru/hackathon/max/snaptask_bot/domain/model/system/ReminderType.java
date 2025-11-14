package ru.hackathon.max.snaptask_bot.domain.model.system;

public enum ReminderType {
    DEADLINE_APPROACHING, // Стандартное напоминание о дедлайне
    START_WORK_PROMPT,    // Умное напоминание, призывающее начать работу
    FOCUS_SESSION_END,    // Уведомление о завершении Помодоро-сессии
    GENERIC
}