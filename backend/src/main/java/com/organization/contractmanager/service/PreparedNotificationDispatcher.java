package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.NotificationSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PreparedNotificationDispatcher implements NotificationDispatcher {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(PreparedNotificationDispatcher.class);
    private final NotificationService notificationService;

    public PreparedNotificationDispatcher(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void dispatch(NotificationSchedule schedule) {
        int created = notificationService.createAndSendForSchedule(schedule).size();
        LOGGER.info("Processed {} notifications for contract {} and expiration {}",
                created, schedule.getContract().getId(), schedule.getExpirationDate());
    }
}
