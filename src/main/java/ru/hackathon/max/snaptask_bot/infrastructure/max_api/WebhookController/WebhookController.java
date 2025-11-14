package ru.hackathon.max.snaptask_bot.infrastructure.max_api.WebhookController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hackathon.max.snaptask_bot.application.service.BotUpdateService;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;

@RestController
@RequestMapping("/api/v1/bot/webhook")
public class WebhookController {
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final BotUpdateService botUpdateService;

    public WebhookController(BotUpdateService botUpdateService) {
        this.botUpdateService = botUpdateService;
    }

    @PostMapping
    public ResponseEntity<?> handleMaxUpdate(@RequestBody MaxUpdateDto updateDto) {
        String updateType = updateDto.getUpdateType();
        try {
            botUpdateService.processIncomingUpdate(updateDto);
        } catch (Exception e) {
            log.error("Ошибка при обработке входящего вебхука от MAX ({}): {}",
                    updateType, e.getMessage(), e);
        }
        return ResponseEntity.ok().build();
    }
}