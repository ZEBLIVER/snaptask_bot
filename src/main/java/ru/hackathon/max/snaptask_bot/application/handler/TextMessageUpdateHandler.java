package ru.hackathon.max.snaptask_bot.application.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.service.task.TaskProcessorService;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MessageDto;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;

import java.util.Optional;

@Component
@Order(50)
public class TextMessageUpdateHandler implements UpdateHandler {
    private static final Logger log = LoggerFactory.getLogger(TextMessageUpdateHandler.class);
    private final TaskProcessorService taskProcessorService;

    public TextMessageUpdateHandler(TaskProcessorService taskProcessorService) {
        this.taskProcessorService = taskProcessorService;
    }

    /**
     * Статический метод для надежного извлечения текста сообщения,
     * учитывая, что сообщение может быть переслано (forwarded).
     * @param messageDto Объект MaxMessageDto
     * @return Очищенный текст сообщения или пустой Optional
     */
    public static Optional<String> extractText(MessageDto messageDto) {
        String text = messageDto.getBody().getText();

        if (text != null && !text.trim().isEmpty()) {
            return Optional.of(text.trim());
        }

        if (messageDto.getLink() != null &&
                messageDto.getLink().getMessage() != null &&
                messageDto.getLink().getMessage().getText() != null &&
                !messageDto.getLink().getMessage().getText().trim().isEmpty()) {

            return Optional.of(messageDto.getLink().getMessage().getText().trim());
        }

        return Optional.empty();
    }


    private static final String NOTIFICATION_MARKER = "🔔 Напоминание:";

    @Override
    public boolean canHandle(MaxUpdateDto updateDto) {
        // Сначала убедитесь, что это не callback
        if ("message_callback".equals(updateDto.getUpdateType())) {
            return false; // Это должен обработать CallbackQueryUpdateHandler
        }

        Optional<String> textOptional = extractText(updateDto.getMessage());

        if (textOptional.isEmpty()) {
            return false;
        }

        String text = textOptional.get();

        // Игнорируем текст, который является нашим напоминанием
        if (text.startsWith(NOTIFICATION_MARKER)) {
            return false;
        }

        return true;
    }

    @Override
    public void handle(MaxUpdateDto updateDto) {
        Long userId = updateDto.getMessage().getSender().getUserId();
        String username = updateDto.getMessage().getSender().getName();

        Optional<String> rawTextOptional = extractText(updateDto.getMessage());

        if (rawTextOptional.isPresent()) {
            String rawText = rawTextOptional.get();
            log.info("📢 Начинаю обработку текстового сообщения от User={} (текст: '{}')", userId, rawText);

            taskProcessorService.processMessage(userId, rawText, username);

            log.info("✅ Обработка текстового сообщения завершена.");
        }
    }
}