package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing;

import lombok.Builder;
import lombok.Value;
import com.fasterxml.jackson.annotation.JsonProperty;

@Value
@Builder
public class AttachmentRequest {

    String type;

    @JsonProperty("payload")
    Keyboard payload;
}