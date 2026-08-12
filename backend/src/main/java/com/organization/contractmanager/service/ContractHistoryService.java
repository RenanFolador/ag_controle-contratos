package com.organization.contractmanager.service;

import com.organization.contractmanager.domain.ContractHistory;
import com.organization.contractmanager.domain.HistoryAction;
import com.organization.contractmanager.dto.ContractHistoryResponse;
import com.organization.contractmanager.repository.ContractHistoryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractHistoryService {
    private final ContractHistoryRepository repository;
    public ContractHistoryService(ContractHistoryRepository repository) {
        this.repository = repository;
    }
    @Transactional
    public void record(UUID contractId, String actor, String entityType, UUID entityId,
                       HistoryAction action, String oldValue, String newValue) {
        repository.save(new ContractHistory(contractId, actor, entityType, entityId,
                action, oldValue, newValue));
    }
    @Transactional(readOnly = true)
    public List<ContractHistoryResponse> findByContract(UUID contractId) {
        return repository.findAllByContractIdOrderByTimestampDesc(contractId).stream()
                .map(item -> new ContractHistoryResponse(
                        item.getId(), item.getActor(), item.getTimestamp(),
                        item.getEntityType(), item.getEntityId(), item.getAction(),
                        item.getOldValue(), item.getNewValue()))
                .toList();
    }
}
