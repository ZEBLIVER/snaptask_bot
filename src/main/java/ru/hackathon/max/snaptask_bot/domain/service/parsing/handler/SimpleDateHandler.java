package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SimpleDateHandler implements DateParserHandler {

    // Паттерн для поиска даты:
    // Группа 1: День, Группа 2: Месяц, Группа 3: Год (опционально, 2 или 4 цифры)
    private static final Pattern SIMPLE_DATE_PATTERN = Pattern.compile(
            "\\b(\\d{1,2})[\\./](\\d{1,2})(?:[\\./](\\d{2}|\\d{4}))?\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Дефолтное время, если пользователь указал только дату
    private static final LocalTime DEFAULT_TIME = LocalTime.of(10, 0);

    @Override
    public ParsingState handle(ParsingState input) {
        // Срабатываем только если дедлайн еще не установлен (DayOfWeekHandler, RelativeDateHandler и т.д.)
        if (input.isDeadlineSet()) {
            return input;
        }

        String textToSearch = input.getRemainingText();
        Matcher matcher = SIMPLE_DATE_PATTERN.matcher(textToSearch);

        if (!matcher.find()) {
            return input;
        }

        // Фиксируем текущий момент для сравнений
        LocalDateTime now = LocalDateTime.now(getZoneId()).withSecond(0).withNano(0);

        try {
            // --- 1. Парсинг и определение года ---
            int day = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            String yearStr = matcher.group(3);

            int year = now.getYear();

            if (yearStr != null) {
                // Если указано 2 цифры (напр. 25), считаем это 2025
                year = yearStr.length() == 2
                        ? 2000 + Integer.parseInt(yearStr)
                        : Integer.parseInt(yearStr);
            }

            // Пытаемся создать дату с текущим (или указанным) годом и дефолтным временем
            LocalDateTime newDeadline = LocalDateTime.of(year, month, day,
                    DEFAULT_TIME.getHour(),
                    DEFAULT_TIME.getMinute());

            // --- 2. Корректировка года (только если год не был явно указан) ---

            // Если год НЕ был указан И полученная дата уже в прошлом,
            // предполагаем, что пользователь имел в виду следующий год.
            // Если год БЫЛ указан (например, 2024), мы оставляем его, чтобы AbsoluteTimeHandler
            // мог его использовать (даже если он в прошлом).
            if (yearStr == null && newDeadline.isBefore(now)) {
                newDeadline = newDeadline.plusYears(1);
            }

            // Note: Если год был указан (например, 2024), и дата прошла,
            // она останется в прошлом. Это позволяет пользователю задать
            // напоминание на "следующий 25.10" (если сегодня 26.10),
            // и только DayOfWeekHandler должен решать, переносить ли её.

            // --- 3. Безопасная очистка текста (Используем Matcher) ---
            String newRemainingText = new StringBuilder(textToSearch)
                    .delete(matcher.start(), matcher.end())
                    .toString()
                    .trim()
                    .replaceAll("\\s+", " "); // Добавим очистку двойных пробелов

            return input.update(
                    newRemainingText,
                    Optional.of(newDeadline),
                    input.getRecurrenceRule()
            );

        } catch (NumberFormatException | java.time.DateTimeException e) {
            // Отлов: 30.02, 32.10 и т.д.
            // Логируем ошибку, возвращаем исходное состояние.
            return input;
        }
    }

    private ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }
}