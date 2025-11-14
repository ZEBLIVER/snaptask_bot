package ru.hackathon.max.snaptask_bot.infrastructure.max_api.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import ru.hackathon.max.snaptask_bot.domain.port.out.MaxMessageSenderPort;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.client.MaxApiClient;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing.*;

import java.util.List;

@Service
public class MaxApiMessageAdapter implements MaxMessageSenderPort {
    private static final Logger log = LoggerFactory.getLogger(MaxApiMessageAdapter.class);

    private final MaxApiClient maxApiClient;

    public MaxApiMessageAdapter(MaxApiClient maxApiClient) {
        this.maxApiClient = maxApiClient;
    }


    @Override
    public void sendMessage(Long recipientId, String text) {
        this.sendMessage(recipientId, text, null);
    }


    @Override
    public void sendMessage(Long recipientId, String text, Keyboard keyboard) {
        if (recipientId == null || text == null || text.isBlank()) {
            log.warn("Попытка отправить пустое сообщение или без ID получателя.");
            return;
        }

        List<AttachmentRequest> attachments = null;
        if (keyboard != null) {
            attachments = List.of(
                    AttachmentRequest.builder()
                            .type("inline_keyboard")
                            .payload(keyboard)
                            .build()
            );
        }

        MaxSendMessageRequest requestBody = MaxSendMessageRequest.builder()
                .text(text)
                .format("markdown")
                .attachments(attachments)
                .notify(true)
                .build();

        try {
            maxApiClient.send(recipientId, requestBody);

            String logMessage = keyboard != null ? "Сообщение с клавиатурой" : "Сообщение";
            log.debug("{} успешно отправлено в чат {}", logMessage, recipientId);

        } catch (RestClientException e) {
            log.error("Ошибка REST-клиента при отправке сообщения в Max API для чата {}: {}", recipientId, e.getMessage());
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при отправке сообщения в чат {}: {}", recipientId, e.getMessage(), e);
        }
    }

    @Override
    public void sendCallbackAnswer(String callbackId, CallbackAnswerRequest request) {
        if (callbackId == null || callbackId.isBlank()) {
            log.warn("Попытка отправить ответ на callback без callbackId.");
            return;
        }

        try {
            maxApiClient.sendCallbackAnswer(callbackId, request);

            log.debug("Ответ на callback ID {} успешно отправлен.", callbackId);

        } catch (RestClientException e) {
            log.error("Ошибка REST-клиента при отправке ответа на callback ID {}: {}", callbackId, e.getMessage());
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при отправке ответа на callback ID {}: {}", callbackId, e.getMessage(), e);
        }

    }
}