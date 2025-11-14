package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming;

import lombok.Data;

import java.util.List;

@Data
public class BodyDto {
    private String mid;
    private Long seq;
    private String text;
    private List<AttachmentDto> attachments;
}
