package ru.hackathon.max.snaptask_bot.domain.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.hackathon.max.snaptask_bot.domain.model.parser.ParsedTaskDetails;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskEntity;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskStatus;
import ru.hackathon.max.snaptask_bot.domain.port.out.MaxMessageSenderPort;
import ru.hackathon.max.snaptask_bot.domain.port.out.TaskRepository;
import ru.hackathon.max.snaptask_bot.domain.service.parsing.SmartParsingService;
import ru.hackathon.max.snaptask_bot.domain.service.UserService;
import ru.hackathon.max.snaptask_bot.domain.service.TimezoneService;
import ru.hackathon.max.snaptask_bot.domain.model.user.UserStatus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskProcessorService {

    private final UserService userService;
    private final SmartParsingService parsingService;
    private final TaskCreationService taskCreationService;
    private final MaxMessageSenderPort messageSenderPort;
    private final TimezoneService timezoneService;
    private final TaskRepository taskRepository;

    private static final String MSG_WELCOME = "Привет! Я бот для умных напоминаний. Перед началом работы, пожалуйста, отправьте мне название своего **города**.";

    private static final String MSG_TIMEZONE_FAILURE = "Не удалось определить часовой пояс по '%s'.\n\n" +
            "Пожалуйста, попробуйте:\n" +
            "- Ввести ближайший крупный город, например, **'Москва'** или **'спб'**.\n" +
            "- Ввести короткий сдвиг от UTC, например, **'+3'** или **'-5'**.";

    private static final String MSG_TASK_CREATED = "✅ Задача **%s** успешно запланирована на **%s**!";
    private static final String MSG_TASK_CREATED_NO_DEADLINE = "✅ Задача **%s** сохранена, но без дедлайна.";

    private static final String MSG_NO_ACTIVE_TASKS = "✅ У вас нет активных задач! 🎉\n\nСоздайте новую задачу, просто написав мне об этом.";
    private static final String MSG_ACTIVE_TASKS_HEADER = "📝 Ваши активные задачи:\n\n";

    private static final DateTimeFormatter USER_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd.MM.yyyy в HH:mm");


    public void processMessage(Long userId, String rawText, String username) {
        Optional<UserStatus> userStatusOptional = Optional.ofNullable(userService.getUserStatus(userId));

        if (userStatusOptional.isEmpty()) {
            userService.registerNewUser(userId, username);
            messageSenderPort.sendMessage(userId, MSG_WELCOME);
            return;
        }

        UserStatus status = userStatusOptional.get();

        if (status == UserStatus.AWAITING_TIMEZONE) {
            handleTimezoneInput(userId, rawText);
            return;
        }

        if (status == UserStatus.REGISTERED) {
            handleTaskInput(userId, rawText);
        }
    }

    /**
     * Обрабатывает команду /list: получает активные задачи пользователя и отправляет список.
     */
    public void listActiveTasks(Long userId) {
        try {
            ZoneId userZoneId = userService.getUserTimezone(userId);

            List<TaskEntity> activeTasks = taskRepository
                    .findByUser_MaxUserIdAndStatusNot(userId, TaskStatus.COMPLETED);

            String responseText;
            if (activeTasks.isEmpty()) {
                responseText = MSG_NO_ACTIVE_TASKS;
            } else {
                responseText = formatTaskList(activeTasks, userZoneId);
            }

            messageSenderPort.sendMessage(userId, responseText);
            log.info("Sent active task list to user {}. Count: {}", userId, activeTasks.size());

        } catch (Exception e) {
            log.error("Unhandled error during task list processing for user {}: {}", userId, e.getMessage(), e);
            messageSenderPort.sendMessage(userId, "Произошла внутренняя ошибка при получении списка задач. Попробуйте позже.");
        }
    }

    private String formatTaskList(List<TaskEntity> tasks, ZoneId userZoneId) {
        StringBuilder sb = new StringBuilder(MSG_ACTIVE_TASKS_HEADER);

        int index = 1;
        for (TaskEntity task : tasks) {
            String deadline = task.getDeadline() != null
                    ? formatDeadline(task.getDeadline(), userZoneId)
                    : "";

            sb.append(String.format("%d. **%s**%s\n", index++, task.getActionText(), deadline));
        }
        return sb.toString();
    }

    private String formatDeadline(Instant deadline, ZoneId userZoneId) {
        return String.format(" (Дедлайн: %s)", deadline.atZone(userZoneId).format(USER_DATE_FORMATTER));
    }


    private void handleTimezoneInput(Long userId, String rawText) {
        Optional<ZoneId> zoneIdOptional = timezoneService.getTimeZone(rawText);

        if (zoneIdOptional.isPresent()) {
            ZoneId zoneId = zoneIdOptional.get();

            userService.completeRegistration(userId, zoneId);

            String successMsg = String.format("Отлично! Ваш часовой пояс (%s) сохранен. " +
                    "Его всегда можно поменять в настройках. Готов создать напоминание, " +
                    "напишите его в свободном формате.", zoneId.getId());
            messageSenderPort.sendMessage(userId, successMsg);

            log.info("User {} successfully registered timezone: {}", userId, zoneId.getId());
        } else {
            String failureMsg = String.format(MSG_TIMEZONE_FAILURE, rawText);

            messageSenderPort.sendMessage(userId, failureMsg);
            log.warn("User {} sent invalid timezone input: {}", userId, rawText);
        }
    }

    private void handleTaskInput(Long userId, String rawText) {
        try {
            ZoneId userZoneId = userService.getUserTimezone(userId);

            ParsedTaskDetails result = parsingService.parse(rawText, userZoneId);

            if (result.getActionText() != null && !result.getActionText().trim().isEmpty()) {
                taskCreationService.createNewTask(userId, result);

                String successMessage;
                if (result.getDeadline().isPresent()) {
                    String formattedDeadline = result.getDeadline().get().atZone(userZoneId).format(USER_DATE_FORMATTER);
                    successMessage = String.format(MSG_TASK_CREATED, result.getActionText(), formattedDeadline);
                } else {
                    successMessage = String.format(MSG_TASK_CREATED_NO_DEADLINE, result.getActionText());
                }

                messageSenderPort.sendMessage(userId, successMessage);
                log.info("Task created for user {}: {}", userId, result.getActionText());
            } else {
                messageSenderPort.sendMessage(userId, "Я не смог понять, какую задачу вы хотите создать. Попробуйте написать что-то более конкретное.");
            }

        } catch (Exception e) {
            log.error("Unhandled error during task processing for user {}: {}", userId, e.getMessage(), e);
            messageSenderPort.sendMessage(userId, "Произошла внутренняя ошибка при создании задачи. Попробуйте позже.");
        }
    }
}