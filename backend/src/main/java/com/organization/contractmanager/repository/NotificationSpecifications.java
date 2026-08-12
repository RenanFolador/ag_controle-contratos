package com.organization.contractmanager.repository;

import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationChannel;
import com.organization.contractmanager.domain.NotificationStatus;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class NotificationSpecifications {
    private NotificationSpecifications() { }

    public static Specification<Notification> hasStatus(NotificationStatus status) {
        return status == null ? null
                : (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<Notification> hasChannel(NotificationChannel channel) {
        return channel == null ? null
                : (root, query, builder) -> builder.equal(root.get("channel"), channel);
    }

    public static Specification<Notification> contractContains(String contract) {
        if (contract == null || contract.isBlank()) return null;
        String pattern = "%" + contract.trim().toLowerCase() + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("contract").get("contractNumber")), pattern),
                builder.like(builder.lower(root.get("contract").get("companyName")), pattern));
    }

    public static Specification<Notification> scheduledOn(LocalDate date) {
        return date == null ? null
                : (root, query, builder) -> builder.equal(root.get("scheduledDate"), date);
    }
}
