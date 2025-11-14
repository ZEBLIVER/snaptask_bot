package ru.hackathon.max.snaptask_bot.infrastructure.max_api.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing.CallbackAnswerRequest;
import ru.hackathon.max.snaptask_bot.infrastructure.max_api.dto.outgoing.MaxSendMessageRequest;

@Component
public class MaxApiClient {
    private static final Logger log = LoggerFactory.getLogger(MaxApiClient.class);

    private final String maxApiBaseUrl;
    private final String maxApiSendMessageUrl;
    private final String authToken;
    private final RestTemplate restTemplate;

    public MaxApiClient(
            RestTemplate restTemplate,
            @Value("${max.api.url}") String maxApiBaseUrl,
            @Value("${max.api.send-message-url}") String maxApiSendMessageUrl,
            @Value("${max.bot.token}") String authToken) {

        this.restTemplate = restTemplate;
        this.maxApiBaseUrl = maxApiBaseUrl;
        this.maxApiSendMessageUrl = maxApiSendMessageUrl;
        this.authToken = authToken;
    }

    /**
     * Выполняет POST-запрос к внешнему Max API для отправки сообщения.
     * @param recipientId ID пользователя.
     * @param requestBody DTO сообщения.
     */
    public void send(Long recipientId, MaxSendMessageRequest requestBody) throws RestClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authToken);

        String urlWithParams = UriComponentsBuilder.fromHttpUrl(maxApiSendMessageUrl)
                .queryParam("user_id", recipientId)
                .toUriString();

        HttpEntity<MaxSendMessageRequest> entity = new HttpEntity<>(requestBody, headers);
        log.debug("HTTP POST {}", urlWithParams);
        restTemplate.postForEntity(urlWithParams, entity, Void.class);
    }

    /**
     * Отправляет ответ на callback-запрос, обновляя сообщение или показывая уведомление.
     * @param callbackId Идентификатор callback-кнопки.
     * @param request DTO ответа на callback.
     */
    public void sendCallbackAnswer(String callbackId, CallbackAnswerRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authToken);

        String fullUrl = UriComponentsBuilder.fromHttpUrl(maxApiBaseUrl)
                .pathSegment("answers")
                .queryParam("callback_id", callbackId)
                .toUriString();

        HttpEntity<CallbackAnswerRequest> entity = new HttpEntity<>(request, headers);

        log.debug("HTTP POST {}", fullUrl);
        restTemplate.postForEntity(fullUrl, entity, String.class);
    }
}