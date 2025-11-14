package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing;

import lombok.Builder;
import lombok.Value;

/**
 * DTO, представляющий одну кнопку в составе Inline-клавиатуры.
 */
@Value
@Builder
public class Button {

    @Builder.Default
    String type = "callback";

    String text;

    String payload;

    String intent;
}