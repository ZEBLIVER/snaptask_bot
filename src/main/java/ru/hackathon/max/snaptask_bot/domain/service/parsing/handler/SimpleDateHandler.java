package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.parser.ParsingState;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SimpleDateHandler implements DateParserHandler {

    private static final Logger log = LoggerFactory.getLogger(SimpleDateHandler.class);


    private static final Pattern SIMPLE_DATE_PATTERN = Pattern.compile(
            "\\b(\\d{1,2})[\\./](\\d{1,2})(?:[\\./](\\d{2}|\\d{4}))?\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final LocalTime DEFAULT_TIME = LocalTime.of(10, 0);

    @Override
    public ParsingState handle(ParsingState input) {
        if (input.isDeadlineSet()) {
            return input;
        }

        String textToSearch = input.getRemainingText();
        Matcher matcher = SIMPLE_DATE_PATTERN.matcher(textToSearch);

        if (!matcher.find()) {
            log.debug("SimpleDateHandler: Дата в формате ДД.ММ не найдена.");
            return input;
        }

        ZoneId userZoneId = input.getUserZoneId();
        LocalDateTime now = ZonedDateTime.now(userZoneId).toLocalDateTime().withSecond(0).withNano(0);

        try {
            int day = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            String yearStr = matcher.group(3);

            int year = now.getYear();

            if (yearStr != null) {
                year = yearStr.length() == 2
                        ? 2000 + Integer.parseInt(yearStr)
                        : Integer.parseInt(yearStr);
            }

            LocalDateTime newDeadline = LocalDateTime.of(year, month, day,
                    DEFAULT_TIME.getHour(),
                    DEFAULT_TIME.getMinute());

            if (yearStr == null && newDeadline.isBefore(now)) {
                log.debug("SimpleDateHandler: Дата {} в прошлом. Переносим на следующий год.", newDeadline);
                newDeadline = newDeadline.plusYears(1);
            }

            String newRemainingText = new StringBuilder(textToSearch)
                    .delete(matcher.start(), matcher.end())
                    .toString()
                    .trim()
                    .replaceAll("\\s+", " ");

            log.debug("SimpleDateHandler нашел дату: {}/{}/{} в тексте. Дедлайн установлен на: {}",
                    day, month, year, newDeadline);

            return input.update(
                    newRemainingText,
                    Optional.of(newDeadline),
                    input.getRecurrenceRule()
            );

        } catch (NumberFormatException | java.time.DateTimeException e) {
            log.warn("SimpleDateHandler: Ошибка парсинга или недопустимая дата (например, 30.02) в тексте: '{}'. {}",
                    textToSearch, e.getMessage());
            return input;
        }
    }
}