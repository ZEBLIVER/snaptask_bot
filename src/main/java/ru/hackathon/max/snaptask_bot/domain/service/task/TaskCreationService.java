package ru.hackathon.max.snaptask_bot.domain.service.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hackathon.max.snaptask_bot.domain.model.parser.ParsedTaskDetails;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskStatus;
import ru.hackathon.max.snaptask_bot.domain.service.UserService;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskEntity;
import ru.hackathon.max.snaptask_bot.domain.model.user.UserEntity;
import ru.hackathon.max.snaptask_bot.domain.port.out.TaskRepository;

import java.time.ZoneId;


import java.time.Instant;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class TaskCreationService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    /**
     * Создает и сохраняет новую задачу на основе результатов парсинга.
     * Выполняет критическую конвертацию LocalDateTime (локальное время) в Instant (UTC).
     * * @param maxUserId Внешний ID пользователя (Telegram ID)
     * @param parsedDetails Результаты парсинга (текст, LocalDateTime дедлайн, правило повтора)
     * @return Сохраненная TaskEntity.
     */
    public TaskEntity createNewTask(Long maxUserId, ParsedTaskDetails parsedDetails) {
        UserEntity user = userService.findByMaxUserId(maxUserId);
        ZoneId userZoneId = ZoneId.of(user.getTimezoneId());

        TaskEntity task = new TaskEntity();
        task.setUser(user);
        task.setActionText(parsedDetails.getActionText());
        task.setRecurrenceRule(parsedDetails.getRecurrenceRule().orElse(null));
        task.setCreatedAt(Instant.now());
        task.setStatus(TaskStatus.TODO);

        parsedDetails.getDeadline().ifPresent(localDateTime -> {

            ZonedDateTime zonedDateTime = localDateTime.atZone(userZoneId);

            task.setDeadline(zonedDateTime.toInstant());
        });

        return taskRepository.save(task);
    }
}