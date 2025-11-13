package ru.hackathon.max.snaptask_bot.domain.service.parsing;

import org.springframework.stereotype.Service;
import ru.hackathon.max.snaptask_bot.domain.model.ParsedTaskDetails;
import ru.hackathon.max.snaptask_bot.domain.model.TaskType;

import java.util.Optional;

@Service
public class SmartParsingService {

    private final DateParser dateParser;
    private final TypeClassifier typeClassifier;

    // Spring автоматически внедрит зависимости (DateParser и TypeClassifier)
    public SmartParsingService(DateParser dateParser, TypeClassifier typeClassifier) {
        this.dateParser = dateParser;
        this.typeClassifier = typeClassifier;
    }

    /**
     * Основной метод для парсинга входящего сообщения от пользователя.
     * @param rawInput Сырое текстовое сообщение.
     * @return Объект ParsedTaskDetails с разобранными компонентами.
     */
    public ParsedTaskDetails parse(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            // Возвращаем пустую задачу, если сообщение пустое
            return new ParsedTaskDetails("", Optional.empty(), Optional.empty());
        }

        // 1. Извлечение даты, времени и правила повторяемости
        // DateParser возвращает ParseResult, который содержит очищенный текст.
        DateParser.ParseResult dateParseResult = dateParser.extractDate(rawInput);

        String cleanActionText = dateParseResult.getCleanText();
        return new ParsedTaskDetails(
                cleanActionText,
                dateParseResult.getDeadline(),
                dateParseResult.getRecurrenceRule()
        );
    }
}