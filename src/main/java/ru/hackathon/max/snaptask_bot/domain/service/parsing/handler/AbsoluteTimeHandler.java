package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AbsoluteTimeHandler implements DateParserHandler {

    // Паттерны для конкретного времени (в 10:00, 11.20, 15ч)
    private static final Pattern ABSOLUTE_TIME_PATTERN =
            Pattern.compile("(\\bв\\s+|\\bдо\\s+)?(\\d{1,2})[\\.:]?(\\d{2})?\\s*(ч|час|:)?\\b");

    @Override
    public ParsingState handle(ParsingState state) {
        String text = state.getRemainingText();
        Matcher matcher = ABSOLUTE_TIME_PATTERN.matcher(text);

        if (matcher.find()) {
            try {
                int hour = Integer.parseInt(matcher.group(2));
                // Если минуты не указаны (group 3 - null), берем 0
                int minute = (matcher.group(3) != null) ? Integer.parseInt(matcher.group(3)) : 0;

                LocalTime foundTime = LocalTime.of(hour, minute);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime newDeadline;

                if (state.isDeadlineSet()) {
                    // Дата уже установлена (например, "завтра"), применяем только время
                    newDeadline = state.getDeadline().get().with(foundTime).withSecond(0).withNano(0);
                } else {
                    // Даты нет. Устанавливаем время на сегодня.
                    newDeadline = now.with(foundTime).withSecond(0).withNano(0);

                    // Если время уже прошло, переносим на завтра
                    if (newDeadline.isBefore(now)) {
                        newDeadline = newDeadline.plusDays(1);
                    }
                }

                // Удаляем распознанную часть
                String foundPhrase = matcher.group(0);
                String cleanText = text.replace(foundPhrase, "").trim();

                return state.update(
                        cleanText,
                        Optional.of(newDeadline),
                        state.getRecurrenceRule()
                );
            } catch (Exception e) {
                // Игнорируем некорректное время (например, 25:00)
                System.err.println("Ошибка парсинга времени: " + e.getMessage());
            }
        }
        return state;
    }
}