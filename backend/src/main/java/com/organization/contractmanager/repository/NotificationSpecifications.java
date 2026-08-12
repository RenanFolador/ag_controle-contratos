package com.organization.contractmanager.repository;

import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationChannel;
import com.organization.contractmanager.domain.NotificationStatus;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;
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

    public static Specification<Notification> contractStatus(
            com.organization.contractmanager.domain.ContractStatus status) {
        return status == null ? null : (root, query, builder) ->
                builder.equal(root.get("contract").get("status"), status);
    }

    public static Specification<Notification> personIs(UUID personId) {
        return personId == null ? null : (root, query, builder) ->
                builder.equal(root.get("person").get("id"), personId);
    }

    public static Specification<Notification> companyContains(String company) {
        if (company == null || company.isBlank()) return null;
        String pattern = "%" + company.trim().toLowerCase() + "%";
        return (root, query, builder) -> builder.like(
                builder.lower(root.get("contract").get("companyName")), pattern);
    }

    public static Specification<Notification> sentBetween(Instant start, Instant end) {
        if (start == null && end == null) return null;
        if (start == null) return (root, query, builder) ->
                builder.lessThan(root.get("sentAt"), end);
        if (end == null) return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("sentAt"), start);
        return (root, query, builder) -> builder.and(
                builder.greaterThanOrEqualTo(root.get("sentAt"), start),
                builder.lessThan(root.get("sentAt"), end));
    }

    public static Specification<Notification> createdBetween(Instant start, Instant end) {
        if (start == null && end == null) return null;
        if (start == null) return (root, query, builder) ->
                builder.lessThan(root.get("createdAt"), end);
        if (end == null) return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("createdAt"), start);
        return (root, query, builder) -> builder.and(
                builder.greaterThanOrEqualTo(root.get("createdAt"), start),
                builder.lessThan(root.get("createdAt"), end));
    }
}
