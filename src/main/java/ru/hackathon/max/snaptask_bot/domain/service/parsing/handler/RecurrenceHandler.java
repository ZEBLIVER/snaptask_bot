package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RecurrenceHandler implements DateParserHandler {

    // Упрощенный паттерн для MVP: "каждые 10 минут", "каждый день"
    private static final Pattern RECURRENCE_PATTERN =
            Pattern.compile("каждые?\\s+([\\d\\w\\s]+?)(минут|час|день|нед)\\w*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);

    @Override
    public ParsingState handle(ParsingState state) {
        if (state.getRecurrenceRule().isPresent()) {
            return state; // Правило уже найдено
        }

        String text = state.getRemainingText();
        Matcher matcher = RECURRENCE_PATTERN.matcher(text);

        if (matcher.find()) {
            String rule = matcher.group(0).trim(); // Вся найденная фраза как правило
            String newText = text.replace(rule, " ").trim();

            return state.update(
                    newText,
                    state.getDeadline(),
                    Optional.of(rule) // Сохраняем правило
            );
        }
        return state;
    }
}