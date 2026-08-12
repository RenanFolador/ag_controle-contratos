package com.organization.contractmanager.exception;

import java.util.UUID;

public class NotificationDeadlineNotFoundException extends RuntimeException {

    public NotificationDeadlineNotFoundException(UUID id) {
        super("Notification deadline not found: " + id);
    }
}
