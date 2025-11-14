package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming;

import lombok.Data;

@Data
public class AttachmentDto {
    private String type;
    private Double latitude;
    private Double longitude;
}
