package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;

public interface DateParserHandler {

    /**
     * Пытается распознать паттерн в тексте.
     *
     * @param input Состояние парсинга (текущий текст, текущий дедлайн).
     * @return Обновленное состояние парсинга.
     */
    ParsingState handle(ParsingState input);
}