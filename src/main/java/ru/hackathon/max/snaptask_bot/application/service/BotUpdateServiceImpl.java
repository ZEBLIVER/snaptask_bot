package ru.hackathon.max.snaptask_bot.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.hackathon.max.snaptask_bot.application.handler.UpdateHandlerFactory;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;

@Async
@Service
public class BotUpdateServiceImpl implements BotUpdateService {
    private static final Logger log = LoggerFactory.getLogger(BotUpdateServiceImpl.class);

    private final UpdateHandlerFactory handlerFactory;

    public BotUpdateServiceImpl(UpdateHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }

    @Override
    public void processIncomingUpdate(MaxUpdateDto updateDto) {
        if (updateDto.getMessage() == null) {
            return;
        }

        Long userId = updateDto.getMessage().getSender().getUserId();
        Long chatId = updateDto.getMessage().getRecipient().getChatId();

        if (userId == null || userId == 0 || chatId == null || chatId == 0) {
            log.debug("Update проигнорирован: невалидный User ID или Chat ID.");
            return;
        }

        handlerFactory.getHandler(updateDto)
                .ifPresentOrElse(
                        (handler) -> handler.handle(updateDto),
                        () -> log.debug("Update проигнорирован: не найден подходящий обработчик для типа: {}", updateDto.getUpdateType())
                );
    }
}