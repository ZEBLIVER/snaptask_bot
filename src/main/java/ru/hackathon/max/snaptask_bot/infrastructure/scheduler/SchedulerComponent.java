package ru.hackathon.max.snaptask_bot.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.service.task.TaskNotificationService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerComponent {

    private final TaskNotificationService taskNotificationService;

    @Scheduled(fixedDelay = 60000)
    public void checkTasksForNotification() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Instant nextMinute = now.plusSeconds(60);

        log.debug("Интервал поиска (UTC): {} — {}", now, nextMinute);

        taskNotificationService.processDueTasks(now, nextMinute);
    }
}