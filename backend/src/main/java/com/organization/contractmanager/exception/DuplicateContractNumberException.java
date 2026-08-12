package com.organization.contractmanager.exception;

public class DuplicateContractNumberException extends RuntimeException {

    public DuplicateContractNumberException(String contractNumber) {
        super("Contract number already exists: " + contractNumber);
    }
}
