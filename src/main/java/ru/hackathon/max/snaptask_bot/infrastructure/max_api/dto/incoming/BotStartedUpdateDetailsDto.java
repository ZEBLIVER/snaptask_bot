package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class BotStartedUpdateDetailsDto {

    @JsonProperty("user_id")
    private Long userId;

    private SenderDto user;

    @JsonProperty("update_type")
    private String updateType;
}