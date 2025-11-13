package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RelativeDateHandler implements DateParserHandler {

    // Паттерн для поиска: "на следующей неделе", "в следующем месяце"
    // Группа 1: Единица времени (неделе, месяце, году)
    private static final Pattern RELATIVE_DATE_PATTERN = Pattern.compile(
            "\\b(?:на\\s+)?(следующей|след)\\s+(неделе|мес|месяце|году)\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Дефолтное время для относительных дат (Например, 10:00 утра понедельника)
    private static final LocalTime DEFAULT_TIME = LocalTime.of(10, 0);

    @Override
    public ParsingState handle(ParsingState input) {
        // Срабатываем только если дедлайн еще НЕ установлен
        if (input.isDeadlineSet()) {
            return input;
        }

        String textToSearch = input.getRemainingText();
        Matcher matcher = RELATIVE_DATE_PATTERN.matcher(textToSearch);

        if (!matcher.find()) {
            return input;
        }

        LocalDateTime now = LocalDateTime.now(getZoneId()).withSecond(0).withNano(0);
        LocalDateTime newDeadline;

        // --- 1. Определение даты ---
        String unit = matcher.group(2).toLowerCase();

        if (unit.startsWith("нед")) { // неделе
            // Устанавливаем на следующий понедельник (ближайший, но не сегодня)
            newDeadline = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).with(DEFAULT_TIME);
        } else if (unit.startsWith("мес")) { // месяце
            // Устанавливаем на 1-е число следующего месяца
            newDeadline = now.with(TemporalAdjusters.firstDayOfNextMonth()).with(DEFAULT_TIME);
        } else if (unit.startsWith("год")) { // году
            // Устанавливаем на 1 января следующего года
            newDeadline = now.with(TemporalAdjusters.firstDayOfNextYear()).with(DEFAULT_TIME);
        } else {
            return input;
        }

        // --- 2. Безопасная очистка текста ---
        String newRemainingText = new StringBuilder(textToSearch)
                .delete(matcher.start(), matcher.end())
                .toString()
                .trim();

        // --- 3. Возвращаем новое состояние ---
        return input.update(
                newRemainingText,
                Optional.of(newDeadline),
                input.getRecurrenceRule()
        );
    }

    private ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }
}