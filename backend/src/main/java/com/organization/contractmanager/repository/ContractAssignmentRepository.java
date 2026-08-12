package com.organization.contractmanager.repository;

import com.organization.contractmanager.domain.ContractAssignment;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContractAssignmentRepository extends JpaRepository<ContractAssignment, UUID>,
        JpaSpecificationExecutor<ContractAssignment> {

    @Override
    @EntityGraph(attributePaths = {"contract", "person"})
    List<ContractAssignment> findAll(Specification<ContractAssignment> specification);

    @EntityGraph(attributePaths = "person")
    List<ContractAssignment> findAllByContractIdOrderByCreatedAtAsc(UUID contractId);

    List<ContractAssignment> findAllByContractIdAndActiveTrue(UUID contractId);

    List<ContractAssignment> findAllByPersonIdAndActiveTrue(UUID personId);

    Optional<ContractAssignment> findByIdAndContractId(UUID id, UUID contractId);

    boolean existsByContractIdAndPersonId(UUID contractId, UUID personId);

    @Query("select assignment from ContractAssignment assignment "
            + "join fetch assignment.person person "
            + "where assignment.contract.id = :contractId "
            + "and assignment.active = true and person.active = true")
    List<ContractAssignment> findActiveResponsibleAssignments(
            @Param("contractId") UUID contractId);
}
