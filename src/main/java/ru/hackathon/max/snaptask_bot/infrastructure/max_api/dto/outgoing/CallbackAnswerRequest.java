package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CallbackAnswerRequest {
    private MessageBody message;
    private String notification;
    private Boolean notify;
}