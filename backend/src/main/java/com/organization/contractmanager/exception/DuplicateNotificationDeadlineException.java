package com.organization.contractmanager.exception;

public class DuplicateNotificationDeadlineException extends RuntimeException {

    public DuplicateNotificationDeadlineException(int daysBefore) {
        super("Notification deadline already exists: " + daysBefore + " days");
    }
}
