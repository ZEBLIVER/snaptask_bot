package ru.hackathon.max.snaptask_bot.domain.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.max.snaptask_bot.domain.model.system.CallbackPayload;
import ru.hackathon.max.snaptask_bot.domain.port.out.MaxMessageSenderPort;
import ru.hackathon.max.snaptask_bot.domain.port.out.TaskRepository;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskEntity;
import ru.hackathon.max.snaptask_bot.domain.model.task.TaskStatus;
import ru.hackathon.max.snaptask_bot.domain.service.KeyboardFactory;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing.AttachmentRequest;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing.CallbackAnswerRequest;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing.Keyboard;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing.MessageBody;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskCallbackService {

    private final TaskRepository taskRepository;
    private final MaxMessageSenderPort messageSenderPort;
    private final KeyboardFactory keyboardFactory;

    @Transactional
    public void processCallback(String callbackId, String payload) {
        String[] parts = payload.split(":");
        if (parts.length < 2) {
            log.error("Неверный формат payload: {}", payload);
            sendNotificationAnswer(callbackId, "❌ Ошибка формата данных.");
            return;
        }

        String command = parts[0];
        Long taskId = Long.valueOf(parts[1]);

        TaskEntity task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            sendNotificationAnswer(callbackId, "❌ Задача не найдена.");
            return;
        }

        String responseText;

        if (CallbackPayload.DEFER_TASK.equals(command) && parts.length == 3) {
            String timeValue = parts[2];

            Instant newDeadline = calculateNewDeadline(timeValue);
            task.setDeadline(newDeadline);
            taskRepository.save(task);

            String durationText = formatDelayDuration(timeValue);
            responseText = String.format("⏳ Задача **%s** отложена %s.", task.getActionText(), durationText);

            updateMessageWithCancellationKeyboardAndSendNotification(callbackId, responseText, task.getId());

            return;

        } else if (CallbackPayload.CANCEL_TASK.equals(command)) {
            // 2. Команда "Отмена"
            task.setStatus(TaskStatus.COMPLETED);
            task.setDeadline(null); // Явно обнуляем дедлайн, чтобы остановить напоминания
            taskRepository.save(task);
            responseText = String.format("🗑️ Задача **%s** отменена.", task.getActionText());

            updateMessageAndSendNotification(callbackId, responseText);

            return;

        } else {
            responseText = "❓ Неизвестная команда.";
            updateMessageAndSendNotification(callbackId, responseText);
            return;
        }
    }

    /**
     * Преобразует строковое значение времени из payload в читаемый текст.
     */
    private String formatDelayDuration(String timeValue) {
        switch (timeValue) {
            case CallbackPayload.TIME_15_MINUTES:
                return "на 15 минут";
            case CallbackPayload.TIME_1_HOUR:
                return "на 1 час";
            case CallbackPayload.TIME_TOMORROW:
                return "до завтра";
            case CallbackPayload.TIME_5_MINUTES:
                return "на 5 минут";
            default:
                return "на неопределенное время";
        }
    }



    /**
     * Используется для успешного откладывания (defer) задачи.
     * Обновляет сообщение, заменяя клавиатуру на клавиатуру "Отменить перенос".
     */
    private void updateMessageWithCancellationKeyboardAndSendNotification(String callbackId, String text, Long taskId) {

        Keyboard keyboard = keyboardFactory.createTaskCancellationOnlyKeyboard(taskId); // <-- Объект типа Keyboard

        AttachmentRequest keyboardAttachment = AttachmentRequest.builder()
                .type("inline_keyboard")
                .payload(keyboard)
                .build();

        MessageBody updatedBody = MessageBody.builder()
                .text(text)
                .format("markdown")
                .attachments(List.of(keyboardAttachment))
                .build();

        CallbackAnswerRequest request = CallbackAnswerRequest.builder()
                .message(updatedBody)
                .notification(text.replaceAll("\\*\\*", ""))
                .build();

        messageSenderPort.sendCallbackAnswer(callbackId, request);
    }


    /**
     * Рассчитывает новый срок задачи (deadline) относительно текущего времени.
     */
    private Instant calculateNewDeadline(String timeValue) {
        long delaySeconds;

        switch (timeValue) {
            case CallbackPayload.TIME_15_MINUTES:
                delaySeconds = TimeUnit.MINUTES.toSeconds(15);
                break;
            case CallbackPayload.TIME_1_HOUR:
                delaySeconds = TimeUnit.HOURS.toSeconds(1);
                break;
            case CallbackPayload.TIME_TOMORROW:
                delaySeconds = TimeUnit.HOURS.toSeconds(24);
                break;
            case CallbackPayload.TIME_5_MINUTES:
                delaySeconds = TimeUnit.MINUTES.toSeconds(5);
                break;
            default:
                log.warn("Неизвестное значение времени отсрочки: {}. Использовано значение по умолчанию (15 минут).", timeValue);
                delaySeconds = TimeUnit.MINUTES.toSeconds(15);
        }
        return Instant.now().plusSeconds(delaySeconds);
    }

    private void sendNotificationAnswer(String callbackId, String text) {
        CallbackAnswerRequest request = CallbackAnswerRequest.builder()
                .notification(text)
                .build();
        messageSenderPort.sendCallbackAnswer(callbackId, request);
    }

    private void updateMessageAndSendNotification(String callbackId, String text) {
        MessageBody updatedBody = MessageBody.builder()
                .text(text)
                .format("markdown")
                .attachments(Collections.emptyList())
                .build();

        CallbackAnswerRequest request = CallbackAnswerRequest.builder()
                .message(updatedBody)
                .notification(text.replaceAll("\\*\\*", ""))
                .build();

        messageSenderPort.sendCallbackAnswer(callbackId, request);
    }
}