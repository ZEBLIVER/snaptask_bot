package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming;

import lombok.Data;

@Data
public class ForwardedMessageDto {
    private String mid;
    private Long seq;
    private String text; // <-- Наш искомый текст
}