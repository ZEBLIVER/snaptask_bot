package ru.hackathon.max.snaptask_bot.domain.model.system;

/**
 * Константы для Payload (токенов), которые отправляются боту после нажатия кнопки.
 * Формат: COMMAND:taskId:value
 */
public final class CallbackPayload {
    private CallbackPayload() {}

    public static final String DEFER_TASK = "DEFER";

    public static final String COMPLETE_TASK = "COMPLETE";
    public static final String CANCEL_TASK = "CANCEL";
    public static final String TIME_5_MINUTES = "5m";
    public static final String TIME_15_MINUTES = "15m"; // Используем 15m как значение
    public static final String TIME_1_HOUR = "1h";
    public static final String TIME_3_HOURS = "3h";
    public static final String TIME_TOMORROW = "24h";
}