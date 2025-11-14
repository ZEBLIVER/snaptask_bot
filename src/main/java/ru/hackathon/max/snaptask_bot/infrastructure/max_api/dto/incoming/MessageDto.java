package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MessageDto {
    private RecipientDto recipient;
    private Long timestamp;
    private BodyDto body;
    private SenderDto sender;
    @JsonProperty("link")
    private LinkDto link;
}
