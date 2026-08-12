package com.organization.contractmanager.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contract_history", indexes = @Index(
        name = "idx_contract_history_contract_occurred_at",
        columnList = "contract_id, occurred_at"))
public class ContractHistory {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "contract_id", nullable = false, updatable = false)
    private UUID contractId;
    @Column(nullable = false, updatable = false)
    private String actor;
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant timestamp;
    @Column(name = "entity_type", nullable = false, updatable = false, length = 50)
    private String entityType;
    @Column(name = "entity_id", nullable = false, updatable = false)
    private UUID entityId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 50)
    private HistoryAction action;
    @Column(name = "old_value", columnDefinition = "TEXT", updatable = false)
    private String oldValue;
    @Column(name = "new_value", columnDefinition = "TEXT", updatable = false)
    private String newValue;

    protected ContractHistory() { }

    public ContractHistory(UUID contractId, String actor, String entityType,
                           UUID entityId, HistoryAction action,
                           String oldValue, String newValue) {
        this.contractId = contractId;
        this.actor = actor;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @PrePersist void onCreate() { timestamp = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getContractId() { return contractId; }
    public String getActor() { return actor; }
    public Instant getTimestamp() { return timestamp; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public HistoryAction getAction() { return action; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
}
