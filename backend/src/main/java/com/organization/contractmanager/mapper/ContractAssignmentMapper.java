package com.organization.contractmanager.mapper;

import com.organization.contractmanager.domain.ContractAssignment;
import com.organization.contractmanager.domain.Person;
import com.organization.contractmanager.dto.AssignmentPersonResponse;
import com.organization.contractmanager.dto.ContractAssignmentResponse;
import org.springframework.stereotype.Component;

@Component
public class ContractAssignmentMapper {

    public ContractAssignmentResponse toResponse(ContractAssignment assignment) {
        Person person = assignment.getPerson();
        return new ContractAssignmentResponse(
                assignment.getId(), assignment.getContract().getId(),
                new AssignmentPersonResponse(
                        person.getId(), person.getName(), person.getRegistration(),
                        person.getEmail(), person.getPhone(), person.isWhatsappEnabled(),
                        person.isActive()),
                assignment.getRole(), assignment.getStartDate(), assignment.getEndDate(),
                assignment.isActive(), assignment.getCreatedAt(), assignment.getCreatedBy());
    }
}
