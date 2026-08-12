package com.organization.contractmanager.service;

public interface WhatsAppApiClient {

    WhatsAppSendResult sendText(String recipient, String message);
}
