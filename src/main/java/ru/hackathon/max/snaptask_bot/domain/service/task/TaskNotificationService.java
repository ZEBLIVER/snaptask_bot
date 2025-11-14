package ru.hackathon.max.snaptask_bot.domain.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskStatus;
import ru.hackathon.max.snaptask_bot.domain.port.out.MaxMessageSenderPort;
import ru.hackathon.max.snaptask_bot.domain.service.KeyboardFactory;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskEntity;
import ru.hackathon.max.snaptask_bot.domain.model.user.UserEntity;
import ru.hackathon.max.snaptask_bot.domain.port.out.TaskRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskNotificationService {

    private final TaskRepository taskRepository;
    private final MaxMessageSenderPort messageSenderPort;
    private final KeyboardFactory keyboardFactory;

    private static final DateTimeFormatter USER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm, dd MMMM");

    private static final String NOTIFICATION_MESSAGE_TEMPLATE =
            "🔔 **Напоминание:** %s\n\n" +
                    "Срок: *%s* (ваше время).\n" +
                    "Напомнить позже?";

    /**
     * Ищет и отправляет уведомления о просроченных задачах.
     */
    @Transactional
    public void processDueTasks(Instant checkTimeStart, Instant checkTimeEnd) {
        List<TaskEntity> tasksDue = taskRepository.findByDeadlineBetweenAndStatusNot(
                checkTimeStart,
                checkTimeEnd,
                TaskStatus.COMPLETED
        );

        log.info("Найдено {} задач, требующих уведомления в интервале {} - {}",
                tasksDue.size(), checkTimeStart, checkTimeEnd);

        for (TaskEntity task : tasksDue) {
            sendNotification(task);
        }
    }

    private void sendNotification(TaskEntity task) {
        UserEntity user = task.getUser();

        if (task.getDeadline() == null) {
            log.warn("Попытка отправить уведомление для задачи {} без дедлайна. Пропуск.", task.getId());
            return;
        }

        ZoneId userZone = ZoneId.of(user.getTimezoneId());

        ZonedDateTime localDueTime = task.getDeadline().atZone(userZone);
        String formattedTime = localDueTime.format(USER_TIME_FORMATTER);

        String message = String.format(
                NOTIFICATION_MESSAGE_TEMPLATE,
                task.getActionText(),
                formattedTime
        );

        var keyboard = keyboardFactory.createTaskNotificationKeyboard(task.getId());

        messageSenderPort.sendMessage(user.getMaxUserId(), message, keyboard);

        log.info("Уведомление отправлено User {} для задачи {}",
                user.getMaxUserId(), task.getActionText());
    }
}