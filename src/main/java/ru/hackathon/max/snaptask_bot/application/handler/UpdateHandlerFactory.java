package ru.hackathon.max.snaptask_bot.application.handler;

import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;

import java.util.List;
import java.util.Optional;

@Component
public class UpdateHandlerFactory {
    private final List<UpdateHandler> handlers;

    public UpdateHandlerFactory(List<UpdateHandler> handlers) {
        this.handlers = handlers;
    }

    /**
     * Находит подходящий обработчик для данного обновления.
     * Сначала проверяются более специфичные (TextMessage), затем общие (UnsupportedMedia).
     */
    public Optional<UpdateHandler> getHandler(MaxUpdateDto updateDto) {
        return handlers.stream()
                .filter(handler -> handler.canHandle(updateDto))
                .findFirst();
    }
}
