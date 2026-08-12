package com.organization.contractmanager.mapper;

import com.organization.contractmanager.domain.Contract;
import com.organization.contractmanager.domain.ContractStatus;
import com.organization.contractmanager.dto.ContractCreateRequest;
import com.organization.contractmanager.dto.ContractResponse;
import com.organization.contractmanager.dto.ContractSummaryResponse;
import com.organization.contractmanager.dto.ContractUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    public Contract toEntity(ContractCreateRequest request, String actor) {
        return new Contract(
                request.contractNumber(), request.processNumber(), request.object(),
                request.companyName(), request.companyCnpj(), request.startDate(),
                request.endDate(), request.initialValue(),
                request.status() == null ? ContractStatus.ACTIVE : request.status(),
                request.notes(), actor);
    }

    public void update(Contract contract, ContractUpdateRequest request, String actor) {
        contract.setContractNumber(request.contractNumber());
        contract.setProcessNumber(request.processNumber());
        contract.setObject(request.object());
        contract.setCompanyName(request.companyName());
        contract.setCompanyCnpj(request.companyCnpj());
        contract.setStartDate(request.startDate());
        contract.setEndDate(request.endDate());
        contract.setInitialValue(request.initialValue());
        contract.setStatus(request.status());
        contract.setNotes(request.notes());
        contract.setUpdatedBy(actor);
    }

    public ContractResponse toResponse(Contract contract) {
        return new ContractResponse(
                contract.getId(), contract.getContractNumber(), contract.getProcessNumber(),
                contract.getObject(), contract.getCompanyName(), contract.getCompanyCnpj(),
                contract.getStartDate(), contract.getEndDate(), contract.getInitialValue(),
                contract.getStatus(), contract.getNotes(), contract.getCreatedAt(),
                contract.getUpdatedAt(), contract.getCreatedBy(), contract.getUpdatedBy());
    }

    public ContractSummaryResponse toSummary(Contract contract) {
        return new ContractSummaryResponse(
                contract.getId(), contract.getContractNumber(), contract.getProcessNumber(),
                contract.getObject(), contract.getCompanyName(), contract.getCompanyCnpj(),
                contract.getStartDate(), contract.getEndDate(), contract.getInitialValue(),
                contract.getStatus());
    }
}
