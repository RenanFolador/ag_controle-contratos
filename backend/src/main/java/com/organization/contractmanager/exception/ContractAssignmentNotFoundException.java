package com.organization.contractmanager.exception;

import java.util.UUID;

public class ContractAssignmentNotFoundException extends RuntimeException {

    public ContractAssignmentNotFoundException(UUID assignmentId, UUID contractId) {
        super("Assignment " + assignmentId + " not found for contract " + contractId);
    }
}
