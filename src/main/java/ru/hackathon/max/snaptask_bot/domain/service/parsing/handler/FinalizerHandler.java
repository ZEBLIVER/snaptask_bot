package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class FinalizerHandler implements DateParserHandler {

    @Override
    public ParsingState handle(ParsingState state) {
        String cleanText = cleanActionText(state.getRemainingText());
        Optional<LocalDateTime> deadline = state.getDeadline();

        // 1. Устанавливаем дефолтный дедлайн, если он не был установлен (сегодня + 1 час)
        if (deadline.isEmpty()) {
            deadline = Optional.of(LocalDateTime.now().plusHours(1).withSecond(0).withNano(0));
        }

        // 2. Обновляем состояние с очищенным текстом и (возможно) дефолтным дедлайном
        return state.update(
                cleanText,
                deadline,
                state.getRecurrenceRule()
        );
    }

    /**
     * Удаляет оставшиеся лишние слова (предлоги, разделители) из текста действия.
     */
    private String cleanActionText(String text) {
        // Убираем лишние символы и двойные пробелы
        text = text.replaceAll("[\\s,\\.]+", " ").trim();
        // Убираем предлоги, которые могли остаться
        String[] stopWords = {"в", "к", "до", "на", "по", "с", "и"};
        for (String word : stopWords) {
            // \\b - граница слова, чтобы не задеть "квас" в слове "квасовар"
            text = text.replaceAll("\\b" + word + "\\b", "").trim();
        }
        // Убираем множественные пробелы
        return text.replaceAll("\\s+", " ").trim();
    }
}