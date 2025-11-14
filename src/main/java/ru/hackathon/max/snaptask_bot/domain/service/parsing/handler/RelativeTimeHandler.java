package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.parser.ParsingState;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RelativeTimeHandler implements DateParserHandler {

    private static final Logger log = LoggerFactory.getLogger(RelativeTimeHandler.class);

    private static final Map<String, Integer> NUM_MAP = Map.of(
            "один", 1, "два", 2, "три", 3, "четыре", 4, "пять", 5,
            "шесть", 6, "семь", 7, "восемь", 8, "девять", 9, "десять", 10
    );

    private static final Pattern RELATIVE_TIME_PATTERN =
            Pattern.compile("\\bчерез\\s+(\\d+|один|два|три|четыре|пять|шесть|семь|восемь|девять|десять)\\s+(минут|мин|час|ч|дня|дней|дн)\\w*",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);

    @Override
    public ParsingState handle(ParsingState state) {
        if (state.isDeadlineSet()) {
            return state;
        }

        String text = state.getRemainingText();
        Matcher matcher = RELATIVE_TIME_PATTERN.matcher(text);

        if (matcher.find()) {
            try {
                int amount;
                String amountStr = matcher.group(1).toLowerCase();
                String unit = matcher.group(2).toLowerCase();

                try {
                    amount = Integer.parseInt(amountStr);
                } catch (NumberFormatException e) {
                    amount = NUM_MAP.getOrDefault(amountStr, 0);
                }

                if (amount == 0) {
                    log.warn("RelativeTimeHandler: Не удалось преобразовать количество '{}' в число.", amountStr);
                    return state;
                }

                ZoneId userZoneId = state.getUserZoneId();
                LocalDateTime now = ZonedDateTime.now(userZoneId).toLocalDateTime().withSecond(0).withNano(0);

                LocalDateTime deadline = now;

                if (unit.startsWith("мин")) deadline = deadline.plusMinutes(amount);
                else if (unit.startsWith("час") || unit.startsWith("ч")) deadline = deadline.plusHours(amount);
                    // Проверка на "дня/дней/дн"
                else if (unit.startsWith("дня") || unit.startsWith("дн")) deadline = deadline.plusDays(amount);
                else {
                    log.warn("RelativeTimeHandler: Найдена неизвестная единица времени в тексте: '{}'", unit);
                    return state;
                }

                String foundPhrase = matcher.group(0);
                String cleanText = text.replaceAll("(?i)" + Pattern.quote(foundPhrase), " ")
                        .replaceAll("\\s+", " ").trim();

                log.debug("RelativeTimeHandler нашел: '{}', дедлайн: {}", foundPhrase, deadline);

                return state.update(
                        cleanText,
                        Optional.of(deadline),
                        state.getRecurrenceRule()
                );
            } catch (Exception e) {
                log.error("Ошибка парсинга относительного времени в тексте: '{}'. {}", text, e.getMessage());
            }
        }
        return state;
    }
}