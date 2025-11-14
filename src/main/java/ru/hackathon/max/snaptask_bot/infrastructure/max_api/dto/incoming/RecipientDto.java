package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RecipientDto {
    @JsonProperty("chat_id")
    private Long chatId;

    @JsonProperty("chat_type")
    private String chatType;

    @JsonProperty("user_id")
    private Long userId;
}
