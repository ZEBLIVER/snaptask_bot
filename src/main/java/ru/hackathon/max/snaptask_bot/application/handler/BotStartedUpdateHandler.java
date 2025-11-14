package ru.hackathon.max.snaptask_bot.application.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.hackathon.max.snaptask_bot.domain.port.out.MaxMessageSenderPort;
import ru.hackathon.max.snaptask_bot.domain.service.UserService;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;

@Component
@Order(1)
public class BotStartedUpdateHandler implements UpdateHandler {
    private static final Logger log = LoggerFactory.getLogger(BotStartedUpdateHandler.class);
    private static final String UPDATE_TYPE_BOT_STARTED = "bot_started";

    private final UserService userService;
    private final MaxMessageSenderPort messageSenderPort;

    private static final String MSG_START_INFO_BASE = "👋 Привет! Я бот для умных напоминаний **SnapTask**.\n\n" +
            "Моя задача — помочь вам не забывать о важном, используя максимально простой ввод.\n\n" +
            "🔔 **Как пользоваться:**\n" +
            "1. **Создание напоминания:** Просто напишите, о чем нужно напомнить, и когда " +
            "(например: *\"Купить молоко завтра в 8 утра\"*).\n" +
            "2. **Все напоминания:** Используйте команду **/list**, чтобы увидеть список всех активных " +
            "и запланированных напоминаний.\n" +
            "3. **Настройки:** Используйте команду **/settings**, чтобы в любой момент изменить ваш часовой пояс.";

    private static final String MSG_WELCOME_PROMPT = "\n\n***Перед началом работы***, пожалуйста, отправьте мне название своего **города** для настройки часового пояса.";


    public BotStartedUpdateHandler(UserService userService, MaxMessageSenderPort messageSenderPort) {
        this.userService = userService;
        this.messageSenderPort = messageSenderPort;
    }

    @Override
    public boolean canHandle(MaxUpdateDto updateDto) {
        return UPDATE_TYPE_BOT_STARTED.equalsIgnoreCase(updateDto.getUpdateType());
    }

    @Override
    public void handle(MaxUpdateDto updateDto) {
        Long userId = updateDto.getUserId();

        String username = (updateDto.getUser() != null) ? updateDto.getUser().getName() : "Unknown";

        if (userId == null) {
            log.error("Критическая ошибка: userId отсутствует в MaxUpdateDto для BOT_STARTED.");
            return;
        }

        log.debug("Начинаю обработку события BOT_STARTED от User={}", userId);

        boolean isNewUser = userService.findOptionalByMaxUserId(userId).isEmpty();
        String welcomeMessage = MSG_START_INFO_BASE;

        if (isNewUser) {
            userService.registerNewUser(userId, username);

            welcomeMessage += MSG_WELCOME_PROMPT;
            log.debug("Новый пользователь {} зарегистрирован и получил расширенное приветствие.", userId);
        } else {
            log.debug("Существующий пользователь {} получил стандартное приветствие.", userId);
        }

        messageSenderPort.sendMessage(userId, welcomeMessage);

        log.debug("Обработка события BOT_STARTED завершена.");
    }
}