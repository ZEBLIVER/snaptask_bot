package ru.hackathon.max.snaptask_bot.application.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.service.task.TaskCallbackService;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.CallbackPayloadInfo;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackQueryUpdateHandler implements UpdateHandler {

    private final TaskCallbackService taskCallbackService;

    /**
     * Проверяет, является ли обновление нажатием кнопки (message_callback).
     */
    @Override
    public boolean canHandle(MaxUpdateDto updateDto) {
        return "message_callback".equals(updateDto.getUpdateType());
    }

    /**
     * Обрабатывает нажатие кнопки.
     */
    @Override
    public void handle(MaxUpdateDto updateDto) {
        CallbackPayloadInfo callback = updateDto.getCallback();

        if (callback == null) {
            log.error("Получен message_callback без данных о callback.");
            return;
        }

        String callbackId = callback.getCallbackId();
        String payload = callback.getPayload();

        taskCallbackService.processCallback(callbackId, payload);

        log.debug("✅ Обработка callback-запроса завершена.");
    }
}