package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LinkDto {
    private String type;

    /**
     * Само пересланное сообщение, из которого мы извлекаем текст.
     */
    @JsonProperty("message")
    private ForwardedMessageDto message;

    private SenderDto sender;

    @JsonProperty("chat_id")
    private Long chatId;
}