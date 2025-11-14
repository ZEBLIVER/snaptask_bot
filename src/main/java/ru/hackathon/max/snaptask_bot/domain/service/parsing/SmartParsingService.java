package ru.hackathon.max.snaptask_bot.domain.service.parsing;

import org.springframework.stereotype.Service;
import ru.hackathon.max.snaptask_bot.domain.model.parser.ParsedTaskDetails;
import ru.hackathon.max.snaptask_bot.domain.model.parser.RawParseResult;

import java.time.ZoneId;
import java.util.Optional;

@Service
public class SmartParsingService {

    private final DateParser dateParser;

    public SmartParsingService(DateParser dateParser) {
        this.dateParser = dateParser;
    }

    /**
     * Основной метод для парсинга входящего сообщения от пользователя.
     * @param rawInput Сырое текстовое сообщение.
     * @param userZoneId Часовой пояс пользователя.
     * @return Объект ParsedTaskDetails с разобранными компонентами.
     */
    public ParsedTaskDetails parse(String rawInput, ZoneId userZoneId) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return new ParsedTaskDetails("", Optional.empty(), Optional.empty());
        }

        RawParseResult dateParseResult = dateParser.extractDate(rawInput, userZoneId);

        return new ParsedTaskDetails(
                dateParseResult.getCleanText(),
                dateParseResult.getDeadline(),
                dateParseResult.getRecurrenceRule()
        );
    }
}