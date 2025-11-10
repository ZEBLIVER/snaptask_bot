package ru.hackathon.max.snaptask_bot.domain.service.parsing;

import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.TaskType;

import java.util.*;

/**
 * Определяет тип задачи по ключевым словам.
 */

@Component
public class TypeClassifier {

    private static final Map<TaskType, String[]> KEYWORDS_MAP;

    static {

        KEYWORDS_MAP = Map.of(TaskType.ACADEMIC, new String[]{
                "отчет", "курсовая", "диплом", "лабораторная", "презентация",
                "экзамен", "сессия", "реферат", "задание", "лекция", "учеба", "подготовиться"
        }, TaskType.PERSONAL, new String[]{
                "позвонить", "купить", "встреча", "сходить", "забрать",
                "починить", "врач", "аптека", "магазин", "оплатить", "дом", "счет", "родители"
        }, TaskType.WORK, new String[]{
                "совещание", "отправить", "письмо", "отчет", "согласовать",
                "проект", "заказчик", "клиент", "дедлайн", "задача", "коллега", "встреча"
        }, TaskType.HABIT, new String[]{
                "спорт", "тренировка", "пробежка", "медитация", "почитать",
                "английский", "урок", "вода", "привычка", "учить", "гимнастика", "сделать"
        });
    }

    public TaskType classify(String cleanText) {
        if (cleanText == null || cleanText.trim().isEmpty()) {
            return TaskType.UNDEFINED;
        }
        final String lowerCaseText = cleanText.toLowerCase();

        Optional<TaskType> foundType = KEYWORDS_MAP.entrySet().stream()
                .filter(entry -> Arrays.stream(entry.getValue())
                        .anyMatch(lowerCaseText::contains))
                .map(Map.Entry::getKey)
                .findFirst();

        return foundType.orElse(TaskType.UNDEFINED);
    }
}
