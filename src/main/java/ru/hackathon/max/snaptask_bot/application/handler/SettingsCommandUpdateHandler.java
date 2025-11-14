package ru.hackathon.max.snaptask_bot.application.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.model.user.UserStatus;
import ru.hackathon.max.snaptask_bot.domain.port.out.MaxMessageSenderPort;
import ru.hackathon.max.snaptask_bot.domain.service.UserService;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;

@Component
@Order(20)
public class SettingsCommandUpdateHandler implements UpdateHandler {
    private static final Logger log = LoggerFactory.getLogger(SettingsCommandUpdateHandler.class);
    private static final String SETTINGS_COMMAND = "/settings";

    private static final String MSG_PROMPT_TIMEZONE = "🕒 Хорошо, давайте сменим часовой пояс.\n" +
            "Пожалуйста, отправьте мне название вашего **города** (например, **'Москва'**), " +
            "или смещение от UTC (например, **'+5'**).";

    private final UserService userService;
    private final MaxMessageSenderPort messageSenderPort;

    public SettingsCommandUpdateHandler(UserService userService, MaxMessageSenderPort messageSenderPort) {
        this.userService = userService;
        this.messageSenderPort = messageSenderPort;
    }

    @Override
    public boolean canHandle(MaxUpdateDto updateDto) {
        String rawText = updateDto.getMessage().getBody().getText();
        return rawText != null && SETTINGS_COMMAND.equalsIgnoreCase(rawText.trim());
    }

    @Override
    public void handle(MaxUpdateDto updateDto) {
        Long userId = updateDto.getMessage().getSender().getUserId();

        log.info("📢 Начинаю обработку команды {} от User={}", SETTINGS_COMMAND, userId);

        userService.setUserStatus(userId, UserStatus.AWAITING_TIMEZONE);

        messageSenderPort.sendMessage(userId, MSG_PROMPT_TIMEZONE);

        log.info("✅ Обработка команды {} завершена. Статус User={} изменен на AWAITING_TIMEZONE.", SETTINGS_COMMAND, userId);
    }
}