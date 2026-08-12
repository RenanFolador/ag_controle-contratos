package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "notification.whatsapp.enabled", havingValue = "true")
public class WhatsAppNotificationProvider implements NotificationProvider {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(WhatsAppNotificationProvider.class);

    private final WhatsAppApiClient apiClient;

    public WhatsAppNotificationProvider(WhatsAppApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public void send(Notification notification) {
        try {
            WhatsAppSendResult result = apiClient.sendText(
                    notification.getRecipientAddress(), notification.getMessage());
            LOGGER.info(
                    "WhatsApp provider accepted notificationId={} providerMessageId={} providerStatus={}",
                    notification.getId(), result.messageId(), result.status());
        } catch (WhatsAppProviderException exception) {
            LOGGER.warn(
                    "WhatsApp provider rejected notificationId={} httpStatus={} providerCode={} traceId={}",
                    notification.getId(), exception.getHttpStatus(),
                    exception.getProviderCode(), exception.getTraceId());
            throw exception;
        }
    }
}
