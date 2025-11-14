package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class    MaxUpdateDto {
    private Long timestamp;

    private MessageDto message;
    @JsonProperty("user_id")

    private Long userId;

    private SenderDto user;

    @JsonProperty("user_locale")
    private String userLocale;

    @JsonProperty("update_type")
    private String updateType;

    @JsonProperty("callback")
    private CallbackPayloadInfo callback;
}
