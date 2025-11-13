package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class DayOfWeekHandler implements DateParserHandler {

    private static final Map<String, DayOfWeek> DAY_MAP = new HashMap<>();
    static {
        // Ключи должны быть в нижнем регистре
        DAY_MAP.put("пн", DayOfWeek.MONDAY);
        DAY_MAP.put("вт", DayOfWeek.TUESDAY);
        DAY_MAP.put("ср", DayOfWeek.WEDNESDAY);
        DAY_MAP.put("чт", DayOfWeek.THURSDAY);
        DAY_MAP.put("пт", DayOfWeek.FRIDAY);
        DAY_MAP.put("сб", DayOfWeek.SATURDAY);
        DAY_MAP.put("вс", DayOfWeek.SUNDAY);
        DAY_MAP.put("понедельник", DayOfWeek.MONDAY);
        DAY_MAP.put("вторник", DayOfWeek.TUESDAY);
        DAY_MAP.put("среду", DayOfWeek.WEDNESDAY);
        DAY_MAP.put("четверг", DayOfWeek.THURSDAY);
        DAY_MAP.put("пятницу", DayOfWeek.FRIDAY);
        DAY_MAP.put("субботу", DayOfWeek.SATURDAY);
        DAY_MAP.put("воскресенье", DayOfWeek.SUNDAY);
    }

    private static final LocalTime DEFAULT_TIME = LocalTime.of(10, 0);

    @Override
    public ParsingState handle(ParsingState state) {
        if (state.isDeadlineSet()) {
            return state; // Дата уже установлена более конкретным хэндлером
        }

        String text = state.getRemainingText();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newDeadline = null;
        String foundPhrase = null;

        // 1. Относительные даты ("завтра", "послезавтра")
        if (text.contains("послезавтра")) {
            newDeadline = now.plusDays(2).with(DEFAULT_TIME);
            foundPhrase = "послезавтра";
        } else if (text.contains("завтра")) {
            newDeadline = now.plusDays(1).with(DEFAULT_TIME);
            foundPhrase = "завтра";
        }

        // 2. Дни недели
        if (newDeadline == null) {
            for (Map.Entry<String, DayOfWeek> entry : DAY_MAP.entrySet()) {
                // Ищем точное совпадение слова или с предлогом "в" или "по"
                String key = entry.getKey();
                String regex = "(?:\\bв\\s+|\\bпо\\s+)?\\b" + key + "\\b";
                Pattern p = Pattern.compile(regex);
                if (p.matcher(text).find()) {
                    DayOfWeek day = entry.getValue();
                    // Находим следующий день недели (включая сегодня, если время еще не прошло)
                    newDeadline = now.with(TemporalAdjusters.nextOrSame(day)).with(DEFAULT_TIME);
                    foundPhrase = p.matcher(text).results().findFirst().get().group(0);
                    break;
                }
            }
        }

        if (newDeadline != null) {
            // Удаляем распознанную часть, учитывая возможные предлоги
            String cleanText = text.replace(foundPhrase, "").replaceAll("(\\s*(в|по)\\s+)", " ").trim();
            return state.update(
                    cleanText,
                    Optional.of(newDeadline),
                    state.getRecurrenceRule()
            );
        }

        return state;
    }
}