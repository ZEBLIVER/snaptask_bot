package ru.hackathon.max.snaptask_bot.application.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.port.out.MaxMessageSenderPort;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.AttachmentDto;

import java.util.List;

@Component
public class UnsupportedMediaUpdateHandler implements UpdateHandler {
    private static final Logger log = LoggerFactory.getLogger(UnsupportedMediaUpdateHandler.class);

     private final MaxMessageSenderPort maxMessageSenderPort;

    public UnsupportedMediaUpdateHandler(MaxMessageSenderPort maxMessageSenderPort) {
        this.maxMessageSenderPort = maxMessageSenderPort;
    }


    @Override
    public boolean canHandle(MaxUpdateDto updateDto) {

        boolean hasText = updateDto.getMessage().getBody().getText() != null && !updateDto.getMessage().getBody().getText().trim().isEmpty();

        List<AttachmentDto> attachments = updateDto.getMessage().getBody().getAttachments();
        boolean hasAttachments = attachments != null && !attachments.isEmpty();


        return !hasText && hasAttachments;
    }

    @Override
    public void handle(MaxUpdateDto updateDto) {
        Long userId = updateDto.getMessage().getSender().getUserId();

        String type = updateDto.getMessage().getBody().getAttachments().get(0).getType();

        log.warn("Сообщение от User {} проигнорировано. Неподдерживаемый тип: '{}'", userId, type);


        maxMessageSenderPort.sendMessage(userId, helpMessage);
    }


     private String helpMessage  = "Привет! Вы можете создавать напоминания просто отправляя мне сообщения вида:" +
             " \"позвонить в банк через 10 минут\", \"взять документы завтра в 10\", " +
             "\"ежедневно в 9 утра: проверить почту\" т.д.";
}