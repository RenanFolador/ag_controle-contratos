package com.organization.contractmanager.repository;

import com.organization.contractmanager.domain.NotificationDeadline;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationDeadlineRepository
        extends JpaRepository<NotificationDeadline, UUID> {

    List<NotificationDeadline> findAllByOrderByDaysBeforeDesc();

    List<NotificationDeadline> findAllByEnabledTrueOrderByDaysBeforeDesc();

    @Query("select deadline from NotificationDeadline deadline where deadline.daysBefore = :daysBefore")
    Optional<NotificationDeadline> findByDaysBefore(@Param("daysBefore") int daysBefore);

    @Query("select (count(deadline) > 0) from NotificationDeadline deadline "
            + "where deadline.daysBefore = :daysBefore")
    boolean existsByDaysBefore(@Param("daysBefore") int daysBefore);
}
