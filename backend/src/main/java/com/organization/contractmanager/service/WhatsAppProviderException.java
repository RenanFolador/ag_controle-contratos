package com.organization.contractmanager.service;

public class WhatsAppProviderException extends RuntimeException {
    private final int httpStatus;
    private final String providerCode;
    private final String traceId;

    public WhatsAppProviderException(
            int httpStatus, String providerCode, String traceId) {
        super("WhatsApp provider request failed (HTTP " + httpStatus
                + ", code " + safe(providerCode) + ")");
        this.httpStatus = httpStatus;
        this.providerCode = safe(providerCode);
        this.traceId = safe(traceId);
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public String getTraceId() {
        return traceId;
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "unavailable" : value;
    }
}
