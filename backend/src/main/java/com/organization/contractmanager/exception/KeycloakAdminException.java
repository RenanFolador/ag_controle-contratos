package com.organization.contractmanager.exception;

public class KeycloakAdminException extends RuntimeException {
    private final int status;

    public KeycloakAdminException(String message, int status) {
        super(message);
        this.status = status;
    }

    public KeycloakAdminException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
