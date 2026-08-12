package com.organization.contractmanager.repository;

import com.organization.contractmanager.domain.ContractHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractHistoryRepository extends JpaRepository<ContractHistory, UUID> {
    List<ContractHistory> findAllByContractIdOrderByTimestampDesc(UUID contractId);
}
