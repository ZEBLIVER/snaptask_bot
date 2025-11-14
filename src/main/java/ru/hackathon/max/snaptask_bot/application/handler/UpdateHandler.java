package ru.hackathon.max.snaptask_bot.application.handler;

import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;

public interface UpdateHandler {
    /**
     * Проверяет, может ли этот обработчик обработать данное обновление.
     */
    boolean canHandle(MaxUpdateDto updateDto);

    /**
     * Обрабатывает обновление.
     */
    void handle(MaxUpdateDto updateDto);
}
