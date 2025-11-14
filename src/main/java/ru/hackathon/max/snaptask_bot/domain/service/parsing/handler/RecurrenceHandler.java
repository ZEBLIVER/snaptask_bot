package ru.hackathon.max.snaptask_bot.domain.service.parsing.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.parser.ParsingState;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RecurrenceHandler implements DateParserHandler {

    private static final Logger log = LoggerFactory.getLogger(RecurrenceHandler.class);


    public static final String DAILY = "DAILY";
    public static final String WEEKLY = "WEEKLY";
    public static final String MONTHLY = "MONTHLY";
    public static final String YEARLY = "YEARLY";


    public enum RecurrenceUnit {
        MINUTES,
        HOURS,
        DAYS,
        WEEKS
    }
    /** Общие фразы типа "ежедневно", "каждый месяц" */
    private static final Pattern COMMON_RECURRENCE_PATTERN = Pattern.compile(
            "(ежедневно|еженедельно|ежемесячно|ежегодно|" +
                    "кажд(ый|ую|ое|ого)?\\s+(день|недел\\w*|месяц\\w*|год\\w*))",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    /** Каждую среду, каждый вторник */
    private static final Pattern DAY_OF_WEEK_PATTERN = Pattern.compile(
            "кажд(ый|ую|ое|ого)?\\s+(понедельник|вторник|сред[ау]|четверг|" +
                    "пятниц[ау]|суббот[ау]|воскресенье|пн|вт|ср|чт|пт|сб|вс)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    /** Каждого 5 числа */
    private static final Pattern DAY_OF_MONTH_PATTERN = Pattern.compile(
            "каждого\\s+\\d+\\s+числа",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );

    /** Каждые 3 дня / каждые 10 минут / каждые 5 недель */
    private static final Pattern COMPLEX_RECURRENCE_PATTERN = Pattern.compile(
            "каждые?\\s+(\\d+)\\s+(минут\\w*|час\\w*|дн\\w*|недел\\w*)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS
    );



    @Override
    public ParsingState handle(ParsingState state) {

        if (state.getRecurrenceRule().isPresent()) {
            return state;
        }

        String text = state.getRemainingText();
        String recurrence = null;
        String matchedPhrase = null;


        Matcher m1 = COMMON_RECURRENCE_PATTERN.matcher(text);
        if (m1.find()) {
            matchedPhrase = m1.group(0);
            recurrence = mapCommonPhraseToToken(matchedPhrase);
        }

        if (recurrence == null) {
            Matcher m2 = DAY_OF_WEEK_PATTERN.matcher(text);
            if (m2.find()) {
                matchedPhrase = m2.group(0);
                recurrence = WEEKLY;
            }
        }

        if (recurrence == null) {
            Matcher m3 = DAY_OF_MONTH_PATTERN.matcher(text);
            if (m3.find()) {
                matchedPhrase = m3.group(0);
                recurrence = MONTHLY;
            }
        }



        if (recurrence == null) {
            Matcher m4 = COMPLEX_RECURRENCE_PATTERN.matcher(text);
            if (m4.find()) {
                matchedPhrase = m4.group(0);

                int interval = Integer.parseInt(m4.group(1));
                String unitRaw = m4.group(2);

                RecurrenceUnit unit = mapUnit(unitRaw);

                if (unit != null) {
                    recurrence = buildEveryToken(interval, unit);
                } else {
                    recurrence = matchedPhrase; // fallback
                }
            }
        }

        if (recurrence == null) {
            return state;
        }

        String newText = text
                .replace(matchedPhrase, " ")
                .trim()
                .replaceAll("\\s+", " ");

        log.debug("RecurrenceHandler нашел правило: {}", recurrence);

        return state.update(
                newText,
                state.getDeadline(),
                Optional.of(recurrence)
        );
    }


    /** DAIlY/WEEKLY/MONTHLY/YEARLY */
    private String mapCommonPhraseToToken(String phrase) {
        String p = phrase.toLowerCase(Locale.ROOT);

        if (p.contains("ежеднев") || p.contains("день")) return DAILY;
        if (p.contains("еженедел") || p.contains("недел")) return WEEKLY;
        if (p.contains("ежемесяч") || p.contains("месяц")) return MONTHLY;
        if (p.contains("ежегод") || p.contains("год")) return YEARLY;

        return null;
    }

    /** Каждые N UNIT → EVERY:N:UNIT */
    private String buildEveryToken(int interval, RecurrenceUnit unit) {
        return "EVERY:" + interval + ":" + unit.name();
    }

    /** Русские слова → RecurrenceUnit */
    private RecurrenceUnit mapUnit(String raw) {
        raw = raw.toLowerCase(Locale.ROOT);

        if (raw.startsWith("минут")) return RecurrenceUnit.MINUTES;
        if (raw.startsWith("час"))   return RecurrenceUnit.HOURS;
        if (raw.startsWith("дн"))    return RecurrenceUnit.DAYS;
        if (raw.startsWith("недел")) return RecurrenceUnit.WEEKS;

        return null;
    }
}
