package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RelativeTimeHandler implements DateParserHandler {

    // Паттерны для относительного времени (через N минут/часов/дней)
    private static final Pattern RELATIVE_TIME_PATTERN =
            Pattern.compile("через\\s+(\\d+)\\s+(минут|мин|час|ч|дня|дней|дн)\\w*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);

    @Override
    public ParsingState handle(ParsingState state) {
        if (state.isDeadlineSet()) {
            return state; // Если дата уже установлена, относительное время игнорируем
        }

        String text = state.getRemainingText();
        Matcher matcher = RELATIVE_TIME_PATTERN.matcher(text);

        if (matcher.find()) {
            try {
                int amount = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2);
                LocalDateTime deadline = LocalDateTime.now();

                if (unit.toLowerCase().startsWith("мин")) deadline = deadline.plusMinutes(amount);
                else if (unit.toLowerCase().startsWith("час") || unit.toLowerCase().startsWith("ч")) deadline = deadline.plusHours(amount);
                else if (unit.toLowerCase().startsWith("дня") || unit.toLowerCase().startsWith("дн")) deadline = deadline.plusDays(amount);
                else return state; // Неизвестная единица

                // Удаляем распознанную часть
                String foundPhrase = matcher.group(0);
                String cleanText = text.replace(foundPhrase, "").trim();

                return state.update(
                        cleanText,
                        Optional.of(deadline),
                        state.getRecurrenceRule()
                );
            } catch (Exception e) {
                // Игнорируем ошибку парсинга
                System.err.println("Ошибка парсинга относительного времени: " + e.getMessage());
            }
        }
        return state;
    }
}