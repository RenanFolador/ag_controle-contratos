package com.organization.contractmanager.service;

import static com.organization.contractmanager.repository.ContractSpecifications.assignedTo;
import static com.organization.contractmanager.repository.ContractSpecifications.companyContains;
import static com.organization.contractmanager.repository.ContractSpecifications.endDateBetween;
import static com.organization.contractmanager.repository.ContractSpecifications.endsInYear;
import static com.organization.contractmanager.repository.ContractSpecifications.expiredBefore;
import static com.organization.contractmanager.repository.ContractSpecifications.hasStatus;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractAssignment;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.domain.Notification;
import com.organization.contractmanager.domain.NotificationStatus;
import com.organization.contractmanager.report.ReportData;
import com.organization.contractmanager.report.ReportExporter;
import com.organization.contractmanager.report.ReportFile;
import com.organization.contractmanager.report.ReportFilter;
import com.organization.contractmanager.report.ReportFormat;
import com.organization.contractmanager.report.ReportType;
import com.organization.contractmanager.repository.ContractAssignmentRepository;
import com.organization.contractmanager.repository.ContractRepository;
import com.organization.contractmanager.repository.NotificationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private static final ZoneId REPORT_ZONE = ZoneId.of("America/Sao_Paulo");
    private final ContractRepository contractRepository;
    private final ContractAssignmentRepository assignmentRepository;
    private final NotificationRepository notificationRepository;
    private final Map<ReportFormat, ReportExporter> exporters;
    private final Clock clock;

    @Autowired
    public ReportService(
            ContractRepository contractRepository,
            ContractAssignmentRepository assignmentRepository,
            NotificationRepository notificationRepository,
            List<ReportExporter> exporters) {
        this(contractRepository, assignmentRepository, notificationRepository,
                exporters, Clock.system(REPORT_ZONE));
    }

    ReportService(
            ContractRepository contractRepository,
            ContractAssignmentRepository assignmentRepository,
            NotificationRepository notificationRepository,
            List<ReportExporter> exporters,
            Clock clock) {
        this.contractRepository = contractRepository;
        this.assignmentRepository = assignmentRepository;
        this.notificationRepository = notificationRepository;
        this.exporters = exporters.stream().collect(Collectors.toUnmodifiableMap(
                ReportExporter::getFormat, Function.identity()));
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ReportFile export(ReportType type, ReportFormat format, ReportFilter filter) {
        validatePeriod(filter);
        ReportExporter exporter = exporters.get(format);
        if (exporter == null) {
            throw new IllegalArgumentException("Formato de relatório ainda não suportado: " + format);
        }
        ReportData data = switch (type) {
            case ACTIVE_CONTRACTS -> contracts(filter, ContractStatus.ACTIVE, false, false);
            case EXPIRED_CONTRACTS -> contracts(filter, null, true, false);
            case EXPIRING_CONTRACTS -> contracts(filter, ContractStatus.ACTIVE, false, true);
            case CONTRACTS_BY_RESPONSIBLE -> contractsByResponsible(filter);
            case SENT_NOTIFICATIONS -> notifications(filter, NotificationStatus.SENT);
            case FAILED_NOTIFICATIONS -> notifications(filter, NotificationStatus.FAILED);
        };
        String filename = type.name().toLowerCase() + "-" + LocalDate.now(clock)
                + "." + exporter.getExtension();
        return new ReportFile(exporter.export(data), exporter.getContentType(), filename);
    }

    private ReportData contracts(
            ReportFilter filter, ContractStatus forcedStatus,
            boolean expired, boolean expiring) {
        LocalDate today = LocalDate.now(clock);
        LocalDate start = filter.startDate();
        LocalDate end = filter.endDate();
        if (expiring) {
            start = start == null ? today : start;
            end = end == null ? today.plusDays(60) : end;
        }
        Specification<Contract> specification = Specification.allOf(
                hasStatus(forcedStatus == null ? filter.status() : forcedStatus),
                endsInYear(filter.year()), assignedTo(filter.personId()),
                companyContains(filter.company()), endDateBetween(start, end),
                expired ? expiredBefore(today) : null);
        List<List<?>> rows = contractRepository.findAll(
                        specification, Sort.by("endDate", "contractNumber")).stream()
                .map(this::contractRow).toList();
        return new ReportData(contractHeaders(), rows);
    }

    private ReportData contractsByResponsible(ReportFilter filter) {
        Specification<ContractAssignment> specification = (root, query, builder) -> {
            var contract = root.get("contract");
            var person = root.get("person");
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (filter.personId() != null) predicates.add(
                    builder.equal(person.get("id"), filter.personId()));
            if (filter.status() != null) predicates.add(
                    builder.equal(contract.get("status"), filter.status()));
            if (filter.year() != null) predicates.add(builder.equal(
                    builder.function("year", Integer.class, contract.get("endDate")),
                    filter.year()));
            if (filter.startDate() != null) predicates.add(builder.greaterThanOrEqualTo(
                    contract.get("endDate"), filter.startDate()));
            if (filter.endDate() != null) predicates.add(builder.lessThanOrEqualTo(
                    contract.get("endDate"), filter.endDate()));
            if (filter.company() != null && !filter.company().isBlank()) predicates.add(
                    builder.like(builder.lower(contract.get("companyName")),
                            "%" + filter.company().trim().toLowerCase() + "%"));
            return builder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        List<List<?>> rows = assignmentRepository.findAll(specification).stream()
                .map(this::assignmentRow).toList();
        return new ReportData(List.of(
                "Contrato", "Empresa", "Data final", "Status", "Responsável",
                "Função", "Início do vínculo", "Fim do vínculo", "Vínculo ativo"), rows);
    }

    private ReportData notifications(ReportFilter filter, NotificationStatus status) {
        LocalDate startDate = filter.startDate();
        LocalDate endDate = filter.endDate();
        if (filter.year() != null) {
            if (startDate == null) startDate = LocalDate.of(filter.year(), 1, 1);
            if (endDate == null) endDate = LocalDate.of(filter.year(), 12, 31);
        }
        Instant start = startDate == null ? null : startDate.atStartOfDay(REPORT_ZONE).toInstant();
        Instant end = endDate == null ? null
                : endDate.plusDays(1).atStartOfDay(REPORT_ZONE).toInstant();
        Specification<Notification> specification = Specification.allOf(
                com.organization.contractmanager.repository.NotificationSpecifications
                        .hasStatus(status),
                com.organization.contractmanager.repository.NotificationSpecifications
                        .contractStatus(filter.status()),
                com.organization.contractmanager.repository.NotificationSpecifications
                        .personIs(filter.personId()),
                com.organization.contractmanager.repository.NotificationSpecifications
                        .companyContains(filter.company()),
                status == NotificationStatus.SENT
                        ? com.organization.contractmanager.repository.NotificationSpecifications
                                .sentBetween(start, end)
                        : com.organization.contractmanager.repository.NotificationSpecifications
                                .createdBetween(start, end));
        List<List<?>> rows = notificationRepository.findAll(
                        specification, Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(this::notificationRow).toList();
        return new ReportData(List.of(
                "Contrato", "Empresa", "Destinatário", "Canal", "Prazo (dias)",
                "Data prevista", "Status", "Enviada em", "Erro"), rows);
    }

    private List<String> contractHeaders() {
        return List.of("Contrato", "Processo", "Empresa", "CNPJ", "Objeto",
                "Data inicial", "Data final", "Valor inicial", "Status");
    }

    private List<?> contractRow(Contract contract) {
        return List.of(contract.getContractNumber(), value(contract.getProcessNumber()),
                contract.getCompanyName(), value(contract.getCompanyCnpj()), contract.getObject(),
                contract.getStartDate(), contract.getEndDate(), contract.getInitialValue(),
                contract.getStatus());
    }

    private List<?> assignmentRow(ContractAssignment assignment) {
        return List.of(assignment.getContract().getContractNumber(),
                assignment.getContract().getCompanyName(), assignment.getContract().getEndDate(),
                assignment.getContract().getStatus(), assignment.getPerson().getName(),
                assignment.getRole(), assignment.getStartDate(), value(assignment.getEndDate()),
                assignment.isActive());
    }

    private List<?> notificationRow(Notification notification) {
        return List.of(notification.getContract().getContractNumber(),
                notification.getContract().getCompanyName(), notification.getRecipientName(),
                notification.getChannel(), notification.getDaysBefore(),
                notification.getScheduledDate(), notification.getStatus(),
                value(notification.getSentAt()), value(notification.getErrorMessage()));
    }

    private Object value(Object value) {
        return value == null ? "" : value;
    }

    private void validatePeriod(ReportFilter filter) {
        if (filter.startDate() != null && filter.endDate() != null
                && filter.endDate().isBefore(filter.startDate())) {
            throw new IllegalArgumentException("A data final do período não pode anteceder a inicial");
        }
    }
}
