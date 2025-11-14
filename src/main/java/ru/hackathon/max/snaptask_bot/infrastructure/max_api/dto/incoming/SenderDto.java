package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SenderDto {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("is_bot")
    private Boolean isBot;

    @JsonProperty("last_activity_time")
    private Long lastActivityTime;

    private String name;
}
