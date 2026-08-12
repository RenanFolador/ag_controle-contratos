package com.organization.contractmanager.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.organization.contractmanager.domain.NotificationDeadline;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest(properties = "spring.flyway.enabled=true")
class NotificationDeadlineRepositoryTests {

    @Autowired
    private NotificationDeadlineRepository repository;

    @Test
    void migrationSeedsInitialEnabledDeadlines() {
        assertThat(repository.findAllByEnabledTrueOrderByDaysBeforeDesc())
                .extracting(NotificationDeadline::getDaysBefore)
                .containsExactly(60, 30, 15);
    }

    @Test
    void persistsAdditionalDeadlineWithAuditTimestamps() {
        NotificationDeadline saved = repository.saveAndFlush(new NotificationDeadline(90, true));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    void rejectsDuplicatePeriod() {
        assertThatThrownBy(() -> repository.saveAndFlush(new NotificationDeadline(60, true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
