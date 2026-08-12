package com.organization.contractmanager.repository;

import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationChannel;
import com.organization.contractmanager.domain.NotificationStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    long countByStatus(NotificationStatus status);

    @Query("select (count(notification) > 0) from Notification notification "
            + "where notification.contract.id = :contractId "
            + "and notification.person.id = :personId "
            + "and notification.expirationDate = :expirationDate "
            + "and notification.daysBefore = :daysBefore "
            + "and notification.channel = :channel")
    boolean existsForDelivery(
            @Param("contractId") UUID contractId,
            @Param("personId") UUID personId,
            @Param("expirationDate") LocalDate expirationDate,
            @Param("daysBefore") int daysBefore,
            @Param("channel") NotificationChannel channel);
}
