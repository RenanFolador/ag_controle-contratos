package com.organization.contractmanager.repository;

import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationChannel;
import com.organization.contractmanager.domain.NotificationStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, UUID>,
        JpaSpecificationExecutor<Notification> {

    @Override
    @EntityGraph(attributePaths = {"contract", "person"})
    Page<Notification> findAll(Specification<Notification> specification, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"contract", "person"})
    List<Notification> findAll(Specification<Notification> specification, Sort sort);

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
