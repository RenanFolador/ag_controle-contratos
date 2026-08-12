package com.organization.contractmanager.exception;

public class InvalidContractDateRangeException extends RuntimeException {

    public InvalidContractDateRangeException() {
        super("End date cannot be before start date");
    }
}
