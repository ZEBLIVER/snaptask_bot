package ru.hackathon.max.snaptask_bot.application.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.port.out.MaxMessageSenderPort;
import ru.hackathon.max.snaptask_bot.domain.service.UserService;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;

@Component
@Order(30)
public class StartCommandUpdateHandler implements UpdateHandler {
    private static final Logger log = LoggerFactory.getLogger(StartCommandUpdateHandler.class);
    private static final String START_COMMAND = "/start";

    private final UserService userService;
    private final MaxMessageSenderPort messageSenderPort;

    private static final String MSG_START_INFO = "👋 Привет! Я бот для умных напоминаний **SnapTask**.\n\n" +
            "Моя задача — помочь вам не забывать о важном, используя максимально простой ввод.\n\n" +
            "🔔 **Как пользоваться:**\n" +
            "1. **Создание напоминания:** Просто напишите, о чем нужно напомнить, и когда " +
            "(например: *\"Купить молоко завтра в 8 утра\"*).\n" +
            "2. **Все напоминания:** Используйте команду **/list**, чтобы увидеть список всех активных " +
            "и запланированных напоминаний.\n" +
            "3. **Настройки:** Используйте команду **/settings**, чтобы в любой момент изменить ваш часовой пояс.\n\n" +
            "🚀 *Давайте начнем!*";

    public StartCommandUpdateHandler(UserService userService, MaxMessageSenderPort messageSenderPort) {
        this.userService = userService;
        this.messageSenderPort = messageSenderPort;
    }

    @Override
    public boolean canHandle(MaxUpdateDto updateDto) {
        String rawText = updateDto.getMessage().getBody().getText();
        return rawText != null && START_COMMAND.equalsIgnoreCase(rawText.trim());
    }

    @Override
    public void handle(MaxUpdateDto updateDto) {
        Long userId = updateDto.getMessage().getSender().getUserId();
        String username = updateDto.getMessage().getSender().getName();

        log.info("📢 Начинаю обработку команды {} от User={}", START_COMMAND, userId);

        if (userService.findOptionalByMaxUserId(userId).isEmpty()) {
            userService.registerNewUser(userId, username);
        }

        messageSenderPort.sendMessage(userId, MSG_START_INFO);

        log.info("✅ Обработка команды {} завершена.", START_COMMAND);
    }
}