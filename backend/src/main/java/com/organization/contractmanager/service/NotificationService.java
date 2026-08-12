package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationChannel;
import com.organization.contractmanager.domain.NotificationSchedule;
import com.organization.contractmanager.domain.Person;
import com.organization.contractmanager.domain.HistoryAction;
import com.organization.contractmanager.repository.ContractAssignmentRepository;
import com.organization.contractmanager.repository.NotificationRepository;
import com.organization.contractmanager.dto.NotificationResponse;
import com.organization.contractmanager.dto.PageResponse;
import com.organization.contractmanager.domain.NotificationStatus;
import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import static com.organization.contractmanager.repository.NotificationSpecifications.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final ContractAssignmentRepository assignmentRepository;
    private final Map<NotificationChannel, NotificationProvider> providers;
    private final Clock clock;
    private final ContractHistoryService historyService;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> findAll(
            int page, int size, NotificationStatus status,
            NotificationChannel channel, String contract, LocalDate date) {
        Specification<Notification> specification = Specification.allOf(
                hasStatus(status), hasChannel(channel), contractContains(contract),
                scheduledOn(date));
        var result = notificationRepository.findAll(specification,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PageResponse<>(result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(),
                result.getTotalPages(), result.isFirst(), result.isLast());
    }

    @Autowired
    public NotificationService(
            NotificationRepository notificationRepository,
            ContractAssignmentRepository assignmentRepository,
            List<NotificationProvider> providers,
            ContractHistoryService historyService) {
        this(notificationRepository, assignmentRepository, providers,
                historyService, Clock.systemUTC());
    }

    NotificationService(
            NotificationRepository notificationRepository,
            ContractAssignmentRepository assignmentRepository,
            List<NotificationProvider> providers,
            ContractHistoryService historyService,
            Clock clock) {
        this.notificationRepository = notificationRepository;
        this.assignmentRepository = assignmentRepository;
        this.providers = providers.stream().collect(Collectors.toUnmodifiableMap(
                NotificationProvider::getChannel, Function.identity()));
        this.clock = clock;
        this.historyService = historyService;
    }

    @Transactional
    public List<Notification> createForSchedule(NotificationSchedule schedule) {
        var uniquePersons = new LinkedHashMap<UUID, Person>();
        assignmentRepository.findActiveResponsibleAssignments(
                        schedule.getContract().getId())
                .forEach(assignment -> uniquePersons.putIfAbsent(
                        assignment.getPerson().getId(), assignment.getPerson()));

        List<Notification> notifications = new ArrayList<>();
        uniquePersons.values().forEach(person -> {
            if (hasText(person.getEmail())) {
                addIfAbsent(schedule, person, NotificationChannel.EMAIL,
                        person.getEmail(), notifications);
            }
            if (person.isWhatsappEnabled() && hasText(person.getPhone())) {
                addIfAbsent(schedule, person, NotificationChannel.WHATSAPP,
                        person.getPhone(), notifications);
            }
        });
        return notificationRepository.saveAll(notifications);
    }

    @Transactional
    public List<Notification> createAndSendForSchedule(NotificationSchedule schedule) {
        List<Notification> notifications = createForSchedule(schedule);
        notifications.forEach(this::send);
        return notificationRepository.saveAll(notifications);
    }

    private void send(Notification notification) {
        NotificationProvider provider = providers.get(notification.getChannel());
        if (provider == null) {
            notification.markFailed(
                    "No provider configured for channel " + notification.getChannel());
            recordDelivery(notification, HistoryAction.NOTIFICATION_FAILED);
            logFailure(notification, "ProviderNotConfigured");
            return;
        }
        try {
            provider.send(notification);
            notification.markSent(Instant.now(clock));
            recordDelivery(notification, HistoryAction.NOTIFICATION_SENT);
            LOGGER.info("Notification sent notificationId={} contractId={} channel={}",
                    notification.getId(), notification.getContract().getId(),
                    notification.getChannel());
        } catch (RuntimeException exception) {
            notification.markFailed(exception.getMessage());
            recordDelivery(notification, HistoryAction.NOTIFICATION_FAILED);
            logFailure(notification, exception.getClass().getSimpleName());
        }
    }

    private void logFailure(Notification notification, String errorType) {
        LOGGER.warn("Notification failed notificationId={} contractId={} channel={} errorType={}",
                notification.getId(), notification.getContract().getId(),
                notification.getChannel(), errorType);
    }

    private void recordDelivery(Notification notification, HistoryAction action) {
        historyService.record(notification.getContract().getId(), "system",
                "NOTIFICATION", notification.getId(), action, "status=PENDING",
                "status=" + notification.getStatus() + ";channel="
                        + notification.getChannel());
    }

    private void addIfAbsent(
            NotificationSchedule schedule, Person person, NotificationChannel channel,
            String address, List<Notification> notifications) {
        if (notificationRepository.existsForDelivery(
                schedule.getContract().getId(), person.getId(),
                schedule.getExpirationDate(), schedule.getDaysBefore(), channel)) {
            return;
        }

        String subject = "[Contratos] Aviso de vencimento — Contrato "
                + schedule.getContract().getContractNumber() + " — prazo de "
                + schedule.getDaysBefore() + " dias";
        String message = "O contrato " + schedule.getContract().getContractNumber()
                + " possui vigencia ate " + schedule.getExpirationDate() + ".";
        notifications.add(new Notification(
                schedule.getContract(), person, schedule.getExpirationDate(),
                schedule.getDaysBefore(), schedule.getScheduledDate(), channel,
                person.getName(), address, subject, message));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(), notification.getContract().getId(),
                notification.getContract().getContractNumber(),
                notification.getContract().getCompanyName(),
                notification.getPerson().getId(), notification.getRecipientName(),
                notification.getRecipientAddress(), notification.getChannel(),
                notification.getDaysBefore(), notification.getExpirationDate(),
                notification.getScheduledDate(), notification.getStatus(),
                notification.getSentAt(), notification.getErrorMessage(),
                notification.getRetryCount(), notification.getCreatedAt());
    }
}
