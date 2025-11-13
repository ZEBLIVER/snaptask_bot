package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.ParsingState;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RecurrenceHandler implements DateParserHandler {

    private static final Logger log = LoggerFactory.getLogger(RecurrenceHandler.class);

    private static final Pattern RECURRENCE_MARKER_PATTERN =
            Pattern.compile(
                    "(\\bежедневно\\b|\\bеженедельно\\b|\\bежемесячно\\b|\\bежегодно\\b|" +
                            "\\bкажд(ый|ую|ое|ого)\\s+(день|недел\\w*|месяц\\w*|год\\w*)\\b|" +
                            "\\bкажд(ый|ую|ое|ого)\\s+(" +
                            "понедельник|вторник|сред(а|у)|четверг|пятниц(а|у)|суббот(а|у)|воскресенье|" +
                            "\\bпн\\b|\\bвт\\b|\\bср\\b|\\bчт\\b|\\bпт\\b|\\bсб\\b|\\bвс\\b" +
                            ")\\b" +
                            ")",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
            );

    private static final Pattern DAY_OF_MONTH_PATTERN =
            Pattern.compile("(\\bкаждого\\s+\\d+\\s+числа\\b)",
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);


    @Override
    public ParsingState handle(ParsingState state) {
        if (state.getRecurrenceRule().isPresent()) {
            return state;
        }

        String text = state.getRemainingText();
        String recurrenceRule = null;
        String newText = text;

        Matcher markerMatcher = RECURRENCE_MARKER_PATTERN.matcher(text);

        if (markerMatcher.find()) {
            recurrenceRule = markerMatcher.group(0).trim();

            newText = text.replaceAll(Pattern.quote(recurrenceRule), " ");

            Matcher domMatcher = DAY_OF_MONTH_PATTERN.matcher(newText);
            if (domMatcher.find()) {
                newText = newText.replaceAll(Pattern.quote(domMatcher.group(0)), " ");
            }

            newText = newText.trim().replaceAll("\\s+", " ");

            log.debug("RecurrenceHandler нашел правило: {}", recurrenceRule);

            return state.update(
                    newText,
                    state.getDeadline(),
                    Optional.of(recurrenceRule)
            );
        }

        Matcher complexMatcher = Pattern.compile("(\\bкаждые?\\s+\\d+\\s+(минут|час|день|недел)\\w*\\b)",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS).matcher(text);

        if (complexMatcher.find()) {
            recurrenceRule = complexMatcher.group(0).trim();

            String finalNewText = text.replaceAll(Pattern.quote(recurrenceRule), " ").trim().replaceAll("\\s+", " ");

            log.debug("RecurrenceHandler нашел правило: {}", recurrenceRule);

            return state.update(
                    finalNewText,
                    state.getDeadline(),
                    Optional.of(recurrenceRule)
            );
        }

        return state;
    }
}