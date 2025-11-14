package ru.hackathon.max.snaptask_bot.application.service;

import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.incoming.MaxUpdateDto;

public interface BotUpdateService {
    void processIncomingUpdate(MaxUpdateDto updateDto);
}