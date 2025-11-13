package ru.hackathon.max.snaptask_bot.domain.service.parsing;

import org.springframework.stereotype.Service;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;
import ru.hackathon.max.snaptask_bot.domain.model.RawParseResult;
import ru.hackathon.max.snaptask_bot.domain.service.parsing.handler.*;


import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DateParser {

    private final List<DateParserHandler> parserHandlers;

    public DateParser(List<DateParserHandler> parserHandlers) {
        this.parserHandlers = parserHandlers.stream()
                .sorted(Comparator.comparing(this::getExecutionOrder)) //
                .collect(Collectors.toList());
    }

    /**
     * Основной метод, который прогоняет входную строку через Цепочку Ответственности.
     */
    public RawParseResult extractDate(String rawInput, ZoneId userZoneId) {
        ParsingState state = new ParsingState(rawInput, userZoneId);

        for (DateParserHandler handler : parserHandlers) {
            state = handler.handle(state);
        }

        return new RawParseResult(
                state.getDeadline(),
                state.getRemainingText(),
                state.getRecurrenceRule()
        );
    }

    /**
     * Определяет приоритет выполнения хэндлеров (более низкое число = более высокий приоритет).
     */
    private int getExecutionOrder(DateParserHandler handler) {
        if (handler instanceof RecurrenceHandler) return 5;
        if (handler instanceof SimpleDateHandler) return 10;
        if (handler instanceof DayOfWeekHandler) return 20;
        if (handler instanceof AbsoluteTimeHandler) return 30;
        if (handler instanceof RelativeTimeHandler) return 25;
        if (handler instanceof RelativeDateHandler) return 40;
        if (handler instanceof FinalizerHandler) return 100;
        return 99;
    }
}