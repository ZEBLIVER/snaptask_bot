package ru.hackathon.max.snaptask_bot.domain.port.out;

import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing.CallbackAnswerRequest;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing.Keyboard;


public interface MaxMessageSenderPort {

    void sendMessage(Long recipientId, String text);
    void sendMessage(Long recipientId, String text, Keyboard keyboard);
    void sendCallbackAnswer(String callbackId, CallbackAnswerRequest request);

}