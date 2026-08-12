package com.organization.contractmanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "notification_schedules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notification_schedules_contract_expiration_deadline",
                columnNames = {"contract_id", "expiration_date", "days_before"}),
        indexes = {
            @Index(name = "idx_notification_schedules_status_scheduled_date",
                    columnList = "status, scheduled_date"),
            @Index(name = "idx_notification_schedules_contract_id", columnList = "contract_id"),
            @Index(name = "idx_notification_schedules_expiration_date",
                    columnList = "expiration_date")
        })
public class NotificationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false, updatable = false)
    private Contract contract;

    @Column(name = "expiration_date", nullable = false, updatable = false)
    private LocalDate expirationDate;

    @Column(name = "days_before", nullable = false, updatable = false)
    private int daysBefore;

    @Column(name = "scheduled_date", nullable = false, updatable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationScheduleStatus status = NotificationScheduleStatus.PENDING;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected NotificationSchedule() {
    }

    public NotificationSchedule(
            Contract contract, LocalDate expirationDate, int daysBefore,
            LocalDate scheduledDate) {
        this.contract = contract;
        this.expirationDate = expirationDate;
        this.daysBefore = daysBefore;
        this.scheduledDate = scheduledDate;
        this.status = NotificationScheduleStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void cancelIfPending() {
        if (status == NotificationScheduleStatus.PENDING) {
            status = NotificationScheduleStatus.CANCELLED;
        }
    }

    public void markProcessing() {
        if (status == NotificationScheduleStatus.PENDING) {
            status = NotificationScheduleStatus.PROCESSING;
        }
    }

    public void markProcessed(Instant processedAt) {
        if (status == NotificationScheduleStatus.PROCESSING) {
            status = NotificationScheduleStatus.PROCESSED;
            this.processedAt = processedAt;
        }
    }

    public void markFailed() {
        if (status == NotificationScheduleStatus.PROCESSING) {
            status = NotificationScheduleStatus.FAILED;
        }
    }

    public UUID getId() { return id; }
    public Contract getContract() { return contract; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public int getDaysBefore() { return daysBefore; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public NotificationScheduleStatus getStatus() { return status; }
    public Instant getProcessedAt() { return processedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
