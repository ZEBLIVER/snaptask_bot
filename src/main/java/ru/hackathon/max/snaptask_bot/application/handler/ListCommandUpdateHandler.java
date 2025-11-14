package ru.hackathon.max.snaptask_bot.application.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.user.UserStatus;
import ru.hackathon.max.snaptask_bot.domain.port.out.MaxMessageSenderPort;
import ru.hackathon.max.snaptask_bot.domain.service.UserService;
import ru.hackathon.max.snaptask_bot.domain.service.task.TaskProcessorService;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;

@Component
@Order(10)
public class ListCommandUpdateHandler implements UpdateHandler {
    private static final Logger log = LoggerFactory.getLogger(ListCommandUpdateHandler.class);
    private static final String LIST_COMMAND = "/list";

    private final TaskProcessorService taskProcessorService;
    private final UserService userService;
    private final MaxMessageSenderPort messageSenderPort;

    private static final String MSG_AWAITING_TIMEZONE = "🕒 Чтобы увидеть список напоминаний, сначала необходимо установить часовой пояс.\n" +
            "Пожалуйста, отправьте мне название своего **города** или смещение от UTC (например, **'+3'**).";

    public ListCommandUpdateHandler(
            TaskProcessorService taskProcessorService,
            UserService userService,
            MaxMessageSenderPort messageSenderPort) {
        this.taskProcessorService = taskProcessorService;
        this.userService = userService;
        this.messageSenderPort = messageSenderPort;
    }

    @Override
    public boolean canHandle(MaxUpdateDto updateDto) {
        String rawText = updateDto.getMessage().getBody().getText();
        return rawText != null && LIST_COMMAND.equalsIgnoreCase(rawText.trim());
    }

    @Override
    public void handle(MaxUpdateDto updateDto) {
        Long userId = updateDto.getMessage().getSender().getUserId();

        UserStatus status = userService.getUserStatus(userId);
        log.info("📢 Начинаю обработку команды {} от User={}. Статус: {}", LIST_COMMAND, userId, status);

        if (status == UserStatus.AWAITING_TIMEZONE) {
            messageSenderPort.sendMessage(userId, MSG_AWAITING_TIMEZONE);
            log.warn("🚫 Запрос /list заблокирован, пользователь {} ожидает ввода часового пояса.", userId);
            return;
        }

        if (status == UserStatus.REGISTERED) {
            taskProcessorService.listActiveTasks(userId);
        } else {
            log.error("Пользователь {} попытался использовать /list, но имеет неожиданный статус: {}", userId, status);
            messageSenderPort.sendMessage(userId, "Произошла ошибка регистрации. Пожалуйста, отправьте **/start** заново.");
        }
    }
}