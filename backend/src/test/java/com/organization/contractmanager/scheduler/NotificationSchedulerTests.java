package com.organization.contractmanager.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.organization.contractmanager.service.NotificationScheduleService;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

class NotificationSchedulerTests {

    @Test
    void delegatesUsingConfiguredCronAndSaoPauloTimezone() throws Exception {
        NotificationScheduleService service = org.mockito.Mockito.mock(
                NotificationScheduleService.class);
        NotificationScheduler scheduler = new NotificationScheduler(service);

        scheduler.processDueNotifications();

        verify(service).processDueSchedules();
        Method method = NotificationScheduler.class.getMethod("processDueNotifications");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        assertThat(scheduled.cron()).isEqualTo("${notification.cron:0 0 8 * * *}");
        assertThat(scheduled.zone()).isEqualTo("America/Sao_Paulo");
    }
}
