package ru.hackathon.max.snaptask_bot.domain.service.parsing;

import org.springframework.stereotype.Service;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;
import ru.hackathon.max.snaptask_bot.domain.service.parsing.handler.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DateParser {

    // --- Вспомогательный класс для возврата результата ---
    public static class ParseResult {
        private final Optional<LocalDateTime> deadline;
        private final String cleanText;
        private final Optional<String> recurrenceRule;

        public ParseResult(Optional<LocalDateTime> deadline, String cleanText, Optional<String> recurrenceRule) {
            this.deadline = deadline;
            this.cleanText = cleanText;
            this.recurrenceRule = recurrenceRule;
        }

        public Optional<LocalDateTime> getDeadline() { return deadline; }
        public String getCleanText() { return cleanText; }
        public Optional<String> getRecurrenceRule() { return recurrenceRule; }
    }
    // --------------------------------------------------------

    private final List<DateParserHandler> parserHandlers;

    public DateParser(List<DateParserHandler> parserHandlers) {
        // !!! КРИТИЧЕСКИЙ ШАГ: Сортировка хэндлеров по приоритету !!!
        // Сортировка гарантирует, что более важные (конкретные) правила обрабатываются первыми.
        this.parserHandlers = parserHandlers.stream()
                .sorted(Comparator.comparing(this::getExecutionOrder))
                .collect(Collectors.toList());
    }

    /**
     * Основной метод, который прогоняет входную строку через Цепочку Ответственности.
     */
    public ParseResult extractDate(String rawInput) {
        // 1. Создаем начальное состояние
        ParsingState state = new ParsingState(rawInput);

        // 2. Прогоняем состояние через все хэндлеры
        for (DateParserHandler handler : parserHandlers) {
            state = handler.handle(state);
        }

        // 3. Возвращаем финальное состояние в виде ParseResult
        return new ParseResult(
                state.getDeadline(),
                state.getRemainingText(),
                state.getRecurrenceRule()
        );
    }

    /**
     * Определяет приоритет выполнения хэндлеров (более низкое число = более высокий приоритет).
     *
     * Порядок обработки:
     * 1. Рекуррентность (должна быть первой, так как не зависит от даты)
     * 2. Конкретные Даты (ДД.ММ.ГГГГ)
     * 3. Конкретные Дни Недели (ПН, ВТ) и Относительные Дни ("завтра")
     * 4. Абсолютное Время (уточняет дату/день, установленный ранее)
     * 5. Относительные Даты ("следующая неделя")
     * 6. Относительное Время ("через 2 часа")
     * 7. Финализатор (устанавливает дефолты и чистит мусор)
     */
    private int getExecutionOrder(DateParserHandler handler) {
        // --- 1. Высокий Приоритет (должен сработать первым, т.к. не зависит от даты/времени) ---
        if (handler instanceof RecurrenceHandler) return 5;       // каждый день

        // --- 2. Средний Приоритет: Установка Конкретной Даты ---
        if (handler instanceof SimpleDateHandler) return 10;       // 25.10.2026
        if (handler instanceof DayOfWeekHandler) return 20;        // Завтра, ПН (менее конкретно, чем дата)

        // --- 3. Средний Приоритет: Уточнение Времени ---
        if (handler instanceof AbsoluteTimeHandler) return 30;     // в 18:30 (корректирует дату/день)

        // --- 4. Низкий Приоритет: Относительные Смещения (должны сработать только если ничего не найдено) ---
        if (handler instanceof RelativeDateHandler) return 40;     // на следующей неделе
        if (handler instanceof RelativeTimeHandler) return 50;     // через 2 часа

        // --- 5. Самый Низкий Приоритет: Финальная обработка ---
        if (handler instanceof FinalizerHandler) return 100;       // Устанавливает дефолтный дедлайн и чистит текст

        return 99; // Неизвестные хэндлеры в середину
    }
}