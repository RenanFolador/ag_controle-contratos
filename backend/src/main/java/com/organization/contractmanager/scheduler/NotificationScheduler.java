package com.organization.contractmanager.scheduler;

import com.organization.contractmanager.service.NotificationScheduleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

@Component
public class NotificationScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationScheduler.class);
    private static final String ZONE = "America/Sao_Paulo";

    private final NotificationScheduleService service;
    private final String cron;

    public NotificationScheduler(
            NotificationScheduleService service,
            @Value("${notification.cron:0 0 8 * * *}") String cron) {
        this.service = service;
        this.cron = cron;
    }

    @PostConstruct
    void logSchedulerStarted() {
        LOGGER.info("Notification scheduler started cron={} timezone={}", cron, ZONE);
    }

    @Scheduled(cron = "${notification.cron:0 0 8 * * *}", zone = ZONE)
    public void processDueNotifications() {
        service.processDueSchedules();
    }
}
