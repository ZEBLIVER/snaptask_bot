package ru.hackathon.max.snaptask_bot.domain.model.parser;

import lombok.Value;
import java.time.LocalDateTime;
import java.util.Optional;

@Value
public class RawParseResult {
    Optional<LocalDateTime> deadline;
    String cleanText;
    Optional<String> recurrenceRule;
}