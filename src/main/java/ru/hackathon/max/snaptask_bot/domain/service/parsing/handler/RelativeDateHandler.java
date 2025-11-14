package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.parser.ParsingState;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RelativeDateHandler implements DateParserHandler {

    private static final Pattern RELATIVE_DATE_PATTERN = Pattern.compile(
            "\\b(?:на\\s+)?(следующей|след)\\s+(неделе|мес|месяце|году)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final LocalTime DEFAULT_TIME = LocalTime.of(10, 0);

    @Override
    public ParsingState handle(ParsingState input) {
        if (input.isDeadlineSet()) {
            return input;
        }

        String textToSearch = input.getRemainingText();
        Matcher matcher = RELATIVE_DATE_PATTERN.matcher(textToSearch);

        if (!matcher.find()) {
            return input;
        }

        ZoneId userZoneId = input.getUserZoneId();
        LocalDateTime now = ZonedDateTime.now(userZoneId).toLocalDateTime().withSecond(0).withNano(0);

        LocalDateTime newDeadline;

        String unit = matcher.group(2).toLowerCase();

        if (unit.startsWith("нед")) {
            newDeadline = now.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).with(DEFAULT_TIME);
        } else if (unit.startsWith("мес")) {
            newDeadline = now.with(TemporalAdjusters.firstDayOfNextMonth()).with(DEFAULT_TIME);
        } else if (unit.startsWith("год")) {
            newDeadline = now.with(TemporalAdjusters.firstDayOfNextYear()).with(DEFAULT_TIME);
        } else {
            return input;
        }

        String foundPhrase = matcher.group(0);
        String newRemainingText = textToSearch.replaceAll("(?i)" + Pattern.quote(foundPhrase), " ")
                .replaceAll("\\s+", " ")
                .trim();

        return input.update(
                newRemainingText,
                Optional.of(newDeadline),
                input.getRecurrenceRule()
        );
    }
}