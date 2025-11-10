package ru.hackathon.max.snaptask_bot.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
public class ParsingState {
    private String remainingText;
    private final Optional<LocalDateTime> deadline;
    private final Optional<String> recurrenceRule;

    public ParsingState(String initialText) {
        this.remainingText = initialText.toLowerCase().trim();
        this.deadline = Optional.empty();
        this.recurrenceRule = Optional.empty();
    }

    private ParsingState(String remainingText, Optional<LocalDateTime> deadline, Optional<String> recurrenceRule) {
        this.remainingText = remainingText;
        this.deadline = deadline;
        this.recurrenceRule = recurrenceRule;
    }

    public ParsingState update(String newRemainingText, Optional<LocalDateTime> newDeadline, Optional<String> newRecurrenceRule) {
        return new ParsingState(newRemainingText, newDeadline, newRecurrenceRule);
    }

    public boolean isDeadlineSet() {
        return deadline.isPresent();
    }
}