package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.parser.ParsingState;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.*;

@Component
public class DayOfWeekHandler implements DateParserHandler {

    private static final Logger log = LoggerFactory.getLogger(DayOfWeekHandler.class);

    private static final Map<String, DayOfWeek> DAY_MAP = new HashMap<>();

    static {
        DAY_MAP.put("понедельник", DayOfWeek.MONDAY);
        DAY_MAP.put("вторник", DayOfWeek.TUESDAY);
        DAY_MAP.put("среда", DayOfWeek.WEDNESDAY);
        DAY_MAP.put("среду", DayOfWeek.WEDNESDAY);
        DAY_MAP.put("четверг", DayOfWeek.THURSDAY);
        DAY_MAP.put("пятница", DayOfWeek.FRIDAY);
        DAY_MAP.put("пятницу", DayOfWeek.FRIDAY);
        DAY_MAP.put("суббота", DayOfWeek.SATURDAY);
        DAY_MAP.put("субботу", DayOfWeek.SATURDAY);
        DAY_MAP.put("воскресенье", DayOfWeek.SUNDAY);

        DAY_MAP.put("пн", DayOfWeek.MONDAY);
        DAY_MAP.put("вт", DayOfWeek.TUESDAY);
        DAY_MAP.put("ср", DayOfWeek.WEDNESDAY);
        DAY_MAP.put("чт", DayOfWeek.THURSDAY);
        DAY_MAP.put("пт", DayOfWeek.FRIDAY);
        DAY_MAP.put("сб", DayOfWeek.SATURDAY);
        DAY_MAP.put("вс", DayOfWeek.SUNDAY);
        DAY_MAP.put("вск", DayOfWeek.SUNDAY);
    }

    private static final LocalTime DEFAULT_TIME = LocalTime.of(10, 0);

    private static final String DAY_REGEX_FORMAT =
            "(?<!\\p{L})(?:в|на|по)?\\s*(%s)(?!\\p{L})";

    @Override
    public ParsingState handle(ParsingState state) {
        if (state.isDeadlineSet()) return state;

        String text = state.getRemainingText().toLowerCase().trim();

        ZoneId userZoneId = state.getUserZoneId();
        LocalDateTime now = ZonedDateTime.now(userZoneId).toLocalDateTime().withSecond(0).withNano(0);

        LocalDateTime newDeadline = null;
        String foundPhrase = null;

        log.debug("DayOfWeekHandler: Исходный текст для парсинга: '{}'", text);

        if (text.contains("послезавтра")) {
            newDeadline = now.plusDays(2).with(DEFAULT_TIME);
            foundPhrase = "послезавтра";
            log.debug("DayOfWeekHandler: Найдено 'послезавтра', дедлайн {}", newDeadline);
        } else if (text.contains("завтра")) {
            newDeadline = now.plusDays(1).with(DEFAULT_TIME);
            foundPhrase = "завтра";
            log.debug("DayOfWeekHandler: Найдено 'завтра', дедлайн {}", newDeadline);
        } else if (text.contains("сегодня")) {
            LocalDateTime todayDefault = now.with(DEFAULT_TIME);
            newDeadline = todayDefault.isBefore(now) ? now.plusDays(1).with(DEFAULT_TIME) : todayDefault;
            foundPhrase = "сегодня";
            log.debug("DayOfWeekHandler: Найдено 'сегодня', дедлайн {}", newDeadline);
        }

        if (newDeadline == null) {
            for (Map.Entry<String, DayOfWeek> entry : DAY_MAP.entrySet()) {
                String dayWord = entry.getKey();
                Pattern pattern = Pattern.compile(
                        String.format(DAY_REGEX_FORMAT, Pattern.quote(dayWord)),
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
                );
                Matcher matcher = pattern.matcher(text);

                if (matcher.find()) {
                    DayOfWeek day = entry.getValue();

                    LocalDateTime candidate = now.with(TemporalAdjusters.nextOrSame(day)).with(DEFAULT_TIME);

                    if (candidate.toLocalDate().equals(now.toLocalDate()) && candidate.isBefore(now)) {
                        candidate = now.with(TemporalAdjusters.next(day)).with(DEFAULT_TIME);
                    }

                    newDeadline = candidate;
                    foundPhrase = matcher.group(0).trim();
                    log.debug("DayOfWeekHandler: ✅ Найден день недели '{}', дедлайн {}", foundPhrase, newDeadline);
                    break;
                }
            }
        }

        if (newDeadline != null) {
            String cleanText = state.getRemainingText()
                    .replaceFirst("(?i)" + Pattern.quote(foundPhrase), "")
                    .replaceAll("\\s+", " ")
                    .trim();

            log.debug("DayOfWeekHandler: Обновлен оставшийся текст: '{}'", cleanText);

            return state.update(cleanText, Optional.of(newDeadline), state.getRecurrenceRule());
        }

        log.debug("DayOfWeekHandler: Дата или день недели не найдены.");
        return state;
    }
}