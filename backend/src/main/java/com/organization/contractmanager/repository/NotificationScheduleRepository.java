package com.organization.contractmanager.repository;

import com.organization.contractmanager.domain.NotificationSchedule;
import com.organization.contractmanager.domain.NotificationScheduleStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationScheduleRepository
        extends JpaRepository<NotificationSchedule, UUID> {

    @Query("select schedule from NotificationSchedule schedule "
            + "where schedule.contract.id = :contractId order by schedule.daysBefore desc")
    List<NotificationSchedule> findAllByContractIdOrderByDaysBeforeDesc(
            @Param("contractId") UUID contractId);

    @Query("select schedule from NotificationSchedule schedule "
            + "where schedule.contract.id = :contractId "
            + "and schedule.expirationDate = :expirationDate "
            + "and schedule.status = com.organization.contractmanager.domain.NotificationScheduleStatus.PENDING")
    List<NotificationSchedule> findPendingByContractIdAndExpirationDate(
            @Param("contractId") UUID contractId,
            @Param("expirationDate") LocalDate expirationDate);

    @Query("select schedule from NotificationSchedule schedule "
            + "join fetch schedule.contract "
            + "where schedule.status = :status "
            + "and schedule.scheduledDate <= :today "
            + "order by schedule.scheduledDate, schedule.id")
    List<NotificationSchedule> findDueSchedules(
            @Param("status") NotificationScheduleStatus status,
            @Param("today") LocalDate today);

    @Query("select schedule from NotificationSchedule schedule "
            + "where schedule.daysBefore = :daysBefore and schedule.status = :status")
    List<NotificationSchedule> findAllByDaysBeforeAndStatus(
            @Param("daysBefore") int daysBefore,
            @Param("status") NotificationScheduleStatus status);

    @Query("select (count(schedule) > 0) from NotificationSchedule schedule "
            + "where schedule.contract.id = :contractId "
            + "and schedule.expirationDate = :expirationDate "
            + "and schedule.daysBefore = :daysBefore")
    boolean existsByContractIdAndExpirationDateAndDaysBefore(
            @Param("contractId") UUID contractId,
            @Param("expirationDate") LocalDate expirationDate,
            @Param("daysBefore") int daysBefore);
}
