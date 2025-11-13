package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@Component
public class FinalizerHandler implements DateParserHandler {

    private static final Logger log = LoggerFactory.getLogger(FinalizerHandler.class);

    @Override
    public ParsingState handle(ParsingState state) {
        Optional<LocalDateTime> deadline = state.getDeadline();

        ZoneId userZoneId = state.getUserZoneId();
        LocalDateTime now = ZonedDateTime.now(userZoneId).toLocalDateTime();

        String cleanText;

        if (deadline.isEmpty()) {
            LocalDateTime defaultDeadline = now.plusHours(1)
                    .withSecond(0)
                    .withNano(0);
            deadline = Optional.of(defaultDeadline);
            log.debug("FinalizerHandler: Дедлайн не найден, установлен дефолт: {}", defaultDeadline);
        } else {
            log.debug("FinalizerHandler: Дедлайн уже установлен: {}", deadline.get());
        }

        cleanText = cleanActionText(state.getRemainingText());
        log.debug("FinalizerHandler: Финальный текст задачи: '{}'", cleanText);

        return state.update(
                cleanText,
                deadline,
                state.getRecurrenceRule()
        );
    }


    private String cleanActionText(String text) {
        if (text == null) return "";

        text = text.replaceAll("[\\s,.]+", " ");

        return text.trim();
    }
}