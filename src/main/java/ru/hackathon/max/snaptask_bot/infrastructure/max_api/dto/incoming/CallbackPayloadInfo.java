package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CallbackPayloadInfo {
    @JsonProperty("callback_id")
    private String callbackId;

    @JsonProperty("payload")
    private String payload;
}