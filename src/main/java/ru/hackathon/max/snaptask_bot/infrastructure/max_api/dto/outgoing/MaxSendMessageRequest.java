package ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class MaxSendMessageRequest {

    String text;
    String format;
    Boolean notify;
    Boolean disable_link_preview;
    List<AttachmentRequest> attachments;
}