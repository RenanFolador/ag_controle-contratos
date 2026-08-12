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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notifications_contract_person_expiration_deadline_channel",
                columnNames = {"contract_id", "person_id", "expiration_date",
                    "days_before", "channel"}),
        indexes = {
            @Index(name = "idx_notifications_status_created_at",
                    columnList = "status, created_at"),
            @Index(name = "idx_notifications_contract_id", columnList = "contract_id"),
            @Index(name = "idx_notifications_person_id", columnList = "person_id")
        })
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false, updatable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false, updatable = false)
    private Person person;

    @Column(name = "expiration_date", nullable = false, updatable = false)
    private LocalDate expirationDate;

    @Column(name = "days_before", nullable = false, updatable = false)
    private int daysBefore;

    @Column(name = "scheduled_date", nullable = false, updatable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "recipient_name", nullable = false, updatable = false)
    private String recipientName;

    @Column(name = "recipient_address", nullable = false, updatable = false)
    private String recipientAddress;

    @Column(nullable = false, updatable = false)
    private String subject;

    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() {
    }

    public Notification(Contract contract, Person person, LocalDate expirationDate,
                        int daysBefore, LocalDate scheduledDate,
                        NotificationChannel channel, String recipientName,
                        String recipientAddress, String subject, String message) {
        this.contract = contract;
        this.person = person;
        this.expirationDate = expirationDate;
        this.daysBefore = daysBefore;
        this.scheduledDate = scheduledDate;
        this.channel = channel;
        this.recipientName = recipientName;
        this.recipientAddress = recipientAddress;
        this.subject = subject;
        this.message = message;
        this.status = NotificationStatus.PENDING;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void markSent(Instant sentAt) {
        status = NotificationStatus.SENT;
        this.sentAt = sentAt;
        errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        status = NotificationStatus.FAILED;
        sentAt = null;
        this.errorMessage = errorMessage == null
                ? "Unknown notification provider error"
                : errorMessage.substring(0, Math.min(errorMessage.length(), 2000));
        retryCount++;
    }

    public UUID getId() { return id; }
    public Contract getContract() { return contract; }
    public Person getPerson() { return person; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public int getDaysBefore() { return daysBefore; }
    public LocalDate getScheduledDate() { return scheduledDate; }
    public NotificationChannel getChannel() { return channel; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientAddress() { return recipientAddress; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public NotificationStatus getStatus() { return status; }
    public Instant getSentAt() { return sentAt; }
    public String getErrorMessage() { return errorMessage; }
    public int getRetryCount() { return retryCount; }
    public Instant getCreatedAt() { return createdAt; }
}
