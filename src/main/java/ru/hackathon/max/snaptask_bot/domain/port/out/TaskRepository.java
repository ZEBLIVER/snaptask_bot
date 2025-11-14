package ru.hackathon.max.snaptask_bot.domain.port.out;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskStatus;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskEntity;

import java.time.Instant;
import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    /**
     * Поиск невыполненных задач (статус не COMPLETED), дедлайн которых находится в заданном интервале Instant (UTC).
     */
    List<TaskEntity> findByDeadlineBetweenAndStatusNot(Instant start, Instant end, TaskStatus status);

    /**
     * Поиск всех активных задач для конкретного пользователя
     */
    List<TaskEntity> findByUser_MaxUserIdAndStatusNot(Long maxUserId, TaskStatus status);

}