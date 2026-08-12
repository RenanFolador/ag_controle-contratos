package com.organization.contractmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@ConditionalOnProperty(name = "notification.whatsapp.enabled", havingValue = "true")
public class MetaWhatsAppApiClient implements WhatsAppApiClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String messagesPath;

    public MetaWhatsAppApiClient(
            RestClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${notification.whatsapp.api-url}") String apiUrl,
            @Value("${notification.whatsapp.api-version}") String apiVersion,
            @Value("${notification.whatsapp.phone-number-id}") String phoneNumberId,
            @Value("${notification.whatsapp.access-token}") String accessToken) {
        requireConfigured(phoneNumberId, "WHATSAPP_PHONE_NUMBER_ID");
        requireConfigured(accessToken, "WHATSAPP_ACCESS_TOKEN");
        this.restClient = builder.baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();
        this.objectMapper = objectMapper;
        this.messagesPath = "/" + apiVersion + "/" + phoneNumberId + "/messages";
    }

    @Override
    public WhatsAppSendResult sendText(String recipient, String message) {
        try {
            MetaSendResponse response = restClient.post()
                    .uri(messagesPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "messaging_product", "whatsapp",
                            "recipient_type", "individual",
                            "to", recipient,
                            "type", "text",
                            "text", Map.of("preview_url", false, "body", message)))
                    .retrieve()
                    .body(MetaSendResponse.class);
            if (response == null || response.messages() == null
                    || response.messages().isEmpty()) {
                throw new WhatsAppProviderException(502, "invalid_response", null);
            }
            MetaMessage accepted = response.messages().getFirst();
            return new WhatsAppSendResult(accepted.id(), accepted.messageStatus());
        } catch (RestClientResponseException exception) {
            throw providerException(exception);
        }
    }

    private WhatsAppProviderException providerException(RestClientResponseException exception) {
        String code = null;
        String traceId = null;
        try {
            JsonNode error = objectMapper.readTree(exception.getResponseBodyAsString())
                    .path("error");
            code = error.path("code").asText(null);
            traceId = error.path("fbtrace_id").asText(null);
        } catch (Exception ignored) {
            // A resposta bruta não é propagada para evitar exposição de dados do provider.
        }
        return new WhatsAppProviderException(
                exception.getStatusCode().value(), code, traceId);
    }

    private void requireConfigured(String value, String environmentVariable) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    environmentVariable + " is required when WhatsApp is enabled");
        }
    }

    private record MetaSendResponse(List<MetaMessage> messages) {
    }

    private record MetaMessage(
            String id,
            @JsonProperty("message_status") String messageStatus) {
    }
}
