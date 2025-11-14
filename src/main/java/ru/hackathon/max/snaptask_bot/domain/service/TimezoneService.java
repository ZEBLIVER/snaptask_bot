package ru.hackathon.max.snaptask_bot.domain.service;

import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TimezoneService {

    private final Map<String, String> cityToTimezoneMap;
    private static final Pattern OFFSET_PATTERN = Pattern.compile("^([+\\-])?(\\d{1,2})$");

    public TimezoneService() {
        this.cityToTimezoneMap = Collections.unmodifiableMap(initializeTimezoneMap());
    }

    private Map<String, String> initializeTimezoneMap() {
        Map<String, String> map = new HashMap<>();

        // UTC+3 (Europe/Moscow)
        map.put("москва", "Europe/Moscow");
        map.put("мск", "Europe/Moscow");
        map.put("moscow", "Europe/Moscow");
        map.put("санкт-петербург", "Europe/Moscow");
        map.put("спб", "Europe/Moscow");
        map.put("питер", "Europe/Moscow");
        map.put("казань", "Europe/Moscow");
        map.put("нижний новгород", "Europe/Moscow");
        map.put("самара", "Europe/Samara"); // UTC+4

        // UTC+5 (Asia/Yekaterinburg)
        map.put("екатеринбург", "Asia/Yekaterinburg");
        map.put("екб", "Asia/Yekaterinburg");
        map.put("пермь", "Asia/Yekaterinburg");
        map.put("уфа", "Asia/Yekaterinburg");
        map.put("челябинск", "Asia/Yekaterinburg");

        // UTC+6 (Asia/Omsk)
        map.put("омск", "Asia/Omsk");

        // UTC+7 (Asia/Novosibirsk)
        map.put("новосибирск", "Asia/Novosibirsk");
        map.put("нск", "Asia/Novosibirsk");
        map.put("томск", "Asia/Novosibirsk");
        map.put("красноярск", "Asia/Krasnoyarsk");

        // UTC+8 (Asia/Irkutsk)
        map.put("иркутск", "Asia/Irkutsk");
        map.put("улан-удэ", "Asia/Irkutsk");

        // UTC+9 (Asia/Yakutsk)
        map.put("якутск", "Asia/Yakutsk");

        // UTC+10 (Asia/Vladivostok)
        map.put("владивосток", "Asia/Vladivostok");
        map.put("хабаровск", "Asia/Vladivostok");

        // UTC+11 (Asia/Magadan)
        map.put("магадан", "Asia/Magadan");

        // UTC+12 (Asia/Kamchatka)
        map.put("камчатка", "Asia/Kamchatka");

        // --- СТРАНЫ СНГ ---
        map.put("киев", "Europe/Kyiv");
        map.put("минск", "Europe/Minsk");
        map.put("алматы", "Asia/Almaty");
        map.put("астана", "Asia/Almaty");
        map.put("ташкент", "Asia/Tashkent");

        // --- ДРУГИЕ ПОПУЛЯРНЫЕ МИРОВЫЕ ГОРОДА ---
        map.put("лондон", "Europe/London");
        map.put("нью-йорк", "America/New_York");
        map.put("берлин", "Europe/Berlin");
        map.put("париж", "Europe/Paris");
        map.put("токио", "Asia/Tokyo");

        return map;
    }

    private String normalizeInput(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[ё]", "е")
                .replaceAll("[^a-zа-я\\s-]", "");
    }

    /**
     * Определяет ZoneId по строке ввода (городу, сдвигу или IANA ID).
     */
    public Optional<ZoneId> getTimeZone(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Optional.empty();
        }

        String normalized = normalizeInput(input);

        String timezoneId = cityToTimezoneMap.get(normalized);
        if (timezoneId != null) {
            return Optional.of(ZoneId.of(timezoneId));
        }

        Optional<ZoneId> offsetZone = parseOffset(input);
        if (offsetZone.isPresent()) {
            return offsetZone;
        }

        try {
            return Optional.of(ZoneId.of(input.trim()));
        } catch (Exception e) {
            // Не является валидным Zone ID
        }

        return Optional.empty();
    }

    private Optional<ZoneId> parseOffset(String input) {
        String trimmedInput = input.trim().replace(" ", "").replace("utc", "").replace("gmt", "");
        Matcher matcher = OFFSET_PATTERN.matcher(trimmedInput);

        if (matcher.matches()) {
            String sign = matcher.group(1) != null ? matcher.group(1) : "+";
            int hours = Integer.parseInt(matcher.group(2));

            if (hours > 18) {
                return Optional.empty();
            }

            String offset = String.format("GMT%s%02d:00", sign, hours);
            try {
                return Optional.of(ZoneId.of(offset));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}