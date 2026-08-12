package com.organization.contractmanager.repository;

import com.organization.contractmanager.domain.Contract;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.organization.contractmanager.dto.DashboardContractCounts;
import java.time.LocalDate;

public interface ContractRepository extends JpaRepository<Contract, UUID>,
        JpaSpecificationExecutor<Contract> {

    Optional<Contract> findByContractNumber(String contractNumber);

    boolean existsByContractNumber(String contractNumber);

    @Query("select new com.organization.contractmanager.dto.DashboardContractCounts("
            + "coalesce(sum(case when contract.status = "
            + "com.organization.contractmanager.domain.ContractStatus.ACTIVE then 1L else 0L end), 0L), "
            + "coalesce(sum(case when contract.status = "
            + "com.organization.contractmanager.domain.ContractStatus.ACTIVE "
            + "and contract.endDate < :today then 1L else 0L end), 0L), "
            + "coalesce(sum(case when contract.status = "
            + "com.organization.contractmanager.domain.ContractStatus.ACTIVE "
            + "and contract.endDate between :today and :in15Days then 1L else 0L end), 0L), "
            + "coalesce(sum(case when contract.status = "
            + "com.organization.contractmanager.domain.ContractStatus.ACTIVE "
            + "and contract.endDate between :today and :in30Days then 1L else 0L end), 0L), "
            + "coalesce(sum(case when contract.status = "
            + "com.organization.contractmanager.domain.ContractStatus.ACTIVE "
            + "and contract.endDate between :today and :in60Days then 1L else 0L end), 0L)) "
            + "from Contract contract")
    DashboardContractCounts dashboardCounts(
            @Param("today") LocalDate today,
            @Param("in15Days") LocalDate in15Days,
            @Param("in30Days") LocalDate in30Days,
            @Param("in60Days") LocalDate in60Days);
}
