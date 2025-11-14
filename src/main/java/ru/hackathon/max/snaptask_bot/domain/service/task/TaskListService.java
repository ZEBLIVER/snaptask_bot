package ru.hackathon.max.snaptask_bot.domain.service.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hackathon.max.snaptask_bot.domain.port.out.TaskRepository;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskEntity;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskStatus;
import ru.hackathon.max.snaptask_bot.domain.service.UserService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskListService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /**
     * Формирует текстовый список всех активных задач пользователя.
     * @param maxUserId ID пользователя в системе MAX.
     * @return Форматированный текст со списком задач или сообщением об их отсутствии.
     */
    public String getActiveTasksListText(Long maxUserId) {

        ZoneId userZoneId = userService.getUserTimezone(maxUserId);

        List<TaskEntity> activeTasks = taskRepository.findByUser_MaxUserIdAndStatusNot(
                maxUserId,
                TaskStatus.COMPLETED // Исключаем завершенные задачи
        );

        if (activeTasks.isEmpty()) {
            return "✅ У вас нет активных задач.";
        }

        activeTasks.sort(Comparator.comparing(
                TaskEntity::getDeadline,
                Comparator.nullsLast(Instant::compareTo)
        ));

        String taskList = activeTasks.stream()
                .map(task -> formatTaskEntity(task, userZoneId))
                .collect(Collectors.joining("\n\n"));

        return "🗒️ **Список ваших активных задач:**\n\n" + taskList;
    }

    /**
     * Форматирует одну задачу в читаемую строку с использованием Markdown.
     * @param userZoneId Актуальный часовой пояс пользователя.
     */
    private String formatTaskEntity(TaskEntity task, ZoneId userZoneId) {
        String deadlineText;
        if (task.getDeadline() != null) {

            deadlineText = task.getDeadline()
                    .atZone(userZoneId)
                    .format(DATE_FORMATTER);
        } else {
            deadlineText = "Без срока";
        }

        return String.format(
                "• **%s** (ID: %d)\n   _Срок:_ %s | _Статус:_ %s",
                task.getActionText(),
                task.getId(),
                deadlineText,
                task.getStatus().name()
        );
    }
}