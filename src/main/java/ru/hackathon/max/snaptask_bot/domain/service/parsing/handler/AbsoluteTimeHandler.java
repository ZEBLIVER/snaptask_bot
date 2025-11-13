package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AbsoluteTimeHandler implements DateParserHandler {

    private static final Logger log = LoggerFactory.getLogger(AbsoluteTimeHandler.class);

    private static final Pattern ABSOLUTE_TIME_PATTERN = Pattern.compile(
            "(?:\\bв\\s+|\\bдо\\s+)?(\\d{1,2})(?:[\\.:](\\d{1,2}))?\\s*(?:ч|час(?:а|ов)?|:)?\\s*(утра|дня|вечера|ночи)?",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    private static final Pattern WORD_TIME_PATTERN = Pattern.compile(
            "\\b(утр(?:ом|а)|днём|днем|вечер(?:ом|а)|ноч(?:ью|и)|в\\s+обед|обед)\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    private static final LocalTime MORNING = LocalTime.of(9, 0);
    private static final LocalTime DAY = LocalTime.of(13, 0);
    private static final LocalTime EVENING = LocalTime.of(19, 0);
    private static final LocalTime NIGHT = LocalTime.of(22, 0);

    @Override
    public ParsingState handle(ParsingState state) {
        String text = state.getRemainingText();
        LocalDateTime newDeadline = null;
        LocalTime foundTime = null;
        String foundPhrase = null;

        ZoneId userZoneId = state.getUserZoneId();
        LocalDateTime now = ZonedDateTime.now(userZoneId).toLocalDateTime().withSecond(0).withNano(0);


        Matcher absMatcher = ABSOLUTE_TIME_PATTERN.matcher(text);
        if (absMatcher.find()) {
            try {
                int hour = Integer.parseInt(absMatcher.group(1));
                int minute = (absMatcher.group(2) != null) ? Integer.parseInt(absMatcher.group(2)) : 0;
                String timeOfDay = absMatcher.group(3);

                hour = adjustHourForTimeOfDay(hour, timeOfDay);

                if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                    throw new IllegalArgumentException("Некорректное время: " + hour + ":" + minute);
                }

                foundTime = LocalTime.of(hour, minute);
                foundPhrase = absMatcher.group(0);

                log.debug("AbsoluteTimeHandler нашел числовое время: {}, контекст: '{}'", foundTime, foundPhrase);

            } catch (Exception e) {
                log.warn("AbsoluteTimeHandler: Ошибка парсинга времени в '{}': {}", text, e.getMessage());
            }
        }

        if (foundTime == null) {
            Matcher wordMatcher = WORD_TIME_PATTERN.matcher(text);
            if (wordMatcher.find()) {
                String phrase = wordMatcher.group(1).toLowerCase();

                if (phrase.contains("утр")) foundTime = MORNING;
                else if (phrase.contains("дн") || phrase.contains("обед")) foundTime = DAY;
                else if (phrase.contains("вечер")) foundTime = EVENING;
                else if (phrase.contains("ноч")) foundTime = NIGHT;

                foundPhrase = wordMatcher.group(0);
                log.debug("AbsoluteTimeHandler нашел словесное время суток: {}", foundPhrase);
            }
        }

        if (foundTime != null) {

            if (state.isDeadlineSet()) {
                newDeadline = state.getDeadline().get().with(foundTime).withSecond(0).withNano(0);
            } else {
                newDeadline = now.with(foundTime).withSecond(0).withNano(0);

                if (newDeadline.isBefore(now)) {
                    newDeadline = newDeadline.plusDays(1);
                }
            }

            String cleanText = text.replace(foundPhrase, "")
                    .replaceAll("\\bв\\b", "")
                    .replaceAll("\\s+", " ")
                    .trim();

            log.debug("AbsoluteTimeHandler итог: {}, дедлайн: {}", foundTime, newDeadline);

            return state.update(
                    cleanText,
                    Optional.of(newDeadline),
                    state.getRecurrenceRule()
            );
        }

        return state;
    }


    private int adjustHourForTimeOfDay(int hour, String timeOfDay) {
        if (timeOfDay == null) return hour;

        String tod = timeOfDay.toLowerCase();

        if (hour == 12 && tod.equals("ночи")) return 0;
        if (hour == 12 && tod.equals("дня")) return 12;

        if (tod.equals("вечера") || tod.equals("дня")) {
            if (hour < 12) return hour + 12;
        }

        return hour;
    }
}