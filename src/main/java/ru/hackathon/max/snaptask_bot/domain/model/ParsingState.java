package ru.hackathon.max.snaptask_bot.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Getter
public class ParsingState {
    private String remainingText;
    private final Optional<LocalDateTime> deadline;
    private final Optional<String> recurrenceRule;
    private final ZoneId userZoneId;

    public ParsingState(String initialText, ZoneId userZoneId) {
        this.remainingText = initialText.toLowerCase().trim();
        this.deadline = Optional.empty();
        this.recurrenceRule = Optional.empty();
        this.userZoneId = userZoneId; // <--- СОХРАНЕНИЕ
    }

    private ParsingState(String remainingText, Optional<LocalDateTime> deadline, Optional<String> recurrenceRule, ZoneId userZoneId) {
        this.remainingText = remainingText;
        this.deadline = deadline;
        this.recurrenceRule = recurrenceRule;
        this.userZoneId = userZoneId;
    }

    public ParsingState update(String newRemainingText, Optional<LocalDateTime> newDeadline, Optional<String> newRecurrenceRule) {
        return new ParsingState(newRemainingText, newDeadline, newRecurrenceRule, this.userZoneId);
    }

    public boolean isDeadlineSet() {
        return deadline.isPresent();
    }
}