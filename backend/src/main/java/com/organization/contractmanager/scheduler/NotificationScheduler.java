package com.organization.contractmanager.scheduler;

import com.organization.contractmanager.service.NotificationScheduleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private final NotificationScheduleService service;

    public NotificationScheduler(NotificationScheduleService service) {
        this.service = service;
    }

    @Scheduled(cron = "${notification.cron:0 0 8 * * *}", zone = "America/Sao_Paulo")
    public void processDueNotifications() {
        service.processDueSchedules();
    }
}
