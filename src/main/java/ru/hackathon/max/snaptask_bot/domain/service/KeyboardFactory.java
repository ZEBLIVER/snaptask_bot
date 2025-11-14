package ru.hackathon.max.snaptask_bot.domain.service;

import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.system.CallbackPayload;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing.Button;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing.Keyboard;

import java.util.List;

@Component
public class KeyboardFactory {

    public Keyboard createTaskNotificationKeyboard(Long taskId) {

        String taskIdStr = taskId.toString();

        List<Button> deferRow = List.of(
                Button.builder()
                        .text("15 мин.")
                        .payload(CallbackPayload.DEFER_TASK + ":" + taskIdStr + ":" + CallbackPayload.TIME_15_MINUTES)
                        .intent("default")
                        .build(),
                Button.builder()
                        .text("1 час")
                        .payload(CallbackPayload.DEFER_TASK + ":" + taskIdStr + ":" + CallbackPayload.TIME_1_HOUR)
                        .intent("default")
                        .build(),
                Button.builder()
                        .text("Завтра")
                        .payload(CallbackPayload.DEFER_TASK + ":" + taskIdStr + ":" + CallbackPayload.TIME_TOMORROW)
                        .intent("default")
                        .build()
        );

        return Keyboard.builder()
                .buttons(List.of(deferRow))
                .build();
    }

    public Keyboard createTaskCancellationOnlyKeyboard(Long taskId) {

        String taskIdStr = taskId.toString();

        List<Button> cancelRow = List.of(
                Button.builder()
                        .text("❌ Отменить перенос")
                        .payload(CallbackPayload.CANCEL_TASK + ":" + taskIdStr)
                        .intent("negative")
                        .build()
        );

        return Keyboard.builder()
                .buttons(List.of(cancelRow))
                .build();
    }
}