package ru.hackathon.max.snaptask_bot.domain.model.parser;

import lombok.Value; // Используем @Value для DTO
import java.time.LocalDateTime;
import java.util.Optional;

@Value
public class ParsedTaskDetails {
    String actionText;
    Optional<LocalDateTime> deadline;
    Optional<String> recurrenceRule;
}