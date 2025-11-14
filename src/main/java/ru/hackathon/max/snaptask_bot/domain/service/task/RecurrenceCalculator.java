package ru.hackathon.max.snaptask_bot.domain.service.task;

import org.springframework.stereotype.Service;
import ru.hackathon.max.snaptask_bot.domain.service.parsing.handler.RecurrenceHandler; // Для токенов

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис для расчета следующего дедлайна на основе правила повторения.
 */
@Service
public class RecurrenceCalculator {

    private static final Pattern EVERY_PATTERN = Pattern.compile("^EVERY:(\\d+):(\\w+)$");

    /**
     * Рассчитывает следующий дедлайн на основе текущего дедлайна и правила повторения.
     * @param currentDeadline Текущий дедлайн (Instant).
     * @param recurrenceRule Правило повторения (DAILY, WEEKLY или EVERY:N:UNIT).
     * @return Optional<Instant> нового дедлайна.
     */
    public Optional<Instant> calculateNextDeadline(Instant currentDeadline, String recurrenceRule) {
        if (recurrenceRule == null || currentDeadline == null) {
            return Optional.empty();
        }

        switch (recurrenceRule) {
            case RecurrenceHandler.DAILY:
                return Optional.of(currentDeadline.plus(1, ChronoUnit.DAYS));
            case RecurrenceHandler.WEEKLY:
                return Optional.of(currentDeadline.plus(7, ChronoUnit.DAYS));
            case RecurrenceHandler.MONTHLY:
                return Optional.of(currentDeadline.plus(30, ChronoUnit.DAYS));
            case RecurrenceHandler.YEARLY:
                return Optional.of(currentDeadline.plus(365, ChronoUnit.DAYS));
        }

        Matcher matcher = EVERY_PATTERN.matcher(recurrenceRule);
        if (matcher.matches()) {
            try {
                int interval = Integer.parseInt(matcher.group(1));
                String unitName = matcher.group(2);

                return calculateComplexDeadline(currentDeadline, interval, unitName);
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private Optional<Instant> calculateComplexDeadline(Instant currentDeadline, int interval, String unitName) {
        try {
            RecurrenceHandler.RecurrenceUnit unit = RecurrenceHandler.RecurrenceUnit.valueOf(unitName);

            ChronoUnit chronoUnit;
            switch (unit) {
                case MINUTES:
                    chronoUnit = ChronoUnit.MINUTES;
                    break;
                case HOURS:
                    chronoUnit = ChronoUnit.HOURS;
                    break;
                case DAYS:
                    chronoUnit = ChronoUnit.DAYS;
                    break;
                case WEEKS:
                    chronoUnit = ChronoUnit.WEEKS;
                    break;
                default:
                    return Optional.empty();
            }

            return Optional.of(currentDeadline.plus(interval, chronoUnit));

        } catch (IllegalArgumentException e) {
            // Единица измерения не распознана (UNIT)
            return Optional.empty();
        }
    }
}