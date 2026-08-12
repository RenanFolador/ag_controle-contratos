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
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "contract_assignments",
        indexes = {
            @Index(name = "idx_contract_assignments_contract_id", columnList = "contract_id"),
            @Index(name = "idx_contract_assignments_person_id", columnList = "person_id"),
            @Index(name = "idx_contract_assignments_contract_active", columnList = "contract_id, active"),
            @Index(name = "idx_contract_assignments_person_active", columnList = "person_id, active")
        })
public class ContractAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContractRole role;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    protected ContractAssignment() {
    }

    public ContractAssignment(Contract contract, Person person, ContractRole role,
                              LocalDate startDate, LocalDate endDate, String createdBy) {
        this.contract = contract;
        this.person = person;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
        this.active = true;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void end(LocalDate endDate) {
        this.endDate = endDate;
        this.active = false;
    }

    public void update(Person person, ContractRole role, LocalDate startDate, LocalDate endDate) {
        this.person = person;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UUID getId() {
        return id;
    }

    public Contract getContract() {
        return contract;
    }

    public Person getPerson() {
        return person;
    }

    public ContractRole getRole() {
        return role;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
